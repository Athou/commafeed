import { createAppAsyncThunk } from "@/app/async-thunk"
import { client } from "@/app/client"
import type { Settings } from "@/app/types"

export const reloadSettings = createAppAsyncThunk("settings/reload", async () => await client.user.getSettings().then(r => r.data))

export const reloadProfile = createAppAsyncThunk("profile/reload", async () => await client.user.getProfile().then(r => r.data))

export const reloadTags = createAppAsyncThunk("entries/tags", async () => await client.entry.getTags().then(r => r.data))

export const changeSettings = createAppAsyncThunk("settings/change", async (newSettings: Partial<Settings>, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    await client.user.saveSettings({ ...settings, ...newSettings })
})
