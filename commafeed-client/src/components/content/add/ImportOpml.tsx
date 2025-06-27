import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Box, Button, FileInput, Group, Stack } from "@mantine/core"
import { isNotEmpty, useForm } from "@mantine/form"
import { useAsyncCallback } from "react-async-hook"
import { TbFileImport } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { redirectToSelectedSource } from "@/app/redirect/thunks"
import { useAppDispatch } from "@/app/store"
import { reloadTree } from "@/app/tree/thunks"
import { Alert } from "@/components/Alert"

export function ImportOpml() {
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const form = useForm<{ file: File }>({
        validate: {
            file: isNotEmpty(_(msg`OPML file is required`)),
        },
    })

    const importOpml = useAsyncCallback(client.feed.importOpml, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })

    return (
        <>
            {importOpml.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(importOpml.error)} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(async v => await importOpml.execute(v.file))}>
                <Stack>
                    <FileInput
                        label={<Trans>OPML file</Trans>}
                        leftSection={<TbFileImport />}
                        placeholder={_(msg`OPML file`)}
                        description={
                            <Trans>
                                An opml file is an XML file containing feed URLs and categories. You can get an OPML file by exporting your
                                data from other feed reading services.
                            </Trans>
                        }
                        {...form.getInputProps("file")}
                        required
                        accept=".xml,.opml"
                    />
                    <Group justify="center">
                        <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftSection={<TbFileImport size={16} />} loading={importOpml.loading}>
                            <Trans>Import</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
