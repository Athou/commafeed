import { Trans } from "@lingui/react/macro"
import {
    Box,
    Divider,
    Group,
    type MantineColorScheme,
    Menu,
    SegmentedControl,
    type SegmentedControlItem,
    useMantineColorScheme,
} from "@mantine/core"
import { showNotification } from "@mantine/notifications"
import { client } from "app/client"
import { redirectToAbout, redirectToAdminUsers, redirectToDonate, redirectToMetrics, redirectToSettings } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import type { ViewMode } from "app/types"
import { setViewMode } from "app/user/slice"
import { reloadProfile } from "app/user/thunks"
import dayjs from "dayjs"
import { useNow } from "hooks/useNow"
import { type ReactNode, useState } from "react"
import {
    TbChartLine,
    TbHeartFilled,
    TbHelp,
    TbLayoutList,
    TbList,
    TbListDetails,
    TbMoon,
    TbNotes,
    TbPower,
    TbSettings,
    TbSun,
    TbSunMoon,
    TbUsers,
    TbWorldDownload,
} from "react-icons/tb"

interface ProfileMenuProps {
    control: React.ReactElement
}

const ProfileMenuControlItem = ({ icon, label }: { icon: ReactNode; label: ReactNode }) => {
    return (
        <Group className={"cf-ProfileMenuControlItem-Group"}>
            {icon}
            <Box className={"cf-ProfileMenuControlItem-Box"} ml={6}>
                {label}
            </Box>
        </Group>
    )
}

const iconSize = 16

interface ColorSchemeControlItem extends SegmentedControlItem {
    value: MantineColorScheme
}

const colorSchemeData: ColorSchemeControlItem[] = [
    {
        value: "light",
        label: <ProfileMenuControlItem icon={<TbSun size={iconSize} />} label={<Trans>Light</Trans>} />,
    },
    {
        value: "dark",
        label: <ProfileMenuControlItem icon={<TbMoon size={iconSize} />} label={<Trans>Dark</Trans>} />,
    },
    {
        value: "auto",
        label: <ProfileMenuControlItem icon={<TbSunMoon size={iconSize} />} label={<Trans>System</Trans>} />,
    },
]

interface ViewModeControlItem extends SegmentedControlItem {
    value: ViewMode
}

const viewModeData: ViewModeControlItem[] = [
    {
        value: "title",
        label: <ProfileMenuControlItem icon={<TbList size={iconSize} />} label={<Trans>Compact</Trans>} />,
    },
    {
        value: "cozy",
        label: <ProfileMenuControlItem icon={<TbLayoutList size={iconSize} />} label={<Trans>Cozy</Trans>} />,
    },
    {
        value: "detailed",
        label: <ProfileMenuControlItem icon={<TbListDetails size={iconSize} />} label={<Trans>Detailed</Trans>} />,
    },
    {
        value: "expanded",
        label: <ProfileMenuControlItem icon={<TbNotes size={iconSize} />} label={<Trans>Expanded</Trans>} />,
    },
]

