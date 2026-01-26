import { flushSync } from "react-dom"
import { createAppAsyncThunk } from "@/app/async-thunk"
import { client } from "@/app/client"
import { Constants } from "@/app/constants"
import {
    type EntrySource,
    type EntrySourceType,
    entriesSlice,
    setMarkAllAsReadConfirmationDialogOpen,
    setSearch,
} from "@/app/entries/slice"
import type { RootState } from "@/app/store"
import { reloadTree, selectNextUnreadTreeItem } from "@/app/tree/thunks"
import type { Entry, MarkRequest, TagRequest } from "@/app/types"
import { reloadTags } from "@/app/user/thunks"
import { scrollToWithCallback } from "@/app/utils"

const getEndpoint = (sourceType: EntrySourceType) =>
    sourceType === "category" || sourceType === "tag" ? client.category.getEntries : client.feed.getEntries

export const loadEntries = createAppAsyncThunk(
    "entries/load",
    async (
        arg: {
            source: EntrySource
            clearSearch: boolean
        },
        thunkApi
    ) => {
        if (arg.clearSearch) thunkApi.dispatch(setSearch(""))

        const state = thunkApi.getState()
        const endpoint = getEndpoint(arg.source.type)
        const result = await endpoint(buildGetEntriesPaginatedRequest(state, arg.source, 0))
        return result.data
    }
)

export const loadMoreEntries = createAppAsyncThunk("entries/loadMore", async (_, thunkApi) => {
    const state = thunkApi.getState()
    const { source } = state.entries
    const offset =
        state.user.settings?.readingMode === "all" || (source.type === "category" && source.id === "starred")
            ? state.entries.entries.length
            : state.entries.entries.filter(e => !e.read).length
    const endpoint = getEndpoint(state.entries.source.type)
    const result = await endpoint(buildGetEntriesPaginatedRequest(state, source, offset))
    return result.data
})

const buildGetEntriesPaginatedRequest = (state: RootState, source: EntrySource, offset: number) => ({
    id: source.type === "tag" ? Constants.categories.all.id : source.id,
    order: state.user.settings?.readingOrder,
    readType: state.entries.search ? "all" : state.user.settings?.readingMode,
    offset,
    limit: 50,
    tag: source.type === "tag" ? source.id : undefined,
    keywords: state.entries.search,
})

export const reloadEntries = createAppAsyncThunk("entries/reload", (_, thunkApi) => {
    const state = thunkApi.getState()
    thunkApi.dispatch(loadEntries({ source: state.entries.source, clearSearch: false }))
})

export const search = createAppAsyncThunk("entries/search", (arg: string, thunkApi) => {
    const state = thunkApi.getState()
    thunkApi.dispatch(setSearch(arg))
    thunkApi.dispatch(loadEntries({ source: state.entries.source, clearSearch: false }))
})

export const markEntry = createAppAsyncThunk(
    "entries/entry/mark",
    (arg: { entry: Entry; read: boolean }) => {
        client.entry.mark({
            id: arg.entry.id,
            read: arg.read,
        })
    },
    {
        condition: arg => arg.entry.markable && arg.entry.read !== arg.read,
    }
)

export const markMultipleEntries = createAppAsyncThunk(
    "entries/entry/markMultiple",
    async (
        arg: {
            entries: Entry[]
            read: boolean
        },
        thunkApi
    ) => {
        const requests: MarkRequest[] = arg.entries.map(e => ({
            id: e.id,
            read: arg.read,
        }))
        await client.entry.markMultiple({ requests })
        thunkApi.dispatch(reloadTree())
    }
)

export const markEntriesUpToEntry = createAppAsyncThunk("entries/entry/upToEntry", (arg: Entry, thunkApi) => {
    const state = thunkApi.getState()
    const { entries } = state.entries

    const index = entries.findIndex(e => e.id === arg.id)
    if (index === -1) return

    thunkApi.dispatch(
        markMultipleEntries({
            entries: entries.slice(0, index + 1),
            read: true,
        })
    )
})

export const markAllEntries = createAppAsyncThunk(
    "entries/entry/markAll",
    async (
        arg: {
            sourceType: EntrySourceType
            req: MarkRequest
        },
        thunkApi
    ) => {
        const endpoint = arg.sourceType === "category" ? client.category.markEntries : client.feed.markEntries
        await endpoint(arg.req)
        thunkApi.dispatch(reloadEntries())
        thunkApi.dispatch(reloadTree())
    }
)

export const markAllAsReadWithConfirmationIfRequired = createAppAsyncThunk(
    "entries/entry/markAllAsReadWithConfirmationIfRequired",
    async (_, thunkApi) => {
        const state = thunkApi.getState()
        const source = state.entries.source
        const entriesTimestamp = state.entries.timestamp ?? Date.now()
        const markAllAsReadConfirmation = state.user.settings?.markAllAsReadConfirmation
        const markAllAsReadNavigateToNextUnread = state.user.settings?.markAllAsReadNavigateToNextUnread

        if (markAllAsReadConfirmation) {
            thunkApi.dispatch(setMarkAllAsReadConfirmationDialogOpen(true))
        } else {
            await thunkApi.dispatch(
                markAllEntries({
                    sourceType: source.type,
                    req: {
                        id: source.id,
                        read: true,
                        olderThan: Date.now(),
                        insertedBefore: entriesTimestamp,
                    },
                })
            )
            const isAllCategorySelected = source.type === "category" && source.id === Constants.categories.all.id
            if (markAllAsReadNavigateToNextUnread && !isAllCategorySelected)
                await thunkApi.dispatch(selectNextUnreadTreeItem({ direction: "forward" }))
        }
    }
)

