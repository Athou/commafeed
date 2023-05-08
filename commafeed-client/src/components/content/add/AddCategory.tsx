import { t, Trans } from "@lingui/macro"
import { Box, Button, Group, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { AddCategoryRequest } from "app/types"
import { Alert } from "components/Alert"
import { useAsyncCallback } from "react-async-hook"
import { TbFolderPlus } from "react-icons/tb"
import { CategorySelect } from "./CategorySelect"

export function AddCategory() {
    const dispatch = useAppDispatch()

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
                    <TextInput label={<Trans>Category</Trans>} placeholder={t`Category`} {...form.getInputProps("name")} required />
                    <CategorySelect label={<Trans>Parent</Trans>} {...form.getInputProps("parentId")} clearable />
                    <Group position="center">
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbFolderPlus size={16} />} loading={addCategory.loading}>
                            <Trans>Add</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
