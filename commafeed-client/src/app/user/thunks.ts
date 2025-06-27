import { createAppAsyncThunk } from "@/app/async-thunk"
import { client } from "@/app/client"
import { reloadEntries } from "@/app/entries/thunks"
import type { IconDisplayMode, ReadingMode, ReadingOrder, ScrollMode, SharingSettings } from "@/app/types"

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

export const changeScrollMode = createAppAsyncThunk("settings/scrollMode", (scrollMode: ScrollMode, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, scrollMode })
})

export const changeEntriesToKeepOnTopWhenScrolling = createAppAsyncThunk(
    "settings/entriesToKeepOnTopWhenScrolling",
    (entriesToKeepOnTopWhenScrolling: number, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, entriesToKeepOnTopWhenScrolling })
    }
)

export const changeStarIconDisplayMode = createAppAsyncThunk(
    "settings/starIconDisplayMode",
    (starIconDisplayMode: IconDisplayMode, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, starIconDisplayMode })
    }
)

export const changeExternalLinkIconDisplayMode = createAppAsyncThunk(
    "settings/externalLinkIconDisplayMode",
    (externalLinkIconDisplayMode: IconDisplayMode, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, externalLinkIconDisplayMode })
    }
)

export const changeMarkAllAsReadConfirmation = createAppAsyncThunk(
    "settings/markAllAsReadConfirmation",
    (markAllAsReadConfirmation: boolean, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, markAllAsReadConfirmation })
    }
)

export const changeMarkAllAsReadNavigateToUnread = createAppAsyncThunk(
    "settings/markAllAsReadNavigateToUnread",
    (markAllAsReadNavigateToNextUnread: boolean, thunkApi) => {
        const { settings } = thunkApi.getState().user
        if (!settings) return
        client.user.saveSettings({ ...settings, markAllAsReadNavigateToNextUnread })
    }
)

export const changeCustomContextMenu = createAppAsyncThunk("settings/customContextMenu", (customContextMenu: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, customContextMenu })
})

export const changeMobileFooter = createAppAsyncThunk("settings/mobileFooter", (mobileFooter: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, mobileFooter })
})

export const changeUnreadCountTitle = createAppAsyncThunk("settings/unreadCountTitle", (unreadCountTitle: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, unreadCountTitle })
})

export const changeUnreadCountFavicon = createAppAsyncThunk("settings/unreadCountFavicon", (unreadCountFavicon: boolean, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, unreadCountFavicon })
})

export const changePrimaryColor = createAppAsyncThunk("settings/primaryColor", (primaryColor: string, thunkApi) => {
    const { settings } = thunkApi.getState().user
    if (!settings) return
    client.user.saveSettings({ ...settings, primaryColor })
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
