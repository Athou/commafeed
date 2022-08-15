import { t } from "@lingui/macro"
import { Center, Divider, Group } from "@mantine/core"
import { reloadEntries } from "app/slices/entries"
import { changeReadingMode, changeReadingOrder } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ActionButton } from "components/ActionButtton"
import { Loader } from "components/Loader"
import { TbArrowDown, TbArrowUp, TbEye, TbEyeOff, TbRefresh, TbUser } from "react-icons/tb"
import { MarkAllAsReadButton } from "./MarkAllAsReadButton"
import { ProfileMenu } from "./ProfileMenu"

function HeaderDivider() {
    return <Divider orientation="vertical" />
}

const iconSize = 18
export function Header() {
    const settings = useAppSelector(state => state.user.settings)
    const profile = useAppSelector(state => state.user.profile)
    const dispatch = useAppDispatch()

    if (!settings) return <Loader />
    return (
        <Center>
            <Group>
                <ActionButton icon={<TbRefresh size={iconSize} />} label={t`Refresh`} onClick={() => dispatch(reloadEntries())} />
                <MarkAllAsReadButton iconSize={iconSize} />

                <HeaderDivider />

                <ActionButton
                    icon={settings.readingMode === "all" ? <TbEye size={iconSize} /> : <TbEyeOff size={iconSize} />}
                    label={settings.readingMode === "all" ? t`All` : t`Unread`}
                    onClick={() => dispatch(changeReadingMode(settings.readingMode === "all" ? "unread" : "all"))}
                />
                <ActionButton
                    icon={settings.readingOrder === "asc" ? <TbArrowUp size={iconSize} /> : <TbArrowDown size={iconSize} />}
                    label={settings.readingOrder === "asc" ? t`Asc` : t`Desc`}
                    onClick={() => dispatch(changeReadingOrder(settings.readingOrder === "asc" ? "desc" : "asc"))}
                />

                <HeaderDivider />

                <ProfileMenu control={<ActionButton icon={<TbUser size={iconSize} />} label={profile?.name} />} />
            </Group>
        </Center>
    )
}
