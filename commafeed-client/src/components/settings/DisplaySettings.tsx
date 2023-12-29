import { Trans } from "@lingui/macro"
import { Divider, Select, SimpleGrid, Stack, Switch } from "@mantine/core"
import { Constants } from "app/constants"
import { useAppDispatch, useAppSelector } from "app/store"
import { type SharingSettings } from "app/types"
import {
    changeAlwaysScrollToEntry,
    changeCustomContextMenu,
    changeLanguage,
    changeMarkAllAsReadConfirmation,
    changeScrollMarks,
    changeScrollSpeed,
    changeSharingSetting,
    changeShowRead,
} from "app/user/thunks"
import { locales } from "i18n"

export function DisplaySettings() {
    const language = useAppSelector(state => state.user.settings?.language)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
    const showRead = useAppSelector(state => state.user.settings?.showRead)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const alwaysScrollToEntry = useAppSelector(state => state.user.settings?.alwaysScrollToEntry)
    const markAllAsReadConfirmation = useAppSelector(state => state.user.settings?.markAllAsReadConfirmation)
    const customContextMenu = useAppSelector(state => state.user.settings?.customContextMenu)
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const dispatch = useAppDispatch()

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
                label={<Trans>Scroll smoothly when navigating between entries</Trans>}
                checked={scrollSpeed ? scrollSpeed > 0 : false}
                onChange={async e => await dispatch(changeScrollSpeed(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Always scroll selected entry to the top of the page, even if it fits entirely on screen</Trans>}
                checked={alwaysScrollToEntry}
                onChange={async e => await dispatch(changeAlwaysScrollToEntry(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Show feeds and categories with no unread entries</Trans>}
                checked={showRead}
                onChange={async e => await dispatch(changeShowRead(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>In expanded view, scrolling through entries mark them as read</Trans>}
                checked={scrollMarks}
                onChange={async e => await dispatch(changeScrollMarks(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Show confirmation when marking all entries as read</Trans>}
                checked={markAllAsReadConfirmation}
                onChange={async e => await dispatch(changeMarkAllAsReadConfirmation(e.currentTarget.checked))}
            />

            <Switch
                label={<Trans>Show CommaFeed's own context menu on right click</Trans>}
                checked={customContextMenu}
                onChange={async e => await dispatch(changeCustomContextMenu(e.currentTarget.checked))}
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
