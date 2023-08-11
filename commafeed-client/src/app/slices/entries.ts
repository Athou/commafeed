import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit"
import { client } from "app/client"
import { Constants } from "app/constants"
import { RootState } from "app/store"
import { Entries, Entry, MarkRequest, TagRequest } from "app/types"
import { scrollToWithCallback } from "app/utils"
import { flushSync } from "react-dom"
// eslint-disable-next-line import/no-cycle
import { reloadTree } from "./tree"
// eslint-disable-next-line import/no-cycle
import { reloadTags } from "./user"

export type EntrySourceType = "category" | "feed" | "tag"
export type EntrySource = { type: EntrySourceType; id: string }
export type ExpendableEntry = Entry & { expanded?: boolean }

interface EntriesState {
    /** selected source */
    source: EntrySource
    sourceLabel: string
    sourceWebsiteUrl: string
    entries: ExpendableEntry[]
    /** stores when the first batch of entries were retrieved
     *
     * this is used when marking all entries of a feed/category to only mark entries up to that timestamp as newer entries were potentially never shown
     */
    timestamp?: number
    selectedEntryId?: string
    hasMore: boolean
    loading: boolean
    search?: string
    scrollingToEntry: boolean
}

const initialState: EntriesState = {
    source: {
        type: "category",
        id: Constants.categories.all.id,
    },
    sourceLabel: "",
    sourceWebsiteUrl: "",
    entries: [],
    hasMore: true,
    loading: false,
    scrollingToEntry: false,
}

const getEndpoint = (sourceType: EntrySourceType) =>
    sourceType === "category" || sourceType === "tag" ? client.category.getEntries : client.feed.getEntries
export const loadEntries = createAsyncThunk<
    Entries,
    { source: EntrySource; clearSearch: boolean },
    {
        state: RootState
    }
>("entries/load", async (arg, thunkApi) => {
    if (arg.clearSearch) thunkApi.dispatch(setSearch(""))

    const state = thunkApi.getState()
    const endpoint = getEndpoint(arg.source.type)
    const result = await endpoint(buildGetEntriesPaginatedRequest(state, arg.source, 0))
    return result.data
})
export const loadMoreEntries = createAsyncThunk<
    Entries,
    void,
    {
        state: RootState
    }
>("entries/loadMore", async (_, thunkApi) => {
    const state = thunkApi.getState()
    const { source } = state.entries
    const offset =
        state.user.settings?.readingMode === "all" ? state.entries.entries.length : state.entries.entries.filter(e => !e.read).length
    const endpoint = getEndpoint(state.entries.source.type)
    const result = await endpoint(buildGetEntriesPaginatedRequest(state, source, offset))
    return result.data
})
const buildGetEntriesPaginatedRequest = (state: RootState, source: EntrySource, offset: number) => ({
    id: source.type === "tag" ? Constants.categories.all.id : source.id,
    order: state.user.settings?.readingOrder,
    readType: state.user.settings?.readingMode,
    offset,
    limit: 50,
    tag: source.type === "tag" ? source.id : undefined,
    keywords: state.entries.search,
})
export const reloadEntries = createAsyncThunk<
    void,
    void,
    {
        state: RootState
    }
>("entries/reload", async (arg, thunkApi) => {
    const state = thunkApi.getState()
    thunkApi.dispatch(loadEntries({ source: state.entries.source, clearSearch: false }))
})
export const search = createAsyncThunk<void, string, { state: RootState }>("entries/search", async (arg, thunkApi) => {
    const state = thunkApi.getState()
    thunkApi.dispatch(setSearch(arg))
    thunkApi.dispatch(loadEntries({ source: state.entries.source, clearSearch: false }))
})
export const markEntry = createAsyncThunk(
    "entries/entry/mark",
    (arg: { entry: Entry; read: boolean }) => {
        client.entry.mark({
            id: arg.entry.id,
            read: arg.read,
        })
    },
    {
        condition: arg => arg.entry.read !== arg.read,
    }
)
export const markMultipleEntries = createAsyncThunk(
    "entries/entry/markMultiple",
    async (arg: { entries: Entry[]; read: boolean }, thunkApi) => {
        const requests: MarkRequest[] = arg.entries.map(e => ({
            id: e.id,
            read: arg.read,
        }))
        await client.entry.markMultiple({ requests })
        thunkApi.dispatch(reloadTree())
    }
)
export const markEntriesUpToEntry = createAsyncThunk<void, Entry, { state: RootState }>(
    "entries/entry/upToEntry",
    async (arg, thunkApi) => {
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
    }
)
export const markAllEntries = createAsyncThunk<
    void,
    { sourceType: EntrySourceType; req: MarkRequest },
    {
        state: RootState
    }
>("entries/entry/markAll", async (arg, thunkApi) => {
    const endpoint = arg.sourceType === "category" ? client.category.markEntries : client.feed.markEntries
    await endpoint(arg.req)
    thunkApi.dispatch(reloadEntries())
    thunkApi.dispatch(reloadTree())
})
export const starEntry = createAsyncThunk("entries/entry/star", (arg: { entry: Entry; starred: boolean }) => {
    client.entry.star({
        id: arg.entry.id,
        feedId: +arg.entry.feedId,
        starred: arg.starred,
    })
})
export const selectEntry = createAsyncThunk<
    void,
    {
        entry: Entry
        expand: boolean
        markAsRead: boolean
        scrollToEntry: boolean
    },
    { state: RootState }
