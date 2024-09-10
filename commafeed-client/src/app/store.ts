import { configureStore } from "@reduxjs/toolkit"
import { entriesSlice } from "app/entries/slice"
import { redirectSlice } from "app/redirect/slice"
import { serverSlice } from "app/server/slice"
import { treeSlice } from "app/tree/slice"
import type { LocalSettings } from "app/types"
import { initialLocalSettings, userSlice } from "app/user/slice"
import { type TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"

export const reducers = {
    entries: entriesSlice.reducer,
    redirect: redirectSlice.reducer,
    tree: treeSlice.reducer,
    server: serverSlice.reducer,
    user: userSlice.reducer,
}

const loadLocalSettings = (): LocalSettings => {
    const json = localStorage.getItem("commafeed-local-settings")
    if (json) {
        return JSON.parse(json)
    }

    // load old settings
    const viewMode = localStorage.getItem("view-mode")
    const sidebarWidth = localStorage.getItem("sidebar-width")
    const announcementHash = localStorage.getItem("announcement-hash")
    return {
        ...initialLocalSettings,
        viewMode: viewMode ? JSON.parse(viewMode) : initialLocalSettings.viewMode,
        sidebarWidth: sidebarWidth ? JSON.parse(sidebarWidth) : initialLocalSettings.sidebarWidth,
        announcementHash: announcementHash ? JSON.parse(announcementHash) : initialLocalSettings.announcementHash,
    }
}

export const store = configureStore({
    reducer: reducers,
    preloadedState: {
        user: {
            localSettings: loadLocalSettings(),
        },
    },
})
store.subscribe(() => {
    const localSettings = store.getState().user.localSettings
    localStorage.setItem("commafeed-local-settings", JSON.stringify(localSettings))
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

export const useAppDispatch: () => AppDispatch = useDispatch
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
