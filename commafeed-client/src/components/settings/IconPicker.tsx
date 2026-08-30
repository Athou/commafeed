import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { ActionIcon, Box, Popover, SimpleGrid, TextInput } from "@mantine/core"
import { useState } from "react"
import { useAsync } from "react-async-hook"
import { TbPhoto } from "react-icons/tb"
import { loadSiIcons } from "@/app/siIcons"
import { Loader } from "@/components/Loader"
import { tss } from "@/tss"

const MAX_RESULTS = 200

const useStyles = tss.create(({ theme, colorScheme }) => ({
    trigger: {
        cursor: "pointer",
        color: colorScheme === "dark" ? theme.colors.gray[2] : "black",
    },
}))

interface IconPickerProps {
    value: string
    onChange: (icon: string) => void
}

export function IconPicker(props: Readonly<IconPickerProps>) {
    const { classes } = useStyles()
    const { _ } = useLingui()
    const [opened, setOpened] = useState(false)
    const [hasOpened, setHasOpened] = useState(false)
    const [query, setQuery] = useState("")

    // Only fetch the ~3300-export react-icons/si module when it's actually needed: either the
    // picker has been opened, or there's already a selected icon whose name we must resolve to
    // render the trigger's preview. Gating on `opened` alone would wipe the resolved icons (and
    // the preview with them) whenever the popover closes or the row remounts after a save.
    const needsIcons = hasOpened || props.value.length > 0
    const { result: icons } = useAsync(needsIcons ? loadSiIcons : async () => undefined, [needsIcons])

    const SelectedIcon = icons?.[props.value]

    const filteredNames = icons
        ? Object.keys(icons)
              .filter(name => name.toLowerCase().includes(query.toLowerCase()))
              .slice(0, MAX_RESULTS)
        : []

    return (
        <Popover withArrow withinPortal shadow="md" opened={opened} onChange={setOpened}>
            <Popover.Target>
                <ActionIcon
                    variant="default"
                    size="lg"
                    className={classes.trigger}
                    onClick={() => {
                        setOpened(o => !o)
                        setHasOpened(true)
                    }}
                >
                    {SelectedIcon ? SelectedIcon({ size: 18 }) : <TbPhoto size={18} />}
                </ActionIcon>
            </Popover.Target>
            <Popover.Dropdown w={320}>
                <TextInput
                    placeholder={_(msg`Search icons`)}
                    value={query}
                    onChange={event => setQuery(event.currentTarget.value)}
                    mb="xs"
                />
                {!icons && <Loader />}
                {icons && (
                    <Box style={{ height: 240, overflowY: "auto" }}>
                        <SimpleGrid cols={6} spacing="xs">
                            {filteredNames.map(name => {
                                const Icon = icons[name]
                                const selected = name === props.value
                                return (
                                    <ActionIcon
                                        key={name}
                                        title={name}
                                        variant={selected ? "filled" : "subtle"}
                                        size="lg"
                                        onClick={() => {
                                            props.onChange(name)
                                            setOpened(false)
                                        }}
                                    >
                                        {Icon({ size: 20, style: { pointerEvents: "none" } })}
                                    </ActionIcon>
                                )
                            })}
                        </SimpleGrid>
                    </Box>
                )}
            </Popover.Dropdown>
        </Popover>
    )
}
