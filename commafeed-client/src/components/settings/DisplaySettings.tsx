import { t } from "@lingui/macro"
import { Divider, Select, SimpleGrid, Stack, Switch } from "@mantine/core"
import { Constants } from "app/constants"
import { changeLanguage, changeScrollMarks, changeScrollSpeed, changeSharingSetting, changeShowRead } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { SharingSettings } from "app/types"
import { locales } from "i18n"

export function DisplaySettings() {
    const language = useAppSelector(state => state.user.settings?.language)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
    const showRead = useAppSelector(state => state.user.settings?.showRead)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const dispatch = useAppDispatch()

    return (
        <Stack>
            <Select
                description={t`Language`}
                value={language}
                data={locales.map(l => ({
                    value: l.key,
                    label: l.label,
                }))}
                onChange={s => s && dispatch(changeLanguage(s))}
            />

            <Switch
                label={t`Scroll smoothly when navigating between entries`}
                checked={scrollSpeed ? scrollSpeed > 0 : false}
                onChange={e => dispatch(changeScrollSpeed(e.currentTarget.checked))}
            />

            <Switch
                label={t`Show feeds and categories with no unread entries`}
                checked={showRead}
                onChange={e => dispatch(changeShowRead(e.currentTarget.checked))}
            />

            <Switch
                label={t`In expanded view, scrolling through entries mark them as read`}
                checked={scrollMarks}
                onChange={e => dispatch(changeScrollMarks(e.currentTarget.checked))}
            />

            <Divider label={t`Sharing sites`} labelPosition="center" />

            <SimpleGrid cols={2}>
                {(Object.keys(Constants.sharing) as Array<keyof SharingSettings>).map(site => (
                    <Switch
                        key={site}
                        label={Constants.sharing[site].label}
                        checked={sharingSettings && sharingSettings[site]}
                        onChange={e => dispatch(changeSharingSetting({ site, value: e.currentTarget.checked }))}
                    />
                ))}
            </SimpleGrid>
        </Stack>
    )
}
