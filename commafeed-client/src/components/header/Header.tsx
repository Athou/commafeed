import { t, Trans } from "@lingui/macro"
import { Center, Code, Divider, Group, Text } from "@mantine/core"
import { openConfirmModal } from "@mantine/modals"
import { markAllEntries, reloadEntries } from "app/slices/entries"
import { changeReadingMode, changeReadingOrder } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ActionButton } from "components/ActionButtton"
import { Loader } from "components/Loader"
import { TbArrowDown, TbArrowUp, TbChecks, TbEye, TbEyeOff, TbRefresh, TbUser } from "react-icons/tb"
import { ProfileMenu } from "./ProfileMenu"

function HeaderDivider() {
    return <Divider orientation="vertical" />
}

const iconSize = 18

export function Header() {
    const source = useAppSelector(state => state.entries.source)
    const sourceLabel = useAppSelector(state => state.entries.sourceLabel)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp)
    const settings = useAppSelector(state => state.user.settings)
    const profile = useAppSelector(state => state.user.profile)
    const dispatch = useAppDispatch()

    const openMarkAllEntriesModal = () =>
        openConfirmModal({
            title: t`Mark all entries as read`,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to mark all entries of <Code>{sourceLabel}</Code> as read?
                    </Trans>
                </Text>
            ),
            labels: { confirm: t`Confirm`, cancel: t`Cancel` },
            confirmProps: { color: "red" },
            onConfirm: () =>
                dispatch(
                    markAllEntries({
                        sourceType: source.type,
                        req: {
                            id: source.id,
                            read: true,
                            olderThan: entriesTimestamp,
                        },
                    })
                ),
        })

    if (!settings) return <Loader />
    return (
        <Center>
            <Group>
                <ActionButton icon={<TbRefresh size={iconSize} />} label={t`Refresh`} onClick={() => dispatch(reloadEntries())} />
                <ActionButton icon={<TbChecks size={iconSize} />} label={t`Mark all as read`} onClick={openMarkAllEntriesModal} />

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
