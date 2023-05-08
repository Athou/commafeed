import { t, Trans } from "@lingui/macro"
import { ActionIcon, Center, Divider, Indicator, Popover, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { reloadEntries, search } from "app/slices/entries"
import { changeReadingMode, changeReadingOrder } from "app/slices/user"
import { useAppDispatch, useAppSelector } from "app/store"
import { ActionButton } from "components/ActionButtton"
import { ButtonToolbar } from "components/ButtonToolbar"
import { Loader } from "components/Loader"
import { useEffect } from "react"
import { TbArrowDown, TbArrowUp, TbEye, TbEyeOff, TbRefresh, TbSearch, TbUser, TbX } from "react-icons/tb"
import { MarkAllAsReadButton } from "./MarkAllAsReadButton"
import { ProfileMenu } from "./ProfileMenu"

function HeaderDivider() {
    return <Divider orientation="vertical" />
}

const iconSize = 18

export function Header() {
    const settings = useAppSelector(state => state.user.settings)
    const profile = useAppSelector(state => state.user.profile)
    const searchFromStore = useAppSelector(state => state.entries.search)
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
            <ButtonToolbar>
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
                    icon={settings.readingOrder === "asc" ? <TbArrowUp size={iconSize} /> : <TbArrowDown size={iconSize} />}
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
            </ButtonToolbar>
        </Center>
    )
}
