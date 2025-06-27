import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Box, Button, Group, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useAsyncCallback } from "react-async-hook"
import { TbFolderPlus } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { redirectToSelectedSource } from "@/app/redirect/thunks"
import { useAppDispatch } from "@/app/store"
import { reloadTree } from "@/app/tree/thunks"
import type { AddCategoryRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
import { CategorySelect } from "./CategorySelect"

export function AddCategory() {
    const dispatch = useAppDispatch()
    const { _ } = useLingui()

    const form = useForm<AddCategoryRequest>()

    const addCategory = useAsyncCallback(client.category.add, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })

    return (
        <>
            {addCategory.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(addCategory.error)} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(addCategory.execute)}>
                <Stack>
                    <TextInput label={<Trans>Category</Trans>} placeholder={_(msg`Category`)} {...form.getInputProps("name")} required />
                    <CategorySelect label={<Trans>Parent</Trans>} {...form.getInputProps("parentId")} clearable />
                    <Group justify="center">
                        <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftSection={<TbFolderPlus size={16} />} loading={addCategory.loading}>
                            <Trans>Add</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
