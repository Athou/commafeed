import { type PayloadAction, createSlice } from "@reduxjs/toolkit"
import { markEntry } from "app/entries/thunks"
import { redirectTo } from "app/redirect/slice"
import { collapseTreeCategory, reloadTree } from "app/tree/thunks"
import type { Category } from "app/types"
import { visitCategoryTree } from "app/utils"

interface TreeState {
    rootCategory?: Category
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
                }
            })
        },
    },
    extraReducers: builder => {
        builder.addCase(reloadTree.fulfilled, (state, action) => {
            visitCategoryTree(action.payload, category => {
            category.feeds = category.feeds.map(feed => {
            const storageKey = `feed-${feed.id}-unread`
            const prevUnread = parseInt(localStorage.getItem(storageKey) || "0", 10)
            const hasNewEntries = feed.unread > prevUnread

            localStorage.setItem(storageKey, feed.unread.toString())

            return {
                ...feed,
                hasNewEntries
            }
            })
        })
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
        builder.addCase(redirectTo, state => {
            state.mobileMenuOpen = false
        })
    },
})

export const { setMobileMenuOpen, toggleSidebar, incrementUnreadCount } = treeSlice.actions