export function ProfileMenu(props: ProfileMenuProps) {
    const [opened, setOpened] = useState(false)
    const now = useNow()
    const profile = useAppSelector(state => state.user.profile)
    const admin = useAppSelector(state => state.user.profile?.admin)
    const viewMode = useAppSelector(state => state.user.localSettings.viewMode)
    const forceRefreshCooldownDuration = useAppSelector(state => state.server.serverInfos?.forceRefreshCooldownDuration)
    const dispatch = useAppDispatch()
    const { colorScheme, setColorScheme } = useMantineColorScheme()

    const nextAvailableForceRefresh = profile?.lastForceRefresh
        ? profile.lastForceRefresh + (forceRefreshCooldownDuration ?? 0)
        : now.getTime()
    const forceRefreshEnabled = nextAvailableForceRefresh <= now.getTime()

    const logout = () => {
        window.location.href = "logout"
    }

    return (
        <Menu position="bottom-end" closeOnItemClick={false} opened={opened} onChange={setOpened}>
            <Menu.Target>{props.control}</Menu.Target>
            <Menu.Dropdown>
                {profile && <Menu.Label>{profile.name}</Menu.Label>}
                <Menu.Item
                    className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Settings"}
                    leftSection={<TbSettings size={iconSize} />}
                    onClick={() => {
                        dispatch(redirectToSettings())
                        setOpened(false)
                    }}
                >
                    <Trans>Settings</Trans>
                </Menu.Item>
                <Menu.Item
                    className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Refresh"}
                    leftSection={<TbWorldDownload size={iconSize} />}
                    disabled={!forceRefreshEnabled}
                    onClick={async () => {
                        setOpened(false)

                        try {
                            await client.feed.refreshAll()

                            // reload profile to update last force refresh timestamp
                            await dispatch(reloadProfile())

                            showNotification({
                                message: <Trans>Your feeds have been queued for refresh.</Trans>,
                                color: "green",
                                autoClose: 1000,
                            })
                        } catch (_) {
                            showNotification({
                                message: <Trans>Force fetching feeds is not yet available.</Trans>,
                                color: "red",
                                autoClose: 2000,
                            })
                        }
                    }}
                >
                    <Trans>Fetch all my feeds now</Trans>
                    {!forceRefreshEnabled && <span> ({dayjs.duration(nextAvailableForceRefresh - now.getTime()).format("HH:mm:ss")})</span>}
                </Menu.Item>

                <Divider className={"cf-ProfileMenu-Divider"} />

                <Menu.Label className={"cf-ProfileMenu-Label"}>
                    <Trans>Theme</Trans>
                </Menu.Label>
                <SegmentedControl
                    className={"cf-ProfileMenu-SegmentedControl-Theme"}
                    fullWidth
                    orientation="vertical"
                    data={colorSchemeData}
                    value={colorScheme}
                    onChange={e => setColorScheme(e as MantineColorScheme)}
                    mb="xs"
                />

                <Divider className={"cf-ProfileMenu-Divider"} />

                <Menu.Label className={"cf-ProfileMenu-Label"}>
                    <Trans>Display</Trans>
                </Menu.Label>
                <SegmentedControl
                    className={"cf-ProfileMenu-SegmentedControl-ViewMode"}
                    fullWidth
                    orientation="vertical"
                    data={viewModeData}
                    value={viewMode}
                    onChange={e => dispatch(setViewMode(e as ViewMode))}
                    mb="xs"
                />

                {admin && (
                    <>
                        <Divider className={"cf-ProfileMenu-Divider"} />
                        <Menu.Label className={"cf-ProfileMenu-Label"}>
                            <Trans>Admin</Trans>
                        </Menu.Label>
                        <Menu.Item
                            className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Admin"}
                            leftSection={<TbUsers size={iconSize} />}
                            onClick={() => {
                                dispatch(redirectToAdminUsers())
                                setOpened(false)
                            }}
                        >
                            <Trans>Manage users</Trans>
                        </Menu.Item>
                        <Menu.Item
                            className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Metrics"}
                            leftSection={<TbChartLine size={iconSize} />}
                            onClick={() => {
                                dispatch(redirectToMetrics())
                                setOpened(false)
                            }}
                        >
                            <Trans>Metrics</Trans>
                        </Menu.Item>
                    </>
                )}

                <Divider className={"cf-ProfileMenu-Divider"} />

                <Menu.Item
                    className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Donate"}
                    leftSection={<TbHeartFilled size={iconSize} color="red" />}
                    onClick={() => {
                        dispatch(redirectToDonate())
                        setOpened(false)
                    }}
                >
                    <Trans>Donate</Trans>
                </Menu.Item>

                <Menu.Item
                    className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-About"}
                    leftSection={<TbHelp size={iconSize} />}
                    onClick={() => {
                        dispatch(redirectToAbout())
                        setOpened(false)
                    }}
                >
                    <Trans>About</Trans>
                </Menu.Item>
                <Menu.Item
                    className={"cf-ProfileMenu-Item cf-ProfileMenu-Item-Logout"}
                    leftSection={<TbPower size={iconSize} />}
                    onClick={logout}
                >
                    <Trans>Logout</Trans>
                </Menu.Item>
            </Menu.Dropdown>
        </Menu>
    )
}
