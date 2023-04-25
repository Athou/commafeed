import { configureStore } from "@reduxjs/toolkit"
import { setupListeners } from "@reduxjs/toolkit/query"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"
import entriesReducer from "./slices/entries"
import redirectReducer from "./slices/redirect"
import serverReducer from "./slices/server"
import treeReducer from "./slices/tree"
import userReducer from "./slices/user"

export const reducers = {
    entries: entriesReducer,
    redirect: redirectReducer,
    tree: treeReducer,
    server: serverReducer,
    user: userReducer,
}

export const store = configureStore({ reducer: reducers })

setupListeners(store.dispatch)

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
