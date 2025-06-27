import { createAppAsyncThunk } from "@/app/async-thunk"
import { client } from "@/app/client"

export const reloadServerInfos = createAppAsyncThunk("server/infos", async () => await client.server.getServerInfos().then(r => r.data))
