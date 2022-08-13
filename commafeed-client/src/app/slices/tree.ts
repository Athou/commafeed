import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit"
import { client } from "app/client"
import { Category, CollapseRequest } from "app/types"
import { visitCategoryTree } from "app/utils"
import { markAllEntries, markEntry } from "./entries"
import { redirectTo } from "./redirect"

interface TreeState {
    rootCategory?: Category
    mobileMenuOpen: boolean
}

const initialState: TreeState = {
    mobileMenuOpen: false,
}

export const reloadTree = createAsyncThunk("tree/reload", () => client.category.getRoot().then(r => r.data))
export const collapseTreeCategory = createAsyncThunk("tree/category/collapse", async (req: CollapseRequest) =>
    client.category.collapse(req)
)

export const treeSlice = createSlice({
    name: "tree",
    initialState,
    reducers: {
        setMobileMenuOpen: (state, action: PayloadAction<boolean>) => {
            state.mobileMenuOpen = action.payload
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
        builder.addCase(markAllEntries.pending, (state, action) => {
            if (!state.rootCategory) return
            const { sourceType } = action.meta.arg
            const sourceId = action.meta.arg.req.id
            visitCategoryTree(state.rootCategory, c => {
                if (sourceType === "category" && c.id === sourceId) {
                    visitCategoryTree(c, c2 =>
                        c2.feeds.forEach(f => {
                            f.unread = 0
                        })
                    )
                } else if (sourceType === "feed") {
                    c.feeds
                        .filter(f => f.id === +sourceId)
                        .forEach(f => {
                            f.unread = 0
                        })
                }
            })
        })
        builder.addCase(redirectTo, state => {
            state.mobileMenuOpen = false
        })
    },
})

export const { setMobileMenuOpen } = treeSlice.actions
export default treeSlice.reducer
