import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { ActionIcon, Button, Code, Group, Stack, Text, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useEffect, useRef, useState } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbPlus, TbTrash } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { useAppDispatch, useAppSelector } from "@/app/store"
import type { CustomSharingDestination, Settings } from "@/app/types"
import { reloadSettings } from "@/app/user/thunks"
import { Alert } from "@/components/Alert"
import { IconPicker } from "@/components/settings/IconPicker"

interface FormValues {
    destinations: CustomSharingDestination[]
}

// Kept out of the <Trans> blocks below on purpose: these are literal placeholder/URL syntax,
// not translatable prose, and lingui would otherwise parse `${url}` as an ICU message argument
// and render it as an empty string.
// biome-ignore lint/suspicious/noTemplateCurlyInString: literal placeholder the user types into the URL pattern
const URL_PLACEHOLDER = "${url}"
// biome-ignore lint/suspicious/noTemplateCurlyInString: literal placeholder the user types into the URL pattern
const TITLE_PLACEHOLDER = "${title}"
// biome-ignore lint/suspicious/noTemplateCurlyInString: literal placeholder inside the example URL
const EXAMPLE_PATTERN = "https://readeck.example.com/bookmarks/unread?url=${url}"

export function CustomSharingSettings() {
    const settings = useAppSelector(state => state.user.settings)
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    // CustomSharingDestination has no id field (matches the DB schema), so rows need a
    // separate client-side-only stable key, kept in sync with the form's destinations array.
    const [rowKeys, setRowKeys] = useState<string[]>([])

    const form = useForm<FormValues>({
        initialValues: { destinations: [] },
        validate: {
            destinations: {
                name: value => (value.trim().length === 0 ? _(msg`Name is required`) : null),
                urlPattern: value => (/^https?:\/\//i.test(value) ? null : _(msg`Must start with http:// or https://`)),
                icon: value => (value ? null : _(msg`Icon is required`)),
            },
        },
    })
    // Sync local form state from Redux only when the settings object actually changes (initial
    // load, or after a save's reloadSettings()) - NOT on every render. `form.setValues` is only
    // referentially stable when the `validate` option passed to useForm is stable, which it
    // isn't here (a fresh object literal every render), so depending on it directly in a
    // useEffect deps array would re-run this sync (and clobber in-progress edits) on every
    // render instead of only when `settings` changes.
    const syncedSettingsRef = useRef<typeof settings>(undefined)
    // Set when this component triggers the reload itself: the response only echoes back what we
    // just saved, so re-seeding the form from it would discard anything typed while the request
    // was in flight.
    const skipNextSyncRef = useRef(false)
    useEffect(() => {
        if (settings && syncedSettingsRef.current !== settings) {
            syncedSettingsRef.current = settings
            if (skipNextSyncRef.current) {
                skipNextSyncRef.current = false
                return
            }
            // Copy each destination: redux-toolkit freezes state objects, and @mantine/form
            // mutates its values in place, so handing it the frozen objects directly makes
            // setFieldValue throw "TypeError: <prop> is read-only" on the first edit.
            form.setValues({ destinations: settings.customSharingDestinations.map(d => ({ ...d })) })
            // Reuse existing keys when the row count is unchanged (the usual case after a save
            // triggers reloadSettings) - regenerating them would remount every row for nothing.
            setRowKeys(keys =>
                keys.length === settings.customSharingDestinations.length
                    ? keys
                    : settings.customSharingDestinations.map(() => crypto.randomUUID())
            )
        }
    })

    const addDestination = () => {
        form.insertListItem("destinations", { name: "", urlPattern: "", icon: "" })
        setRowKeys(keys => [...keys, crypto.randomUUID()])
    }

    const removeDestination = (index: number) => {
        form.removeListItem("destinations", index)
        setRowKeys(keys => keys.filter((_key, i) => i !== index))
    }

    const save = useAsyncCallback(
        async (destinations: CustomSharingDestination[]) => {
            if (!settings) return
            const updated: Settings = { ...settings, customSharingDestinations: destinations }
            await client.user.saveSettings(updated)
        },
        {
            onSuccess: () => {
                skipNextSyncRef.current = true
                dispatch(reloadSettings())
            },
        }
    )

    return (
        <form onSubmit={form.onSubmit(values => save.execute(values.destinations))}>
            <Stack>
                {save.error && <Alert level="error" messages={errorToStrings(save.error)} />}

                <Stack gap={4}>
                    <Text size="sm" c="dimmed">
                        <Trans>
                            Define your own share destinations. Sharing an entry opens the URL pattern in a new window, with these
                            placeholders replaced by the entry's values:
                        </Trans>{" "}
                        <Code>{URL_PLACEHOLDER}</Code> <Code>{TITLE_PLACEHOLDER}</Code>
                    </Text>
                    <Text size="sm" c="dimmed">
                        <Trans>
                            The rest of the pattern depends on the service you are using - check its documentation for the address that adds
                            a link. For example, Readeck:
                        </Trans>{" "}
                        <Code>{EXAMPLE_PATTERN}</Code>
                    </Text>
                </Stack>

                {form.values.destinations.map((destination, index) => (
                    <Group key={rowKeys[index] ?? index} align="flex-start" wrap="nowrap">
                        <IconPicker value={destination.icon} onChange={icon => form.setFieldValue(`destinations.${index}.icon`, icon)} />
                        <TextInput placeholder={_(msg`Name`)} style={{ flex: 1 }} {...form.getInputProps(`destinations.${index}.name`)} />
                        <TextInput
                            placeholder="https://example.com/save?url=${url}&title=${title}"
                            style={{ flex: 2 }}
                            {...form.getInputProps(`destinations.${index}.urlPattern`)}
                        />
                        <ActionIcon variant="subtle" color="red" onClick={() => removeDestination(index)}>
                            <TbTrash size={18} />
                        </ActionIcon>
                    </Group>
                ))}

                <Group>
                    <Button variant="default" leftSection={<TbPlus size={16} />} onClick={addDestination}>
                        <Trans>Add destination</Trans>
                    </Button>
                </Group>

                <Group>
                    <Button type="submit" leftSection={<TbDeviceFloppy size={16} />} loading={save.loading}>
                        <Trans>Save</Trans>
                    </Button>
                </Group>
            </Stack>
        </form>
    )
}
