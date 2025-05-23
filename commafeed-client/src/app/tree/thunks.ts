import { createAppAsyncThunk } from "app/async-thunk"
import { client } from "app/client"
import { redirectToCategory, redirectToFeed } from "app/redirect/thunks"
import { incrementUnreadCount } from "app/tree/slice"
import type { CollapseRequest, Subscription } from "app/types"
import { flattenCategoryTree, visitCategoryTree } from "app/utils"

export const reloadTree = createAppAsyncThunk("tree/reload", async () => await client.category.getRoot().then(r => r.data))

export const collapseTreeCategory = createAppAsyncThunk(
    "tree/category/collapse",
    async (req: CollapseRequest) => await client.category.collapse(req).then(r => r.data)
)

export const selectNextUnreadTreeItem = createAppAsyncThunk(
    "tree/selectNextUnreadItem",
    (
        arg: {
            direction: "forward" | "backward"
        },
        thunkApi
    ) => {
        const state = thunkApi.getState()
        const root = state.tree.rootCategory
        if (!root) return

        const { source } = state.entries
        if (source.type === "category") {
            const categories = flattenCategoryTree(root)
            if (arg.direction === "backward") categories.reverse()

            const index = categories.findIndex(c => c.id === source.id)
            if (index === -1) return

            for (let i = index + 1; i < categories.length; i++) {
                const c = categories[i]
                if (c.feeds.some(f => f.unread > 0)) {
                    return thunkApi.dispatch(redirectToCategory(String(c.id)))
                }
            }
        } else if (source.type === "feed") {
            const feeds: Subscription[] = []
            visitCategoryTree(root, c => feeds.push(...c.feeds), { childrenFirst: true })
            if (arg.direction === "backward") feeds.reverse()

            const index = feeds.findIndex(f => f.id === +source.id)
            if (index === -1) return

            for (let i = index + 1; i < feeds.length; i++) {
                const f = feeds[i]
                if (f.unread > 0) {
                    return thunkApi.dispatch(redirectToFeed(String(f.id)))
                }
            }
        }
    }
)

export const newFeedEntriesDiscovered = createAppAsyncThunk(
    "tree/new-feed-entries-discovered",
    async ({ feedId, amount }: { feedId: number; amount: number }, thunkApi) => {
        const root = thunkApi.getState().tree.rootCategory
        if (!root) return

        const feed = flattenCategoryTree(root)
            .flatMap(c => c.feeds)
            .some(f => f.id === feedId)
        if (!feed) {
            // feed not found in the tree, reload the tree completely
            thunkApi.dispatch(reloadTree())
        } else {
            thunkApi.dispatch(
                incrementUnreadCount({
                    feedId,
                    amount,
                })
            )
        }
    }
)