>("entries/entry/select", (arg, thunkApi) => {
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
            thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry: previouslySelectedEntry, expanded: false }))
        }
        thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry, expanded: arg.expand }))
    })

    if (arg.scrollToEntry) {
        const entryElement = document.getElementById(Constants.dom.entryId(entry))
        if (entryElement) {
            const alwaysScrollToEntry = state.user.settings?.alwaysScrollToEntry
            const entryEntirelyVisible = Constants.layout.isTopVisible(entryElement) && Constants.layout.isBottomVisible(entryElement)
            if (alwaysScrollToEntry || !entryEntirelyVisible) {
                const scrollSpeed = state.user.settings?.scrollSpeed
                thunkApi.dispatch(entriesSlice.actions.setScrollingToEntry(true))
                scrollToEntry(entryElement, scrollSpeed, () => thunkApi.dispatch(entriesSlice.actions.setScrollingToEntry(false)))
            }
        }
    }
})
const scrollToEntry = (entryElement: HTMLElement, scrollSpeed: number | undefined, onScrollEnded: () => void) => {
    scrollToWithCallback({
        options: {
            // add a small gap between the top of the content and the top of the page
            top: entryElement.offsetTop - Constants.layout.headerHeight - 3,
            behavior: scrollSpeed && scrollSpeed > 0 ? "smooth" : "auto",
        },
        onScrollEnded,
    })
}

export const selectPreviousEntry = createAsyncThunk<
    void,
    {
        expand: boolean
        markAsRead: boolean
        scrollToEntry: boolean
    },
    { state: RootState }
>("entries/entry/selectPrevious", (arg, thunkApi) => {
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
})
export const selectNextEntry = createAsyncThunk<
    void,
    {
        expand: boolean
        markAsRead: boolean
        scrollToEntry: boolean
    },
    { state: RootState }
>("entries/entry/selectNext", (arg, thunkApi) => {
    const state = thunkApi.getState()
    const { entries } = state.entries
    const nextIndex = entries.findIndex(e => e.id === state.entries.selectedEntryId) + 1
    if (nextIndex < entries.length) {
        thunkApi.dispatch(
            selectEntry({
                entry: entries[nextIndex],
                expand: arg.expand,
                markAsRead: arg.markAsRead,
                scrollToEntry: arg.scrollToEntry,
            })
        )
    }
})
export const tagEntry = createAsyncThunk<
    void,
    TagRequest,
    {
        state: RootState
    }
>("entries/entry/tag", async (arg, thunkApi) => {
    await client.entry.tag(arg)
    thunkApi.dispatch(reloadTags())
})

export const entriesSlice = createSlice({
    name: "entries",
    initialState,
    reducers: {
        setSelectedEntry: (state, action: PayloadAction<Entry>) => {
            state.selectedEntryId = action.payload.id
        },
        setEntryExpanded: (state, action: PayloadAction<{ entry: Entry; expanded: boolean }>) => {
            state.entries
                .filter(e => e.id === action.payload.entry.id)
                .forEach(e => {
                    e.expanded = action.payload.expanded
                })
        },
        setScrollingToEntry: (state, action: PayloadAction<boolean>) => {
            state.scrollingToEntry = action.payload
        },
        setSearch: (state, action: PayloadAction<string>) => {
            state.search = action.payload
        },
    },
    extraReducers: builder => {
        builder.addCase(markEntry.pending, (state, action) => {
            state.entries
                .filter(e => e.id === action.meta.arg.entry.id)
                .forEach(e => {
                    e.read = action.meta.arg.read
                })
        })
        builder.addCase(markMultipleEntries.pending, (state, action) => {
            state.entries
                .filter(e => action.meta.arg.entries.some(e2 => e2.id === e.id))
                .forEach(e => {
                    e.read = action.meta.arg.read
                })
        })
        builder.addCase(markAllEntries.pending, (state, action) => {
            state.entries
                .filter(e => (action.meta.arg.req.olderThan ? e.date < action.meta.arg.req.olderThan : true))
                .forEach(e => {
                    e.read = true
                })
        })
        builder.addCase(starEntry.pending, (state, action) => {
            state.entries
                .filter(e => action.meta.arg.entry.id === e.id && action.meta.arg.entry.feedId === e.feedId)
                .forEach(e => {
                    e.starred = action.meta.arg.starred
                })
        })
        builder.addCase(loadEntries.pending, (state, action) => {
            state.source = action.meta.arg.source
            state.entries = []
            state.timestamp = undefined
            state.sourceLabel = ""
            state.sourceWebsiteUrl = ""
            state.hasMore = true
            state.selectedEntryId = undefined
            state.loading = true
        })
        builder.addCase(loadMoreEntries.pending, state => {
            state.loading = true
        })
        builder.addCase(loadEntries.fulfilled, (state, action) => {
            state.entries = action.payload.entries
            state.timestamp = action.payload.timestamp
            state.sourceLabel = action.payload.name
            state.sourceWebsiteUrl = action.payload.feedLink
            state.hasMore = action.payload.hasMore
            state.loading = false
        })
        builder.addCase(loadMoreEntries.fulfilled, (state, action) => {
            // remove already existing entries
            const entriesToAdd = action.payload.entries.filter(e => !state.entries.some(e2 => e.id === e2.id))
            state.entries = [...state.entries, ...entriesToAdd]
            state.hasMore = action.payload.hasMore
            state.loading = false
        })
        builder.addCase(tagEntry.pending, (state, action) => {
            state.entries
                .filter(e => +e.id === action.meta.arg.entryId)
                .forEach(e => {
                    e.tags = action.meta.arg.tags
                })
        })
    },
})

export const { setSearch } = entriesSlice.actions
export default entriesSlice.reducer
