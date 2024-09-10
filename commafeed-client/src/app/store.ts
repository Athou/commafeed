import { configureStore } from "@reduxjs/toolkit"
import { entriesSlice } from "app/entries/slice"
import { redirectSlice } from "app/redirect/slice"
import { serverSlice } from "app/server/slice"
import { treeSlice } from "app/tree/slice"
import type { ViewMode } from "app/types"
import { userSlice } from "app/user/slice"
import { type TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"

export const reducers = {
    entries: entriesSlice.reducer,
    redirect: redirectSlice.reducer,
    tree: treeSlice.reducer,
    server: serverSlice.reducer,
    user: userSlice.reducer,
}

export const store = configureStore({
    reducer: reducers,
    preloadedState: {
        user: {
            localSettings: {
                viewMode: localStorage.getItem("view-mode") as ViewMode,
            },
        },
    },
})
store.subscribe(() => {
    const state = store.getState()
    localStorage.setItem("view-mode", state.user.localSettings.viewMode)
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
