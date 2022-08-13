import { t, Trans } from "@lingui/macro"
import { Box, Button, FileInput, Group, Stack } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { Alert } from "components/Alert"
import { TbFileImport } from "react-icons/tb"
import useMutation from "use-mutation"

export function ImportOpml() {
    const dispatch = useAppDispatch()

    const form = useForm<{ file: File }>({
        validate: {
            file: v => (v ? null : t`file is required`),
        },
    })

    const [importOpml, importOpmlResult] = useMutation(client.feed.importOpml, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })
    const errors = errorToStrings(importOpmlResult.error)

    return (
        <>
            {errors.length > 0 && (
                <Box mb="md">
                    <Alert messages={errors} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(v => importOpml(v.file))}>
                <Stack>
                    <FileInput
                        label={t`OPML file`}
                        placeholder={t`OPML file`}
                        description={t`An opml file is an XML file containing feed URLs and categories. You can get an OPML file by exporting your data from other feed reading services.`}
                        {...form.getInputProps("file")}
                        required
                        accept="application/xml"
                    />
                    <Group position="center">
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbFileImport size={16} />} loading={importOpmlResult.status === "running"}>
                            <Trans>Import</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
