import { createAppAsyncThunk } from "app/async-thunk"
import { client } from "app/client"
import { incrementUnreadCount } from "app/tree/slice"
import type { CollapseRequest } from "app/types"
import { flattenCategoryTree } from "app/utils"

export const reloadTree = createAppAsyncThunk("tree/reload", async () => await client.category.getRoot().then(r => r.data))

export const collapseTreeCategory = createAppAsyncThunk(
    "tree/category/collapse",
    async (req: CollapseRequest) => await client.category.collapse(req)
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
