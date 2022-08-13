import { Trans } from "@lingui/macro"
import { Divider, Menu, useMantineColorScheme } from "@mantine/core"
import { redirectToAdminUsers, redirectToSettings } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { useState } from "react"
import { TbMoon, TbPower, TbSettings, TbSun, TbUsers } from "react-icons/tb"

interface ProfileMenuProps {
    control: React.ReactElement
}

export function ProfileMenu(props: ProfileMenuProps) {
    const [opened, setOpened] = useState(false)
    const admin = useAppSelector(state => state.user.profile?.admin)
    const dispatch = useAppDispatch()
    const { colorScheme, toggleColorScheme } = useMantineColorScheme()
    const dark = colorScheme === "dark"

    const logout = () => {
        window.location.href = "logout"
    }

    return (
        <Menu position="bottom-end" closeOnItemClick={false} opened={opened} onChange={setOpened}>
            <Menu.Target>{props.control}</Menu.Target>
            <Menu.Dropdown>
                <Menu.Item
                    icon={<TbSettings />}
                    onClick={() => {
                        dispatch(redirectToSettings())
                        setOpened(false)
                    }}
                >
                    <Trans>Settings</Trans>
                </Menu.Item>
                <Menu.Item icon={dark ? <TbMoon /> : <TbSun />} onClick={() => toggleColorScheme()}>
                    <Trans>Theme</Trans>
                </Menu.Item>

                {admin && (
                    <>
                        <Divider />
                        <Menu.Label>
                            <Trans>Admin</Trans>
                        </Menu.Label>
                        <Menu.Item
                            icon={<TbUsers />}
                            onClick={() => {
                                dispatch(redirectToAdminUsers())
                                setOpened(false)
                            }}
                        >
                            <Trans>Manage users</Trans>
                        </Menu.Item>
                    </>
                )}

                <Divider />
                <Menu.Item icon={<TbPower />} onClick={logout}>
                    <Trans>Logout</Trans>
                </Menu.Item>
            </Menu.Dropdown>
        </Menu>
    )
}
