import { t, Trans } from "@lingui/macro"
import { ActionIcon, Box, Center, Divider, Group, Indicator, Popover, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { reloadEntries, search, selectNextEntry, selectPreviousEntry } from "app/slices/entries"
import { changeReadingMode, changeReadingOrder } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ActionButton } from "components/ActionButton"
import { Loader } from "components/Loader"
import { useActionButton } from "hooks/useActionButton"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import { useMobile } from "hooks/useMobile"
import { useEffect } from "react"
import {
    TbArrowDown,
    TbArrowUp,
    TbExternalLink,
    TbEye,
    TbEyeOff,
    TbRefresh,
    TbSearch,
    TbSettings,
    TbSortAscending,
    TbSortDescending,
    TbUser,
    TbX,
} from "react-icons/tb"
import { MarkAllAsReadButton } from "./MarkAllAsReadButton"
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
            sx={{
                width: "100%",
                display: "flex",
                justifyContent: "space-between",
            }}
        >
            {props.children}
        </Box>
    ) : (
        <Group spacing={spacing}>{props.children}</Group>
    )
}

const iconSize = 18

export function Header() {
    const settings = useAppSelector(state => state.user.settings)
    const profile = useAppSelector(state => state.user.profile)
    const searchFromStore = useAppSelector(state => state.entries.search)
    const { isBrowserExtensionPopup, openSettingsPage, openAppInNewTab } = useBrowserExtension()
    const dispatch = useAppDispatch()

    const searchForm = useForm<{ search: string }>({
        validate: {
            search: value => (value.length > 0 && value.length < 3 ? t`Search requires at least 3 characters` : null),
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
        <Center>
            <HeaderToolbar>
                <ActionButton
                    icon={<TbArrowDown size={iconSize} />}
                    label={<Trans>Next</Trans>}
                    onClick={() =>
                        dispatch(
                            selectNextEntry({
                                expand: true,
                                markAsRead: true,
                                scrollToEntry: true,
                            })
                        )
                    }
                />
                <ActionButton
                    icon={<TbArrowUp size={iconSize} />}
                    label={<Trans>Previous</Trans>}
                    onClick={() =>
                        dispatch(
                            selectPreviousEntry({
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
                    label={<Trans>Refresh</Trans>}
                    onClick={() => dispatch(reloadEntries())}
                />
                <MarkAllAsReadButton iconSize={iconSize} />

                <HeaderDivider />

                <ActionButton
                    icon={settings.readingMode === "all" ? <TbEye size={iconSize} /> : <TbEyeOff size={iconSize} />}
                    label={settings.readingMode === "all" ? <Trans>All</Trans> : <Trans>Unread</Trans>}
                    onClick={() => dispatch(changeReadingMode(settings.readingMode === "all" ? "unread" : "all"))}
                />
                <ActionButton
                    icon={settings.readingOrder === "asc" ? <TbSortAscending size={iconSize} /> : <TbSortDescending size={iconSize} />}
                    label={settings.readingOrder === "asc" ? <Trans>Asc</Trans> : <Trans>Desc</Trans>}
                    onClick={() => dispatch(changeReadingOrder(settings.readingOrder === "asc" ? "desc" : "asc"))}
                />

                <Popover>
                    <Popover.Target>
                        <Indicator disabled={!searchFromStore}>
                            <ActionButton icon={<TbSearch size={iconSize} />} label={<Trans>Search</Trans>} />
                        </Indicator>
                    </Popover.Target>
                    <Popover.Dropdown>
                        <form onSubmit={searchForm.onSubmit(values => dispatch(search(values.search)))}>
                            <TextInput
                                placeholder={t`Search`}
                                {...searchForm.getInputProps("search")}
                                icon={<TbSearch size={iconSize} />}
                                rightSection={
                                    <ActionIcon onClick={() => searchFromStore && dispatch(search(""))}>
                                        <TbX />
                                    </ActionIcon>
                                }
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
                            label={<Trans>Extension options</Trans>}
                            onClick={() => openSettingsPage()}
                        />
                        <ActionButton
                            icon={<TbExternalLink size={iconSize} />}
                            label={<Trans>Open CommaFeed</Trans>}
                            onClick={() => openAppInNewTab()}
                        />
                    </>
                )}
            </HeaderToolbar>
        </Center>
    )
}
