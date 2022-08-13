import { Select, SelectItem, SelectProps } from "@mantine/core"
import { Constants } from "app/constants"
import { useAppSelector } from "app/store"
import { flattenCategoryTree } from "app/utils"

export function CategorySelect(props: Partial<SelectProps>) {
    const rootCategory = useAppSelector(state => state.tree.rootCategory)
    const categories = rootCategory && flattenCategoryTree(rootCategory)
    const selectData: SelectItem[] | undefined = categories
        ?.filter(c => c.id !== Constants.categoryIds.all)
        .sort((c1, c2) => c1.name.localeCompare(c2.name))
        .map(c => ({
            label: c.name,
            value: c.id,
        }))

    return <Select {...props} data={selectData ?? []} disabled={!selectData} />
}
