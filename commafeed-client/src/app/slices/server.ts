import { createAsyncThunk, createSlice } from "@reduxjs/toolkit"
import { client } from "app/client"
import { ServerInfo } from "app/types"

interface ServerState {
    serverInfos?: ServerInfo
}

const initialState: ServerState = {}

export const reloadServerInfos = createAsyncThunk("server/infos", () => client.server.getServerInfos().then(r => r.data))
export const serverSlice = createSlice({
    name: "server",
    initialState,
    reducers: {},
    extraReducers: builder => {
        builder.addCase(reloadServerInfos.fulfilled, (state, action) => {
            state.serverInfos = action.payload
        })
    },
})

export default serverSlice.reducer