export const starEntry = createAppAsyncThunk(
    "entries/entry/star",
    (arg: { entry: Entry; starred: boolean }) => {
        client.entry.star({
            id: arg.entry.id,
            feedId: +arg.entry.feedId,
            starred: arg.starred,
        })
    },
    {
        condition: arg => arg.entry.markable && arg.entry.starred !== arg.starred,
    }
)

export const selectEntry = createAppAsyncThunk(
    "entries/entry/select",
    (
        arg: {
            entry: Entry
            expand: boolean
            markAsRead: boolean
            scrollToEntry: boolean
        },
        thunkApi
    ) => {
        const state = thunkApi.getState()
        const entry = state.entries.entries.find(e => e.id === arg.entry.id)
        if (!entry) return

        // flushSync is required because we need the newly selected entry to be expanded
        // and the previously selected entry to be collapsed to be able to scroll to the right position
        flushSync(() => {
            // mark as read if requested
            if (arg.markAsRead) {
                thunkApi.dispatch(markEntry({ entry, read: true }))
            }

            // set entry as selected
            thunkApi.dispatch(entriesSlice.actions.setSelectedEntry(entry))

            // expand if requested
            const previouslySelectedEntry = state.entries.entries.find(e => e.id === state.entries.selectedEntryId)
            if (previouslySelectedEntry) {
                thunkApi.dispatch(
                    entriesSlice.actions.setEntryExpanded({
                        entry: previouslySelectedEntry,
                        expanded: false,
                    })
                )
            }
            thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry, expanded: arg.expand }))
        })

        if (arg.scrollToEntry) {
            const viewMode = state.user.localSettings.viewMode

            const entryIndex = state.entries.entries.indexOf(entry)
            const entriesToKeepOnTopWhenScrolling =
                viewMode === "expanded" ? 0 : Math.min(state.user.settings?.entriesToKeepOnTopWhenScrolling ?? 0, entryIndex)
            const entryToScrollTo = state.entries.entries[entryIndex - entriesToKeepOnTopWhenScrolling]

            const entryElement = document.getElementById(Constants.dom.entryId(entry))
            const entryElementToScrollTo = document.getElementById(Constants.dom.entryId(entryToScrollTo))
            if (entryElement && entryElementToScrollTo) {
                const scrollMode = state.user.settings?.scrollMode
                const entryEntirelyVisible =
                    Constants.layout.isTopVisible(entryElementToScrollTo) && Constants.layout.isBottomVisible(entryElement)
                if (scrollMode === "always" || (scrollMode === "if_needed" && !entryEntirelyVisible)) {
                    const scrollSpeed = state.user.settings?.scrollSpeed
                    const margin = viewMode === "detailed" ? 8 : 3
                    thunkApi.dispatch(entriesSlice.actions.setScrollingToEntry(true))
                    scrollToEntry(entryElementToScrollTo, margin, scrollSpeed, () =>
                        thunkApi.dispatch(entriesSlice.actions.setScrollingToEntry(false))
                    )
                }
            }
        }
    }
)

const scrollToEntry = (entryElement: HTMLElement, margin: number, scrollSpeed: number | undefined, onScrollEnded: () => void) => {
    const header = document.getElementsByTagName("header").item(0)?.getBoundingClientRect()
    const offset = (header?.bottom ?? 0) + margin
    scrollToWithCallback({
        options: {
            top: entryElement.offsetTop - offset,
            behavior: scrollSpeed && scrollSpeed > 0 ? "smooth" : "auto",
        },
        onScrollEnded,
    })
}

export const selectPreviousEntry = createAppAsyncThunk(
    "entries/entry/selectPrevious",
    (
        arg: {
            expand: boolean
            markAsRead: boolean
            scrollToEntry: boolean
        },
        thunkApi
    ) => {
        const state = thunkApi.getState()
        const { entries } = state.entries
        const previousIndex = entries.findIndex(e => e.id === state.entries.selectedEntryId) - 1
        if (previousIndex >= 0) {
            thunkApi.dispatch(
                selectEntry({
                    entry: entries[previousIndex],
                    expand: arg.expand,
                    markAsRead: arg.markAsRead,
                    scrollToEntry: arg.scrollToEntry,
                })
            )
        }
    }
)

export const selectNextEntry = createAppAsyncThunk(
    "entries/entry/selectNext",
    async (
        arg: {
            expand: boolean
            markAsRead: boolean
            scrollToEntry: boolean
        },
        thunkApi
    ) => {
        const state = thunkApi.getState()
        const { entries, hasMore, loading } = state.entries
        const nextIndex = entries.findIndex(e => e.id === state.entries.selectedEntryId) + 1

        // load more entries if needed
        // this can happen if the last entry is too large to fit on the screen and the infinite loader doesn't trigger
        if (nextIndex >= entries.length && hasMore && !loading) {
            await thunkApi.dispatch(loadMoreEntries())
        }

        const entriesAfterLoading = thunkApi.getState().entries.entries
        if (nextIndex < entriesAfterLoading.length) {
            thunkApi.dispatch(
                selectEntry({
                    entry: entriesAfterLoading[nextIndex],
                    expand: arg.expand,
                    markAsRead: arg.markAsRead,
                    scrollToEntry: arg.scrollToEntry,
                })
            )
        }
    }
)

export const tagEntry = createAppAsyncThunk("entries/entry/tag", async (arg: TagRequest, thunkApi) => {
    await client.entry.tag(arg)
    thunkApi.dispatch(reloadTags())
})
