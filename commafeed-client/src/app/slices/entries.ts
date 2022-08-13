import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit"
import { client } from "app/client"
import { Constants } from "app/constants"
import { RootState } from "app/store"
import { Entries, Entry, GetEntriesRequest, MarkRequest } from "app/types"

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
        id: Constants.categoryIds.all,
    },
    sourceLabel: "",
    sourceWebsiteUrl: "",
    entries: [],
    hasMore: true,
}

const getEndpoint = (sourceType: EntrySourceType) => (sourceType === "category" ? client.category.getEntries : client.feed.getEntries)
export const loadEntries = createAsyncThunk<Entries, { req: GetEntriesRequest; sourceType: EntrySourceType }, { state: RootState }>(
    "entries/load",
    async arg => {
        const endpoint = getEndpoint(arg.sourceType)
        const result = await endpoint({
            ...arg.req,
            offset: 0,
            limit: 50,
        })
        return result.data
    }
)
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
export const reloadEntries = createAsyncThunk<Entries, void, { state: RootState }>("entries/reload", async (_, thunkApi) => {
    const state = thunkApi.getState()
    const endpoint = getEndpoint(state.entries.source.type)
    const result = await endpoint({
        id: state.entries.source.id,
        readType: state.user.settings?.readingMode,
        order: state.user.settings?.readingOrder,
        offset: 0,
        limit: 50,
    })
    return result.data
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
export const markAllEntries = createAsyncThunk("entries/entry/markAll", (arg: { sourceType: EntrySourceType; req: MarkRequest }) => {
    const endpoint = arg.sourceType === "category" ? client.category.markEntries : client.feed.markEntries
    endpoint(arg.req)
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
        builder.addCase(markAllEntries.pending, state => {
            state.entries.forEach(e => {
                e.read = true
            })
        })
        builder.addCase(loadEntries.pending, (state, action) => {
            state.source = {
                type: action.meta.arg.sourceType,
                id: action.meta.arg.req.id,
            }
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
        builder.addCase(reloadEntries.pending, state => {
            state.entries = []
            state.timestamp = undefined
            state.sourceLabel = ""
            state.sourceWebsiteUrl = ""
            state.hasMore = true
            state.selectedEntryId = undefined
        })
        builder.addCase(reloadEntries.fulfilled, (state, action) => {
            state.entries = action.payload.entries
            state.timestamp = action.payload.timestamp
            state.sourceLabel = action.payload.name
            state.sourceWebsiteUrl = action.payload.feedLink
            state.hasMore = action.payload.hasMore
        })
    },
})

export default entriesSlice.reducer
