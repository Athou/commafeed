import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Box, Divider, Group, NumberInput, Radio, Select, type SelectProps, SimpleGrid, Stack, Switch } from "@mantine/core"
import type { ComboboxData } from "@mantine/core/lib/components/Combobox/Combobox.types"
import type { ReactNode } from "react"
import { Constants } from "@/app/constants"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { IconDisplayMode, ScrollMode, SharingSettings } from "@/app/types"
import {
    changeCustomContextMenu,
    changeEntriesToKeepOnTopWhenScrolling,
    changeExternalLinkIconDisplayMode,
    changeLanguage,
    changeMarkAllAsReadConfirmation,
    changeMarkAllAsReadNavigateToUnread,
    changeMobileFooter,
    changePrimaryColor,
    changeScrollMarks,
    changeScrollMode,
    changeScrollSpeed,
    changeSharingSetting,
    changeShowRead,
    changeStarIconDisplayMode,
    changeUnreadCountFavicon,
    changeUnreadCountTitle,
} from "@/app/user/thunks"
import { locales } from "@/i18n"

export function DisplaySettings() {
    const language = useAppSelector(state => state.user.settings?.language)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
    const showRead = useAppSelector(state => state.user.settings?.showRead)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const scrollMode = useAppSelector(state => state.user.settings?.scrollMode)
    const entriesToKeepOnTop = useAppSelector(state => state.user.settings?.entriesToKeepOnTopWhenScrolling)
    const starIconDisplayMode = useAppSelector(state => state.user.settings?.starIconDisplayMode)
    const externalLinkIconDisplayMode = useAppSelector(state => state.user.settings?.externalLinkIconDisplayMode)
    const markAllAsReadConfirmation = useAppSelector(state => state.user.settings?.markAllAsReadConfirmation)
    const markAllAsReadNavigateToNextUnread = useAppSelector(state => state.user.settings?.markAllAsReadNavigateToNextUnread)
    const customContextMenu = useAppSelector(state => state.user.settings?.customContextMenu)
    const mobileFooter = useAppSelector(state => state.user.settings?.mobileFooter)
    const unreadCountTitle = useAppSelector(state => state.user.settings?.unreadCountTitle)
    const unreadCountFavicon = useAppSelector(state => state.user.settings?.unreadCountFavicon)
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const primaryColor = useAppSelector(state => state.user.settings?.primaryColor) || Constants.theme.defaultPrimaryColor
    const { _ } = useLingui()
    const dispatch = useAppDispatch()

    const scrollModeOptions: Record<ScrollMode, ReactNode> = {
        always: <Trans>Always</Trans>,
        never: <Trans>Never</Trans>,
        if_needed: <Trans>If the entry doesn't entirely fit on the screen</Trans>,
    }

    const displayModeData: ComboboxData = [
        {
            value: "always",
            label: _(msg`Always`),
        },
        {
            value: "on_desktop",
            label: _(msg`On desktop`),
        },
        {
            value: "on_mobile",
            label: _(msg`On mobile`),
        },
        {
            value: "never",
            label: _(msg`Never`),
        },
    ]

    const colorData: ComboboxData = [
        { value: "dark", label: _(msg`Dark`) },
        { value: "gray", label: _(msg`Gray`) },
        { value: "red", label: _(msg`Red`) },
        { value: "pink", label: _(msg`Pink`) },
        { value: "grape", label: _(msg`Grape`) },
        { value: "violet", label: _(msg`Violet`) },
        { value: "indigo", label: _(msg`Indigo`) },
        { value: "blue", label: _(msg`Blue`) },
        { value: "cyan", label: _(msg`Cyan`) },
        { value: "green", label: _(msg`Green`) },
        { value: "lime", label: _(msg`Lime`) },
        { value: "yellow", label: _(msg`Yellow`) },
        { value: "orange", label: _(msg`Orange`) },
        { value: "teal", label: _(msg`Teal`) },
    ].sort((a, b) => a.label.localeCompare(b.label))
    const colorRenderer: SelectProps["renderOption"] = ({ option }) => (
        <Group>
            <Box h={18} w={18} bg={option.value} />
            <Box>{option.label}</Box>
        </Group>
    )

    return (
        <Stack>
            <Divider label={<Trans>Display</Trans>} labelPosition="center" />

            <Select
                label={<Trans>Language</Trans>}
                value={language}
                data={locales.map(l => ({
                    value: l.key,
                    label: l.label,
                }))}
                onChange={async s => await (s && dispatch(changeLanguage(s)))}
            />

            <Select
                label={<Trans>Primary color</Trans>}
                data={colorData}
                value={primaryColor}
                onChange={async value => value && (await dispatch(changePrimaryColor(value)))}
                renderOption={colorRenderer}
            />

            <Switch
                label={<Trans>Show feeds and categories with no unread entries</Trans>}
                checked={showRead}
                onChange={async e => await dispatch(changeShowRead(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Show confirmation when marking all entries as read</Trans>}
                checked={markAllAsReadConfirmation}
                onChange={async e => await dispatch(changeMarkAllAsReadConfirmation(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Navigate to the next category/feed with unread entries when marking all entries as read</Trans>}
                checked={markAllAsReadNavigateToNextUnread}
                onChange={async e => await dispatch(changeMarkAllAsReadNavigateToUnread(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>On mobile, show action buttons at the bottom of the screen</Trans>}
                checked={mobileFooter}
                onChange={async e => await dispatch(changeMobileFooter(e.currentTarget.checked))}
            />

            <Divider label={<Trans>Browser tab</Trans>} labelPosition="center" />

            <Switch
                label={<Trans>Show unread count in tab title</Trans>}
                checked={unreadCountTitle}
                onChange={async e => await dispatch(changeUnreadCountTitle(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Show unread count in tab favicon</Trans>}
                checked={unreadCountFavicon}
                onChange={async e => await dispatch(changeUnreadCountFavicon(e.currentTarget.checked))}
            />

            <Divider label={<Trans>Entry headers</Trans>} labelPosition="center" />

            <Select
                label={<Trans>Show star icon</Trans>}
                value={starIconDisplayMode}
                data={displayModeData}
                onChange={async s => await dispatch(changeStarIconDisplayMode(s as IconDisplayMode))}
            />

            <Select
                label={<Trans>Show external link icon</Trans>}
                value={externalLinkIconDisplayMode}
                data={displayModeData}
                onChange={async s => await dispatch(changeExternalLinkIconDisplayMode(s as IconDisplayMode))}
            />

            <Switch
                label={<Trans>Show CommaFeed's own context menu on right click</Trans>}
                checked={customContextMenu}
                onChange={async e => await dispatch(changeCustomContextMenu(e.currentTarget.checked))}
            />

            <Divider label={<Trans>Scrolling</Trans>} labelPosition="center" />

            <Radio.Group
                label={<Trans>Scroll selected entry to the top of the page</Trans>}
                value={scrollMode}
                onChange={async value => await dispatch(changeScrollMode(value as ScrollMode))}
            >
                <Group mt="xs">
                    {Object.entries(scrollModeOptions).map(e => (
                        <Radio key={e[0]} value={e[0]} label={e[1]} />
                    ))}
                </Group>
            </Radio.Group>

            <NumberInput
                label={<Trans>Entries to keep above the selected entry when scrolling</Trans>}
                description={<Trans>Only applies to compact, cozy and detailed modes</Trans>}
                min={0}
                value={entriesToKeepOnTop}
                onChange={async value => await dispatch(changeEntriesToKeepOnTopWhenScrolling(+value))}
            />

            <Switch
                label={<Trans>Scroll smoothly when navigating between entries</Trans>}
                checked={scrollSpeed ? scrollSpeed > 0 : false}
                onChange={async e => await dispatch(changeScrollSpeed(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>In expanded view, scrolling through entries mark them as read</Trans>}
                checked={scrollMarks}
                onChange={async e => await dispatch(changeScrollMarks(e.currentTarget.checked))}
            />

            <Divider label={<Trans>Sharing sites</Trans>} labelPosition="center" />

            <SimpleGrid cols={2}>
                {(Object.keys(Constants.sharing) as Array<keyof SharingSettings>).map(site => (
                    <Switch
                        key={site}
                        label={Constants.sharing[site].label}
                        checked={sharingSettings?.[site]}
                        onChange={async e =>
                            await dispatch(
                                changeSharingSetting({
                                    site,
                                    value: e.currentTarget.checked,
                                })
                            )
                        }
                    />
                ))}
            </SimpleGrid>
        </Stack>
    )
}
