import { type PayloadAction, createSlice } from "@reduxjs/toolkit"
import { loadEntries, markEntry } from "app/entries/thunks"
import { redirectTo } from "app/redirect/slice"
import { collapseTreeCategory, reloadTree } from "app/tree/thunks"
import type { Category, Subscription } from "app/types"
import { flattenCategoryTree, visitCategoryTree } from "app/utils"

export interface TreeSubscription extends Subscription {
    // client-side only flag
    hasNewEntries?: boolean
}

export interface TreeCategory extends Category {
    feeds: TreeSubscription[]
    children: TreeCategory[]
}

interface TreeState {
    rootCategory?: TreeCategory
    mobileMenuOpen: boolean
    sidebarVisible: boolean
}

const initialState: TreeState = {
    mobileMenuOpen: false,
    sidebarVisible: true,
}

export const treeSlice = createSlice({
    name: "tree",
    initialState,
    reducers: {
        setMobileMenuOpen: (state, action: PayloadAction<boolean>) => {
            state.mobileMenuOpen = action.payload
        },
        toggleSidebar: state => {
            state.sidebarVisible = !state.sidebarVisible
        },
        incrementUnreadCount: (
            state,
            action: PayloadAction<{
                feedId: number
                amount: number
            }>
        ) => {
            if (!state.rootCategory) return
            visitCategoryTree(state.rootCategory, c => {
                for (const f of c.feeds.filter(f => f.id === action.payload.feedId)) {
                    f.unread += action.payload.amount
                    f.hasNewEntries = true
                }
            })
        },
    },
    extraReducers: builder => {
        builder.addCase(reloadTree.fulfilled, (state, action) => {
            // set hasNewEntries to true if new unread > previous unread
            if (state.rootCategory) {
                const oldFeeds = flattenCategoryTree(state.rootCategory).flatMap(c => c.feeds)
                const oldFeedsById = new Map(oldFeeds.map(f => [f.id, f]))

                const newFeeds = flattenCategoryTree(action.payload).flatMap(c => c.feeds)
                for (const newFeed of newFeeds) {
                    const oldFeed = oldFeedsById.get(newFeed.id)
                    if (oldFeed && newFeed.unread > oldFeed.unread) {
                        newFeed.hasNewEntries = true
                    }
                }
            }

            state.rootCategory = action.payload
        })
        builder.addCase(collapseTreeCategory.pending, (state, action) => {
            if (!state.rootCategory) return
            visitCategoryTree(state.rootCategory, c => {
                if (+c.id === action.meta.arg.id) c.expanded = !action.meta.arg.collapse
            })
        })
        builder.addCase(markEntry.pending, (state, action) => {
            if (!state.rootCategory) return
            visitCategoryTree(state.rootCategory, c => {
                for (const f of c.feeds.filter(f => f.id === +action.meta.arg.entry.feedId)) {
                    f.unread = action.meta.arg.read ? f.unread - 1 : f.unread + 1
                }
            })
        })
        builder.addCase(loadEntries.pending, (state, action) => {
            if (!state.rootCategory) return

            const { source } = action.meta.arg
            if (source.type === "category") {
                visitCategoryTree(state.rootCategory, c => {
                    if (c.id === source.id) {
                        for (const f of flattenCategoryTree(c).flatMap(c => c.feeds)) {
                            f.hasNewEntries = false
                        }
                    }
                })
            } else if (source.type === "feed") {
                const feeds = flattenCategoryTree(state.rootCategory).flatMap(c => c.feeds)
                for (const f of feeds.filter(f => f.id === +source.id)) {
                    f.hasNewEntries = false
                }
            }
        })
        builder.addCase(redirectTo, state => {
            state.mobileMenuOpen = false
        })
    },
})

export const { setMobileMenuOpen, toggleSidebar, incrementUnreadCount } = treeSlice.actions
