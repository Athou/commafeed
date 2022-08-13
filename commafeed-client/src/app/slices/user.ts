import { t } from "@lingui/macro"
import { showNotification } from "@mantine/notifications"
import { createAsyncThunk, createSlice, isAnyOf } from "@reduxjs/toolkit"
import { client } from "app/client"
import { RootState } from "app/store"
import { ReadingMode, ReadingOrder, Settings, UserModel } from "app/types"

interface UserState {
    settings?: Settings
    profile?: UserModel
}

const initialState: UserState = {}

export const reloadSettings = createAsyncThunk("settings/reload", () => client.user.getSettings().then(r => r.data))
export const reloadProfile = createAsyncThunk("profile/reload", () => client.user.getProfile().then(r => r.data))
export const changeReadingMode = createAsyncThunk<void, ReadingMode, { state: RootState }>(
    "settings/readingMode",
    (readingMode, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, readingMode })
    }
)
export const changeReadingOrder = createAsyncThunk<void, ReadingOrder, { state: RootState }>(
    "settings/readingOrder",
    (readingOrder, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, readingOrder })
    }
)
export const changeLanguage = createAsyncThunk<void, string, { state: RootState }>("settings/language", (language, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, language })
})
export const changeScrollSpeed = createAsyncThunk<void, boolean, { state: RootState }>("settings/scrollSpeed", (speed, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, scrollSpeed: speed ? 400 : 0 })
})

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
        builder.addMatcher(isAnyOf(changeLanguage.fulfilled, changeScrollSpeed.fulfilled), () => {
            showNotification({
                message: t`Settings saved.`,
                color: "green",
            })
        })
    },
})

export default userSlice.reducer
