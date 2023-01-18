import { t, Trans } from "@lingui/macro"
import { Menu } from "@mantine/core"
import { showNotification } from "@mantine/notifications"
import { client } from "app/client"
import { reloadEntries } from "app/slices/entries"
import { useAppDispatch } from "app/store"
import { TbRotateClockwise, TbWorldDownload } from "react-icons/tb"

interface RefreshMenuProps {
    control: React.ReactElement
}

const iconSize = 16

export function RefreshMenu(props: RefreshMenuProps) {
    const dispatch = useAppDispatch()

    return (
        <Menu>
            <Menu.Target>{props.control}</Menu.Target>
            <Menu.Dropdown>
                <Menu.Item icon={<TbRotateClockwise size={iconSize} />} onClick={() => dispatch(reloadEntries())}>
                    <Trans>Reload</Trans>
                </Menu.Item>
                <Menu.Item
                    icon={<TbWorldDownload size={iconSize} />}
                    onClick={() =>
                        client.feed.refreshAll().then(() =>
                            showNotification({
                                message: t`Your feeds have been queued for refresh.`,
                                color: "green",
                            })
                        )
                    }
                >
                    <Trans>Fetch all my feeds now</Trans>
                </Menu.Item>
            </Menu.Dropdown>
        </Menu>
    )
}
