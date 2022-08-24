import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit"
import { client } from "app/client"
import { Constants } from "app/constants"
import { RootState } from "app/store"
import { Entries, Entry, MarkRequest } from "app/types"
// eslint-disable-next-line import/no-cycle
import { reloadTree } from "./tree"

export type EntrySourceType = "category" | "feed"
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
}

const getEndpoint = (sourceType: EntrySourceType) => (sourceType === "category" ? client.category.getEntries : client.feed.getEntries)
export const loadEntries = createAsyncThunk<Entries, EntrySource, { state: RootState }>("entries/load", async (source, thunkApi) => {
    const state = thunkApi.getState()
    const endpoint = getEndpoint(source.type)
    const result = await endpoint({
        id: source.id,
        order: state.user.settings?.readingOrder,
        readType: state.user.settings?.readingMode,
        offset: 0,
        limit: 50,
    })
    return result.data
})
export const loadMoreEntries = createAsyncThunk<Entries, void, { state: RootState }>("entries/loadMore", async (_, thunkApi) => {
    const state = thunkApi.getState()
    const offset =
        state.user.settings?.readingMode === "all" ? state.entries.entries.length : state.entries.entries.filter(e => !e.read).length
    const endpoint = getEndpoint(state.entries.source.type)
    const result = await endpoint({
        id: state.entries.source.id,
        readType: state.user.settings?.readingMode,
        order: state.user.settings?.readingOrder,
        offset,
        limit: 50,
    })
    return result.data
})
export const reloadEntries = createAsyncThunk<void, void, { state: RootState }>("entries/reload", async (_, thunkApi) => {
    const state = thunkApi.getState()
    thunkApi.dispatch(loadEntries(state.entries.source))
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
export const markAllEntries = createAsyncThunk<void, { sourceType: EntrySourceType; req: MarkRequest }, { state: RootState }>(
    "entries/entry/markAll",
    async (arg, thunkApi) => {
        const endpoint = arg.sourceType === "category" ? client.category.markEntries : client.feed.markEntries
        await endpoint(arg.req)
        thunkApi.dispatch(reloadTree())
    }
)
export const starEntry = createAsyncThunk("entries/entry/star", (arg: { entry: Entry; starred: boolean }) => {
    client.entry.star({
        id: arg.entry.id,
        feedId: +arg.entry.feedId,
        starred: arg.starred,
    })
})
export const selectEntry = createAsyncThunk<void, Entry, { state: RootState }>("entries/entry/select", (arg, thunkApi) => {
    const state = thunkApi.getState()
    const entry = state.entries.entries.find(e => e.id === arg.id)
    if (!entry) return

    // only mark entry as read if we're expanding
    if (!entry.expanded) {
        thunkApi.dispatch(markEntry({ entry, read: true }))
    }

    // set entry as selected
    thunkApi.dispatch(entriesSlice.actions.setSelectedEntry(entry))

    // collapse or expand entry if needed
    const previouslySelectedEntry = state.entries.entries.find(e => e.id === state.entries.selectedEntryId)
    if (entry === previouslySelectedEntry) {
        // selecting an entry already selected toggles expanded status
        thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry, expanded: !entry.expanded }))
    } else {
        if (previouslySelectedEntry) {
            thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry: previouslySelectedEntry, expanded: false }))
        }
        thunkApi.dispatch(entriesSlice.actions.setEntryExpanded({ entry, expanded: true }))
    }
})
export const selectPreviousEntry = createAsyncThunk<void, void, { state: RootState }>("entries/entry/selectPrevious", (_, thunkApi) => {
    const state = thunkApi.getState()
    const { entries } = state.entries
    const previousIndex = entries.findIndex(e => e.id === state.entries.selectedEntryId) - 1
    if (previousIndex >= 0) {
        thunkApi.dispatch(selectEntry(entries[previousIndex]))
    }
})
export const selectNextEntry = createAsyncThunk<void, void, { state: RootState }>("entries/entry/selectNext", (_, thunkApi) => {
    const state = thunkApi.getState()
    const { entries } = state.entries
    const nextIndex = entries.findIndex(e => e.id === state.entries.selectedEntryId) + 1
    if (nextIndex < entries.length) {
        thunkApi.dispatch(selectEntry(entries[nextIndex]))
    }
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
            state.source = action.meta.arg
            state.entries = []
            state.timestamp = undefined
            state.sourceLabel = ""
            state.sourceWebsiteUrl = ""
            state.hasMore = true
            state.selectedEntryId = undefined
        })
        builder.addCase(loadEntries.fulfilled, (state, action) => {
            state.entries = action.payload.entries
            state.timestamp = action.payload.timestamp
            state.sourceLabel = action.payload.name
            state.sourceWebsiteUrl = action.payload.feedLink
            state.hasMore = action.payload.hasMore
        })
        builder.addCase(loadMoreEntries.fulfilled, (state, action) => {
            // remove already existing entries
            const entriesToAdd = action.payload.entries.filter(e => !state.entries.some(e2 => e.id === e2.id))
            state.entries = [...state.entries, ...entriesToAdd]
            state.hasMore = action.payload.hasMore
        })
    },
})

export default entriesSlice.reducer
