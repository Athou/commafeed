import { createAppAsyncThunk } from "app/async-thunk"
import { client } from "app/client"
import type { CollapseRequest } from "app/types"

export const reloadTree = createAppAsyncThunk("tree/reload", async () => await client.category.getRoot().then(r => r.data))
export const collapseTreeCategory = createAppAsyncThunk(
    "tree/category/collapse",
    async (req: CollapseRequest) => await client.category.collapse(req)
)
