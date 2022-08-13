import { t } from "@lingui/macro"
import { Select, Stack, Switch } from "@mantine/core"
import { changeLanguage, changeScrollSpeed } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { locales } from "i18n"

export function DisplaySettings() {
    const language = useAppSelector(state => state.user.settings?.language)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
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
        </Stack>
    )
}
