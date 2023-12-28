import { t } from "@lingui/macro"
import { showNotification } from "@mantine/notifications"
import { createSlice, isAnyOf } from "@reduxjs/toolkit"
import { client } from "app/client"
import { createAppAsyncThunk } from "app/thunk"
import { type ReadingMode, type ReadingOrder, type Settings, type SharingSettings, type UserModel } from "app/types"
// eslint-disable-next-line import/no-cycle
import { reloadEntries } from "./entries"

interface UserState {
    settings?: Settings
    profile?: UserModel
    tags?: string[]
}

const initialState: UserState = {}

export const reloadSettings = createAppAsyncThunk("settings/reload", async () => await client.user.getSettings().then(r => r.data))
export const reloadProfile = createAppAsyncThunk("profile/reload", async () => await client.user.getProfile().then(r => r.data))
export const reloadTags = createAppAsyncThunk("entries/tags", async () => await client.entry.getTags().then(r => r.data))
export const changeReadingMode = createAppAsyncThunk("settings/readingMode", (readingMode: ReadingMode, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, readingMode })
    thunkApi.dispatch(reloadEntries())
})
export const changeReadingOrder = createAppAsyncThunk("settings/readingOrder", (readingOrder: ReadingOrder, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, readingOrder })
    thunkApi.dispatch(reloadEntries())
})
export const changeLanguage = createAppAsyncThunk("settings/language", (language: string, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, language })
})
export const changeScrollSpeed = createAppAsyncThunk("settings/scrollSpeed", (speed: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, scrollSpeed: speed ? 400 : 0 })
})
export const changeShowRead = createAppAsyncThunk("settings/showRead", (showRead: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, showRead })
})
export const changeScrollMarks = createAppAsyncThunk("settings/scrollMarks", (scrollMarks: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, scrollMarks })
})
export const changeAlwaysScrollToEntry = createAppAsyncThunk("settings/alwaysScrollToEntry", (alwaysScrollToEntry: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, alwaysScrollToEntry })
})
export const changeMarkAllAsReadConfirmation = createAppAsyncThunk(
    "settings/markAllAsReadConfirmation",
    (markAllAsReadConfirmation: boolean, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, markAllAsReadConfirmation })
    }
)
export const changeCustomContextMenu = createAppAsyncThunk("settings/customContextMenu", (customContextMenu: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, customContextMenu })
})
export const changeSharingSetting = createAppAsyncThunk(
    "settings/sharingSetting",
    (
        sharingSetting: {
            site: keyof SharingSettings
            value: boolean
        },
        thunkApi
    ) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({
            ...settings,
            sharingSettings: {
                ...settings.sharingSettings,
                [sharingSetting.site]: sharingSetting.value,
            },
        })
    }
)

export const userSlice = createSlice({
    name: "user",
    initialState,
    reducers: {},
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
        builder.addCase(changeReadingMode.pending, (state, action) => {
            if (!state.settings) return
            state.settings.readingMode = action.meta.arg
        })
        builder.addCase(changeReadingOrder.pending, (state, action) => {
            if (!state.settings) return
            state.settings.readingOrder = action.meta.arg
        })
        builder.addCase(changeLanguage.pending, (state, action) => {
            if (!state.settings) return
            state.settings.language = action.meta.arg
        })
        builder.addCase(changeScrollSpeed.pending, (state, action) => {
            if (!state.settings) return
            state.settings.scrollSpeed = action.meta.arg ? 400 : 0
        })
        builder.addCase(changeShowRead.pending, (state, action) => {
            if (!state.settings) return
            state.settings.showRead = action.meta.arg
        })
        builder.addCase(changeScrollMarks.pending, (state, action) => {
            if (!state.settings) return
            state.settings.scrollMarks = action.meta.arg
        })
        builder.addCase(changeAlwaysScrollToEntry.pending, (state, action) => {
            if (!state.settings) return
            state.settings.alwaysScrollToEntry = action.meta.arg
        })
        builder.addCase(changeMarkAllAsReadConfirmation.pending, (state, action) => {
            if (!state.settings) return
            state.settings.markAllAsReadConfirmation = action.meta.arg
        })
        builder.addCase(changeCustomContextMenu.pending, (state, action) => {
            if (!state.settings) return
            state.settings.customContextMenu = action.meta.arg
        })
        builder.addCase(changeSharingSetting.pending, (state, action) => {
            if (!state.settings) return
            state.settings.sharingSettings[action.meta.arg.site] = action.meta.arg.value
        })
        builder.addMatcher(
            isAnyOf(
                changeLanguage.fulfilled,
                changeScrollSpeed.fulfilled,
                changeShowRead.fulfilled,
                changeScrollMarks.fulfilled,
                changeAlwaysScrollToEntry.fulfilled,
                changeMarkAllAsReadConfirmation.fulfilled,
                changeCustomContextMenu.fulfilled,
                changeSharingSetting.fulfilled
            ),
            () => {
                showNotification({
                    message: t`Settings saved.`,
                    color: "green",
                })
            }
        )
    },
})

export default userSlice.reducer
