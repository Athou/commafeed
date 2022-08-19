import { t } from "@lingui/macro"
import { Select, SelectItem, SelectProps } from "@mantine/core"
import { Constants } from "app/constants"
import { useAppSelector } from "app/store"
import { flattenCategoryTree } from "app/utils"

type CategorySelectProps = Partial<SelectProps> & { withAll?: boolean }

export function CategorySelect(props: CategorySelectProps) {
    const rootCategory = useAppSelector(state => state.tree.rootCategory)
    const categories = rootCategory && flattenCategoryTree(rootCategory)
    const selectData: SelectItem[] | undefined = categories
        ?.filter(c => c.id !== Constants.categories.all.id)
        .sort((c1, c2) => c1.name.localeCompare(c2.name))
        .map(c => ({
            label: c.name,
            value: c.id,
        }))
    if (props.withAll) {
        selectData?.unshift({
            label: t`All`,
            value: Constants.categories.all.id,
        })
    }

    return <Select {...props} data={selectData ?? []} disabled={!selectData} />
}
