import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import {
    Box,
    type ComboboxData,
    Divider,
    Group,
    Loader,
    NumberInput,
    Radio,
    Select,
    type SelectProps,
    SimpleGrid,
    Stack,
    Switch,
} from "@mantine/core"
import type { ReactNode } from "react"
import { Constants } from "@/app/constants"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { IconDisplayMode, ScrollMode, SharingSettings } from "@/app/types"
import { changeSettings } from "@/app/user/thunks"
import { locales } from "@/i18n"

export function DisplaySettings() {
    const settings = useAppSelector(state => state.user.settings)
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

    if (!settings) return <Loader />
    return (
        <Stack>
            <Divider label={<Trans>Display</Trans>} labelPosition="center" />

            <Select
                label={<Trans>Language</Trans>}
                value={settings.language}
                data={locales.map(l => ({
                    value: l.key,
                    label: l.label,
                }))}
                onChange={language => language && dispatch(changeSettings({ language }))}
            />

            <Select
                label={<Trans>Primary color</Trans>}
                data={colorData}
                value={settings.primaryColor}
                onChange={primaryColor => primaryColor && dispatch(changeSettings({ primaryColor }))}
                renderOption={colorRenderer}
            />

            <Switch
                label={<Trans>Show feeds and categories with no unread entries</Trans>}
                checked={settings.showRead}
                onChange={e => dispatch(changeSettings({ showRead: e.currentTarget.checked }))}
            />

            <Switch
                label={<Trans>Show confirmation when marking all entries as read</Trans>}
                checked={settings.markAllAsReadConfirmation}
                onChange={e => dispatch(changeSettings({ markAllAsReadConfirmation: e.currentTarget.checked }))}
            />

            <Switch
                label={<Trans>Navigate to the next category/feed with unread entries when marking all entries as read</Trans>}
                checked={settings.markAllAsReadNavigateToNextUnread}
                onChange={e => dispatch(changeSettings({ markAllAsReadNavigateToNextUnread: e.currentTarget.checked }))}
            />

            <Switch
                label={<Trans>On mobile, show action buttons at the bottom of the screen</Trans>}
                checked={settings.mobileFooter}
                onChange={e => dispatch(changeSettings({ mobileFooter: e.currentTarget.checked }))}
            />

            <Divider label={<Trans>Scrolling</Trans>} labelPosition="center" />

            <Switch
                label={<Trans>Disable "Pull to refresh" browser behavior</Trans>}
                description={<Trans>This setting can cause scrolling issues on some browsers (e.g. Safari)</Trans>}
                checked={settings.disablePullToRefresh}
                onChange={e => dispatch(changeSettings({ disablePullToRefresh: e.currentTarget.checked }))}
            />

            <Radio.Group
                label={<Trans>Scroll selected entry to the top of the page</Trans>}
                value={settings.scrollMode}
                onChange={value => dispatch(changeSettings({ scrollMode: value }))}
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
                value={settings.entriesToKeepOnTopWhenScrolling}
                onChange={value => dispatch(changeSettings({ entriesToKeepOnTopWhenScrolling: +value }))}
            />

            <Switch
                label={<Trans>Scroll smoothly when navigating between entries</Trans>}
                checked={settings.scrollSpeed ? settings.scrollSpeed > 0 : false}
                onChange={e => dispatch(changeSettings({ scrollSpeed: e.currentTarget.checked ? 400 : 0 }))}
            />

            <Switch
                label={<Trans>In expanded view, scrolling through entries mark them as read</Trans>}
                checked={settings.scrollMarks}
                onChange={e => dispatch(changeSettings({ scrollMarks: e.currentTarget.checked }))}
            />

            <Divider label={<Trans>Browser tab</Trans>} labelPosition="center" />

            <Switch
                label={<Trans>Show unread count in tab title</Trans>}
                checked={settings.unreadCountTitle}
                onChange={e => dispatch(changeSettings({ unreadCountTitle: e.currentTarget.checked }))}
            />

            <Switch
                label={<Trans>Show unread count in tab favicon</Trans>}
                checked={settings.unreadCountFavicon}
                onChange={e => dispatch(changeSettings({ unreadCountFavicon: e.currentTarget.checked }))}
            />

            <Divider label={<Trans>Entry headers</Trans>} labelPosition="center" />

            <Select
                label={<Trans>Show star icon</Trans>}
                value={settings.starIconDisplayMode}
                data={displayModeData}
                onChange={value => dispatch(changeSettings({ starIconDisplayMode: value as IconDisplayMode }))}
            />

            <Select
                label={<Trans>Show external link icon</Trans>}
                value={settings.externalLinkIconDisplayMode}
                data={displayModeData}
                onChange={value => dispatch(changeSettings({ externalLinkIconDisplayMode: value as IconDisplayMode }))}
            />

            <Switch
                label={<Trans>Show CommaFeed's own context menu on right click</Trans>}
                checked={settings.customContextMenu}
                onChange={e => dispatch(changeSettings({ customContextMenu: e.currentTarget.checked }))}
            />

            <Divider label={<Trans>Sharing sites</Trans>} labelPosition="center" />

            <SimpleGrid cols={2}>
                {(Object.keys(Constants.sharing) as Array<keyof SharingSettings>).map(site => (
                    <Switch
                        key={site}
                        label={Constants.sharing[site].label}
                        checked={settings.sharingSettings[site]}
                        onChange={e =>
                            dispatch(
                                changeSettings({
                                    sharingSettings: {
                                        ...settings.sharingSettings,
                                        [site]: e.currentTarget.checked,
                                    },
                                })
                            )
                        }
                    />
                ))}
            </SimpleGrid>
        </Stack>
    )
}
