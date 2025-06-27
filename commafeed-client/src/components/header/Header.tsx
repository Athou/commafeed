import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Box, Center, CloseButton, Divider, Group, Indicator, Popover, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useEffect } from "react"
import {
    TbArrowDown,
    TbArrowUp,
    TbChecks,
    TbExternalLink,
    TbEye,
    TbEyeOff,
    TbRefresh,
    TbSearch,
    TbSettings,
    TbSortAscending,
    TbSortDescending,
    TbUser,
} from "react-icons/tb"
import { markAllAsReadWithConfirmationIfRequired, reloadEntries, search, selectNextEntry, selectPreviousEntry } from "@/app/entries/thunks"
import { useAppDispatch, useAppSelector } from "@/app/store"
import { changeReadingMode, changeReadingOrder } from "@/app/user/thunks"
import { ActionButton } from "@/components/ActionButton"
import { Loader } from "@/components/Loader"
import { useActionButton } from "@/hooks/useActionButton"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"
import { useMobile } from "@/hooks/useMobile"
import { ProfileMenu } from "./ProfileMenu"

function HeaderDivider() {
    return <Divider orientation="vertical" />
}

function HeaderToolbar(props: { children: React.ReactNode }) {
    const { spacing } = useActionButton()
    const mobile = useMobile("480px")
    return mobile ? (
        // on mobile use all available width
        <Box
            style={{
                width: "100%",
                display: "flex",
                justifyContent: "space-between",
            }}
            className="cf-toolbar"
        >
            {props.children}
        </Box>
    ) : (
        <Group gap={spacing} className="cf-toolbar">
            {props.children}
        </Group>
    )
}

const iconSize = 18

export function Header() {
    const settings = useAppSelector(state => state.user.settings)
    const profile = useAppSelector(state => state.user.profile)
    const searchFromStore = useAppSelector(state => state.entries.search)
    const { isBrowserExtensionPopup, openSettingsPage, openAppInNewTab } = useBrowserExtension()
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const searchForm = useForm<{ search: string }>({
        validate: {
            search: value => (value.length > 0 && value.length < 3 ? _(msg`Search requires at least 3 characters`) : null),
        },
    })
    const { setValues } = searchForm

    useEffect(() => {
        setValues({
            search: searchFromStore,
        })
    }, [setValues, searchFromStore])

    if (!settings) return <Loader />
    return (
        <Center className="cf-toolbar-wrapper">
            <HeaderToolbar>
                <ActionButton
                    icon={<TbArrowUp size={iconSize} />}
                    label={msg`Previous`}
                    onClick={async () =>
                        await dispatch(
                            selectPreviousEntry({
                                expand: true,
                                markAsRead: true,
                                scrollToEntry: true,
                            })
                        )
                    }
                />
                <ActionButton
                    icon={<TbArrowDown size={iconSize} />}
                    label={msg`Next`}
                    onClick={async () =>
                        await dispatch(
                            selectNextEntry({
                                expand: true,
                                markAsRead: true,
                                scrollToEntry: true,
                            })
                        )
                    }
                />

                <HeaderDivider />

                <ActionButton
                    icon={<TbRefresh size={iconSize} />}
                    label={msg`Refresh`}
                    onClick={async () => await dispatch(reloadEntries())}
                />
                <ActionButton
                    icon={<TbChecks size={iconSize} />}
                    label={msg`Mark all as read`}
                    onClick={() => dispatch(markAllAsReadWithConfirmationIfRequired())}
                />

                <HeaderDivider />

                <ActionButton
                    icon={settings.readingMode === "all" ? <TbEye size={iconSize} /> : <TbEyeOff size={iconSize} />}
                    label={settings.readingMode === "all" ? msg`All` : msg`Unread`}
                    onClick={async () => await dispatch(changeReadingMode(settings.readingMode === "all" ? "unread" : "all"))}
                />
                <ActionButton
                    icon={settings.readingOrder === "asc" ? <TbSortAscending size={iconSize} /> : <TbSortDescending size={iconSize} />}
                    label={settings.readingOrder === "asc" ? msg`Asc` : msg`Desc`}
                    onClick={async () => await dispatch(changeReadingOrder(settings.readingOrder === "asc" ? "desc" : "asc"))}
                />

                <Popover>
                    <Popover.Target>
                        <Indicator disabled={!searchFromStore}>
                            <ActionButton icon={<TbSearch size={iconSize} />} label={msg`Search`} />
                        </Indicator>
                    </Popover.Target>
                    <Popover.Dropdown>
                        <form onSubmit={searchForm.onSubmit(async values => await dispatch(search(values.search)))}>
                            <TextInput
                                placeholder={_(msg`Search`)}
                                {...searchForm.getInputProps("search")}
                                leftSection={<TbSearch size={iconSize} />}
                                rightSection={<CloseButton onClick={async () => await (searchFromStore && dispatch(search("")))} />}
                                autoFocus
                            />
                        </form>
                    </Popover.Dropdown>
                </Popover>

                <HeaderDivider />

                <ProfileMenu control={<ActionButton icon={<TbUser size={iconSize} />} label={profile?.name} />} />

                {isBrowserExtensionPopup && (
                    <>
                        <HeaderDivider />

                        <ActionButton
                            icon={<TbSettings size={iconSize} />}
                            label={msg`Extension options`}
                            onClick={() => openSettingsPage()}
                        />
                        <ActionButton
                            icon={<TbExternalLink size={iconSize} />}
                            label={msg`Open CommaFeed`}
                            onClick={() => openAppInNewTab()}
                        />
                    </>
                )}
            </HeaderToolbar>
        </Center>
    )
}
