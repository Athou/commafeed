import { t, Trans } from "@lingui/macro"
import { Divider, Group, Radio, Select, SimpleGrid, Stack, Switch } from "@mantine/core"
import { type ComboboxData } from "@mantine/core/lib/components/Combobox/Combobox.types"
import { Constants } from "app/constants"
import { useAppDispatch, useAppSelector } from "app/store"
import { type IconDisplayMode, type ScrollMode, type SharingSettings } from "app/types"
import {
    changeCustomContextMenu,
    changeExternalLinkIconDisplayMode,
    changeLanguage,
    changeMarkAllAsReadConfirmation,
    changeMobileFooter,
    changeScrollMarks,
    changeScrollMode,
    changeScrollSpeed,
    changeSharingSetting,
    changeShowRead,
    changeStarIconDisplayMode,
} from "app/user/thunks"
import { locales } from "i18n"
import { type ReactNode } from "react"

const displayModeData: ComboboxData = [
    {
        value: "always",
        label: t`Always`,
    },
    {
        value: "on_desktop",
        label: t`On desktop`,
    },
    {
        value: "on_mobile",
        label: t`On mobile`,
    },
    {
        value: "never",
        label: t`Never`,
    },
]

export function DisplaySettings() {
    const language = useAppSelector(state => state.user.settings?.language)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
    const showRead = useAppSelector(state => state.user.settings?.showRead)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const scrollMode = useAppSelector(state => state.user.settings?.scrollMode)
    const starIconDisplayMode = useAppSelector(state => state.user.settings?.starIconDisplayMode)
    const externalLinkIconDisplayMode = useAppSelector(state => state.user.settings?.externalLinkIconDisplayMode)
    const markAllAsReadConfirmation = useAppSelector(state => state.user.settings?.markAllAsReadConfirmation)
    const customContextMenu = useAppSelector(state => state.user.settings?.customContextMenu)
    const mobileFooter = useAppSelector(state => state.user.settings?.mobileFooter)
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const dispatch = useAppDispatch()

    const scrollModeOptions: Record<ScrollMode, ReactNode> = {
        always: <Trans>Always</Trans>,
        never: <Trans>Never</Trans>,
        if_needed: <Trans>If the entry doesn't entirely fit on the screen</Trans>,
    }

    return (
        <Stack>
            <Select
                description={<Trans>Language</Trans>}
                value={language}
                data={locales.map(l => ({
                    value: l.key,
                    label: l.label,
                }))}
                onChange={async s => await (s && dispatch(changeLanguage(s)))}
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
                label={<Trans>On mobile, show action buttons at the bottom of the screen</Trans>}
                checked={mobileFooter}
                onChange={async e => await dispatch(changeMobileFooter(e.currentTarget.checked))}
            />

            <Divider label={<Trans>Entry headers</Trans>} labelPosition="center" />

            <Select
                description={<Trans>Show star icon</Trans>}
                value={starIconDisplayMode}
                data={displayModeData}
                onChange={async s => await dispatch(changeStarIconDisplayMode(s as IconDisplayMode))}
            />

            <Select
                description={<Trans>Show external link icon</Trans>}
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
