import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { Select, type SelectProps } from "@mantine/core"
import type { ComboboxItem } from "@mantine/core/lib/components/Combobox/Combobox.types"
import { Constants } from "@/app/constants"
import { useAppSelector } from "@/app/store"
import type { Category } from "@/app/types"
import { flattenCategoryTree } from "@/app/utils"

type CategorySelectProps = Partial<SelectProps> & {
    withAll?: boolean
    withoutCategoryIds?: string[]
}

export function CategorySelect(props: CategorySelectProps) {
    const rootCategory = useAppSelector(state => state.tree.rootCategory)
    const { _ } = useLingui()

    const categories = rootCategory && flattenCategoryTree(rootCategory)
    const categoriesById = categories?.reduce((map, c) => {
        map.set(c.id, c)
        return map
    }, new Map<string, Category>())
    const categoryLabel = (category: Category) => {
        let cat = category
        let label = cat.name

        while (cat.parentId) {
            const parent = categoriesById?.get(cat.parentId)
            if (!parent) {
                break
            }
            label = `${parent.name} → ${label}`
            cat = parent
        }

        return label
    }
    const selectData: ComboboxItem[] | undefined = categories
        ?.filter(c => c.id !== Constants.categories.all.id)
        .filter(c => !props.withoutCategoryIds?.includes(c.id))
        .map(c => ({
            label: categoryLabel(c),
            value: c.id,
        }))
        .sort((c1, c2) => c1.label.localeCompare(c2.label))
    if (props.withAll) {
        selectData?.unshift({
            label: _(msg`All`),
            value: Constants.categories.all.id,
        })
    }

    return <Select {...props} data={selectData ?? []} disabled={!selectData} />
}
