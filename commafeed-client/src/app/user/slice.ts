import { t } from "@lingui/core/macro"
import { showNotification } from "@mantine/notifications"
import { createSlice, isAnyOf, type PayloadAction } from "@reduxjs/toolkit"
import type { LocalSettings, Settings, UserModel, ViewMode } from "@/app/types"
import {
    changeCustomContextMenu,
    changeDisablePullToRefresh,
    changeEntriesToKeepOnTopWhenScrolling,
    changeExternalLinkIconDisplayMode,
    changeLanguage,
    changeMarkAllAsReadConfirmation,
    changeMarkAllAsReadNavigateToUnread,
    changeMobileFooter,
    changePrimaryColor,
    changePushNotificationSettings,
    changeReadingMode,
    changeReadingOrder,
    changeScrollMarks,
    changeScrollMode,
    changeScrollSpeed,
    changeSharingSetting,
    changeShowRead,
    changeStarIconDisplayMode,
    changeUnreadCountFavicon,
    changeUnreadCountTitle,
    reloadProfile,
    reloadSettings,
    reloadTags,
} from "./thunks"

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
        builder.addCase(changeScrollMode.pending, (state, action) => {
            if (!state.settings) return
            state.settings.scrollMode = action.meta.arg
        })
        builder.addCase(changeEntriesToKeepOnTopWhenScrolling.pending, (state, action) => {
            if (!state.settings) return
            state.settings.entriesToKeepOnTopWhenScrolling = action.meta.arg
        })
        builder.addCase(changeStarIconDisplayMode.pending, (state, action) => {
            if (!state.settings) return
            state.settings.starIconDisplayMode = action.meta.arg
        })
        builder.addCase(changeExternalLinkIconDisplayMode.pending, (state, action) => {
            if (!state.settings) return
            state.settings.externalLinkIconDisplayMode = action.meta.arg
        })
        builder.addCase(changeMarkAllAsReadConfirmation.pending, (state, action) => {
            if (!state.settings) return
            state.settings.markAllAsReadConfirmation = action.meta.arg
        })
        builder.addCase(changeMarkAllAsReadNavigateToUnread.pending, (state, action) => {
            if (!state.settings) return
            state.settings.markAllAsReadNavigateToNextUnread = action.meta.arg
        })
        builder.addCase(changeCustomContextMenu.pending, (state, action) => {
            if (!state.settings) return
            state.settings.customContextMenu = action.meta.arg
        })
        builder.addCase(changeMobileFooter.pending, (state, action) => {
            if (!state.settings) return
            state.settings.mobileFooter = action.meta.arg
        })
        builder.addCase(changeUnreadCountTitle.pending, (state, action) => {
            if (!state.settings) return
            state.settings.unreadCountTitle = action.meta.arg
        })
        builder.addCase(changeUnreadCountFavicon.pending, (state, action) => {
            if (!state.settings) return
            state.settings.unreadCountFavicon = action.meta.arg
        })
        builder.addCase(changeDisablePullToRefresh.pending, (state, action) => {
            if (!state.settings) return
            state.settings.disablePullToRefresh = action.meta.arg
        })
        builder.addCase(changePrimaryColor.pending, (state, action) => {
            if (!state.settings) return
            state.settings.primaryColor = action.meta.arg
        })
        builder.addCase(changeSharingSetting.pending, (state, action) => {
            if (!state.settings) return
            state.settings.sharingSettings[action.meta.arg.site] = action.meta.arg.value
        })
        builder.addCase(changePushNotificationSettings.pending, (state, action) => {
            if (!state.settings) return
            state.settings.pushNotificationSettings = action.meta.arg
        })

        builder.addMatcher(
            isAnyOf(
                changeLanguage.fulfilled,
                changeScrollSpeed.fulfilled,
                changeShowRead.fulfilled,
                changeScrollMarks.fulfilled,
                changeScrollMode.fulfilled,
                changeEntriesToKeepOnTopWhenScrolling.fulfilled,
                changeStarIconDisplayMode.fulfilled,
                changeExternalLinkIconDisplayMode.fulfilled,
                changeMarkAllAsReadConfirmation.fulfilled,
                changeMarkAllAsReadNavigateToUnread.fulfilled,
                changeCustomContextMenu.fulfilled,
                changeMobileFooter.fulfilled,
                changeUnreadCountTitle.fulfilled,
                changeUnreadCountFavicon.fulfilled,
                changeDisablePullToRefresh.fulfilled,
                changePrimaryColor.fulfilled,
                changeSharingSetting.fulfilled,
                changePushNotificationSettings.fulfilled
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

export const { setViewMode, setSidebarWidth, setAnnouncementHash, setFontSizePercentage } = userSlice.actions
