import { createSlice, type PayloadAction } from "@reduxjs/toolkit"
import { Constants } from "@/app/constants"
import { loadEntries, loadMoreEntries, markAllEntries, markEntry, markMultipleEntries, starEntry, tagEntry } from "@/app/entries/thunks"
import type { Entry } from "@/app/types"

export type EntrySourceType = "category" | "feed" | "tag"

export interface EntrySource {
    type: EntrySourceType
    id: string
}

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
    markAllAsReadConfirmationDialogOpen: boolean
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
    markAllAsReadConfirmationDialogOpen: false,
}

export const entriesSlice = createSlice({
    name: "entries",
    initialState,
    reducers: {
        setSelectedEntry: (state, action: PayloadAction<Entry>) => {
            state.selectedEntryId = action.payload.id
        },
        setEntryExpanded: (state, action: PayloadAction<{ entry: Entry; expanded: boolean }>) => {
            for (const e of state.entries.filter(e => e.id === action.payload.entry.id)) {
                e.expanded = action.payload.expanded
            }
        },
        setScrollingToEntry: (state, action: PayloadAction<boolean>) => {
            state.scrollingToEntry = action.payload
        },
        setSearch: (state, action: PayloadAction<string>) => {
            state.search = action.payload
        },
        setMarkAllAsReadConfirmationDialogOpen: (state, action: PayloadAction<boolean>) => {
            state.markAllAsReadConfirmationDialogOpen = action.payload
        },
    },
    extraReducers: builder => {
        builder.addCase(markEntry.pending, (state, action) => {
            for (const e of state.entries.filter(e => e.id === action.meta.arg.entry.id)) {
                e.read = action.meta.arg.read
            }
        })
        builder.addCase(markMultipleEntries.pending, (state, action) => {
            for (const e of state.entries.filter(e => action.meta.arg.entries.some(e2 => e2.id === e.id))) {
                e.read = action.meta.arg.read
            }
        })
        builder.addCase(markAllEntries.pending, (state, action) => {
            for (const e of state.entries.filter(e => (action.meta.arg.req.olderThan ? e.date < action.meta.arg.req.olderThan : true))) {
                e.read = true
            }
        })
        builder.addCase(starEntry.pending, (state, action) => {
            for (const e of state.entries.filter(e => action.meta.arg.entry.id === e.id && action.meta.arg.entry.feedId === e.feedId)) {
                e.starred = action.meta.arg.starred
            }
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
            for (const e of state.entries.filter(e => +e.id === action.meta.arg.entryId)) {
                e.tags = action.meta.arg.tags
            }
        })
    },
})

export const { setSearch, setMarkAllAsReadConfirmationDialogOpen } = entriesSlice.actions
