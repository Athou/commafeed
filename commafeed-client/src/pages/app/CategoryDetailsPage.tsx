import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Button, Code, Container, Divider, Group, Input, NumberInput, Stack, Text, TextInput, Title } from "@mantine/core"
import { useForm } from "@mantine/form"
import { openConfirmModal } from "@mantine/modals"
import { client, errorToStrings } from "app/client"
import { Constants } from "app/constants"
import { redirectToRootCategory, redirectToSelectedSource } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { reloadTree } from "app/tree/thunks"
import type { CategoryModificationRequest } from "app/types"
import { flattenCategoryTree } from "app/utils"
import { Alert } from "components/Alert"
import { Loader } from "components/Loader"
import { CategorySelect } from "components/content/add/CategorySelect"
import { useEffect } from "react"
import { useAsync, useAsyncCallback } from "react-async-hook"
import { TbDeviceFloppy, TbTrash } from "react-icons/tb"
import { useParams } from "react-router-dom"

export function CategoryDetailsPage() {
    const { id = Constants.categories.all.id } = useParams()
    const { _ } = useLingui()

    const apiKey = useAppSelector(state => state.user.profile?.apiKey)
    const dispatch = useAppDispatch()

    const query = useAsync(async () => await client.category.getRoot(), [])
    const category =
        id === Constants.categories.starred.id
            ? { ...Constants.categories.starred, name: _(msg`Starred`) }
            : query.result && flattenCategoryTree(query.result.data).find(c => c.id === id)

    const form = useForm<CategoryModificationRequest>()
    const { setValues } = form

    const modifyCategory = useAsyncCallback(client.category.modify, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToSelectedSource())
        },
    })
    const deleteCategory = useAsyncCallback(client.category.delete, {
        onSuccess: () => {
            dispatch(reloadTree())
            dispatch(redirectToRootCategory())
        },
    })

    const openDeleteCategoryModal = () => {
        const categoryName = category?.name
        openConfirmModal({
            title: <Trans>Delete Category</Trans>,
            children: (
                <Text size="sm">
                    <Trans>
                        Are you sure you want to delete category <Code>{categoryName}</Code>?
                    </Trans>
                </Text>
            ),
            labels: { confirm: <Trans>Confirm</Trans>, cancel: <Trans>Cancel</Trans> },
            confirmProps: { color: "red" },
            onConfirm: async () => await deleteCategory.execute({ id: +id }),
        })
    }

    useEffect(() => {
        if (!category?.id) return
        setValues({
            id: +category.id,
            name: category.name,
            parentId: category.parentId,
            position: category.position,
        })
    }, [setValues, category?.id, category?.name, category?.parentId, category?.position])

    const editable = id !== Constants.categories.all.id && id !== Constants.categories.starred.id
    if (!category) return <Loader />
    return (
        <Container className={"cf-CategoryDetails-Container"}>
            {modifyCategory.error && (
                <Box className={"cf-CategoryDetails-Container-Box1"} mb="md">
                    <Alert messages={errorToStrings(modifyCategory.error)} />
                </Box>
            )}

            {deleteCategory.error && (
                <Box className={"cf-CategoryDetails-Container-Box2"} mb="md">
                    <Alert messages={errorToStrings(deleteCategory.error)} />
                </Box>
            )}

            <form className={"cf-CategoryDetails-Container-Form"} onSubmit={form.onSubmit(modifyCategory.execute)}>
                <Stack>
                    <Title order={3}>{category.name}</Title>
                    <Input.Wrapper label={<Trans>Generated feed url</Trans>}>
                        <Box>
                            {apiKey && (
                                <Anchor
                                    href={`rest/category/entriesAsFeed?id=${category.id}&apiKey=${apiKey}`}
                                    target="_blank"
                                    rel="noreferrer"
                                >
                                    <Trans>Link</Trans>
                                </Anchor>
                            )}
                            {!apiKey && <Trans>Generate an API key in your profile first.</Trans>}
                        </Box>
                    </Input.Wrapper>

                    {editable && (
                        <>
                            <Divider />

                            <TextInput label={<Trans>Name</Trans>} {...form.getInputProps("name")} required />
                            <CategorySelect
                                label={<Trans>Parent Category</Trans>}
                                {...form.getInputProps("parentId")}
                                clearable
                                withoutCategoryIds={[id]}
                            />
                            <NumberInput label={<Trans>Position</Trans>} {...form.getInputProps("position")} required min={0} />
                        </>
                    )}

                    <Group>
                        <Button variant="default" onClick={async () => await dispatch(redirectToSelectedSource())}>
                            <Trans>Cancel</Trans>
                        </Button>
                        {editable && (
                            <>
                                <Button type="submit" leftSection={<TbDeviceFloppy size={16} />} loading={modifyCategory.loading}>
                                    <Trans>Save</Trans>
                                </Button>
                                <Divider orientation="vertical" />
                                <Button
                                    color="red"
                                    leftSection={<TbTrash size={16} />}
                                    onClick={() => openDeleteCategoryModal()}
                                    loading={deleteCategory.loading}
                                >
                                    <Trans>Delete</Trans>
                                </Button>
                            </>
                        )}
                    </Group>
                </Stack>
            </form>
        </Container>
    )
}
