import { createSlice, type PayloadAction } from "@reduxjs/toolkit"
import { markEntry } from "app/entries/thunks"
import { redirectTo } from "app/redirect/slice"
import { collapseTreeCategory, reloadTree } from "app/tree/thunks"
import { type Category } from "app/types"
import { visitCategoryTree } from "app/utils"

interface TreeState {
    rootCategory?: Category
    mobileMenuOpen: boolean
    sidebarWidth: number
    sidebarVisible: boolean
}

const initialState: TreeState = {
    mobileMenuOpen: false,
    sidebarWidth: 350,
    sidebarVisible: true,
}

export const treeSlice = createSlice({
    name: "tree",
    initialState,
    reducers: {
        setMobileMenuOpen: (state, action: PayloadAction<boolean>) => {
            state.mobileMenuOpen = action.payload
        },
        setSidebarWidth: (state, action: PayloadAction<number>) => {
            state.sidebarWidth = action.payload
        },
        toggleSidebar: state => {
            state.sidebarVisible = !state.sidebarVisible
        },
    },
    extraReducers: builder => {
        builder.addCase(reloadTree.fulfilled, (state, action) => {
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
            visitCategoryTree(state.rootCategory, c =>
                c.feeds
                    .filter(f => f.id === +action.meta.arg.entry.feedId)
                    .forEach(f => {
                        f.unread = action.meta.arg.read ? f.unread - 1 : f.unread + 1
                    })
            )
        })
        builder.addCase(redirectTo, state => {
            state.mobileMenuOpen = false
        })
    },
})

export const { setMobileMenuOpen, setSidebarWidth, toggleSidebar } = treeSlice.actions
