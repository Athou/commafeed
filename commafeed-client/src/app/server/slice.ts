import { createSlice, type PayloadAction } from "@reduxjs/toolkit"
import { reloadServerInfos } from "@/app/server/thunks"
import type { ServerInfo } from "@/app/types"

interface ServerState {
    serverInfos?: ServerInfo
    webSocketConnected: boolean
}

const initialState: ServerState = {
    webSocketConnected: false,
}

export const serverSlice = createSlice({
    name: "server",
    initialState,
    reducers: {
        setWebSocketConnected: (state, action: PayloadAction<boolean>) => {
            state.webSocketConnected = action.payload
        },
    },
    extraReducers: builder => {
        builder.addCase(reloadServerInfos.fulfilled, (state, action) => {
            state.serverInfos = action.payload
        })
    },
})

export const { setWebSocketConnected } = serverSlice.actions
