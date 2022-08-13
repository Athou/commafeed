import { t, Trans } from "@lingui/macro"
import { Box, Button, Group, Stack, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { AddCategoryRequest } from "app/types"
import { Alert } from "components/Alert"
import { TbFolderPlus } from "react-icons/tb"
import useMutation from "use-mutation"
import { CategorySelect } from "./CategorySelect"

export function AddCategory() {
    const dispatch = useAppDispatch()

    const form = useForm<AddCategoryRequest>()

    const [addCategory, addCategoryResult] = useMutation(client.category.add, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })
    const errors = errorToStrings(addCategoryResult.error)

    return (
        <>
            {errors.length > 0 && (
                <Box mb="md">
                    <Alert messages={errors} />
                </Box>
            )}

            <form onSubmit={form.onSubmit(addCategory)}>
                <Stack>
                    <TextInput label={t`Category`} placeholder={t`Category`} {...form.getInputProps("name")} required />
                    <CategorySelect label={t`Parent`} {...form.getInputProps("parentId")} clearable />
                    <Group position="center">
                        <Button variant="default" onClick={() => dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        <Button type="submit" leftIcon={<TbFolderPlus size={16} />} loading={addCategoryResult.status === "running"}>
                            <Trans>Add</Trans>
                        </Button>
                    </Group>
                </Stack>
            </form>
        </>
    )
}
