import { t, Trans } from "@lingui/macro"
import { Box, Button, FileInput, Group, Stack } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { Alert } from "components/Alert"
import { useAsyncCallback } from "react-async-hook"
import { TbFileImport } from "react-icons/tb"

export function ImportOpml() {
    const dispatch = useAppDispatch()

    const form = useForm<{ file: File }>({
        validate: {
            file: v => (v ? null : t`file is required`),
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

            <form onSubmit={form.onSubmit(v => importOpml.execute(v.file))}>
                <Stack>
                    <FileInput
                        label={<Trans>OPML file</Trans>}
                        placeholder={t`OPML file`}
                        description={
                            <Trans>
                                An opml file is an XML file containing feed URLs and categories. You can get an OPML file by exporting your
                                data from other feed reading services.
                            </Trans>
                        }
                        {...form.getInputProps("file")}
                        required
                        accept="application/xml"
                    />
                    <Group position="center">
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbFileImport size={16} />} loading={importOpml.loading}>
                            <Trans>Import</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
