import { t } from "@lingui/core/macro"
import { showNotification } from "@mantine/notifications"
import { createSlice, type PayloadAction } from "@reduxjs/toolkit"
import type { LocalSettings, Settings, UserModel, ViewMode } from "@/app/types"
import { changeSettings, reloadProfile, reloadSettings, reloadTags } from "./thunks"

interface UserState {
    settings?: Settings
    localSettings: LocalSettings
    profile?: UserModel
    tags?: string[]
}

export const initialLocalSettings: LocalSettings = {
    viewMode: "detailed",
    sidebarWidth: 360,
    announcementHash: "no-hash",
    fontSizePercentage: 100,
}

const initialState: UserState = {
    localSettings: initialLocalSettings,
}

export const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {
        setViewMode: (state, action: PayloadAction<ViewMode>) => {
            state.localSettings.viewMode = action.payload
        },
        setFontSizePercentage: (state, action: PayloadAction<number>) => {
            state.localSettings.fontSizePercentage = action.payload
        },
        setSidebarWidth: (state, action: PayloadAction<number>) => {
            state.localSettings.sidebarWidth = action.payload
        },
        setAnnouncementHash: (state, action: PayloadAction<string>) => {
            state.localSettings.announcementHash = action.payload
        },
    },
    extraReducers: builder => {
        builder.addCase(reloadSettings.fulfilled, (state, action) => {
            state.settings = action.payload
        })
        builder.addCase(reloadProfile.fulfilled, (state, action) => {
            state.profile = action.payload
        })
        builder.addCase(reloadTags.fulfilled, (state, action) => {
            state.tags = action.payload
        })
        builder.addCase(changeSettings.pending, (state, action) => {
            if (!state.settings) return
            state.settings = { ...state.settings, ...action.meta.arg }
        })
        builder.addCase(changeSettings.fulfilled, () => {
            showNotification({
                message: t`Settings saved.`,
                color: "green",
            })
        })
    },
})

export const { setViewMode, setSidebarWidth, setAnnouncementHash, setFontSizePercentage } = userSlice.actions
