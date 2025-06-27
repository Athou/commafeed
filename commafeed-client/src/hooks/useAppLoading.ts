import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { useAppSelector } from "@/app/store"

interface Step {
    label: string
    done: boolean
}

export const useAppLoading = () => {
    const profileLoaded = useAppSelector(state => !!state.user.profile)
    const settingsLoaded = useAppSelector(state => !!state.user.settings)
    const rootCategoryLoaded = useAppSelector(state => !!state.tree.rootCategory)
    const tagsLoaded = useAppSelector(state => !!state.user.tags)
    const { _ } = useLingui()

    const steps: Step[] = [
        {
            label: _(msg`Loading settings...`),
            done: settingsLoaded,
        },
        {
            label: _(msg`Loading profile...`),
            done: profileLoaded,
        },
        {
            label: _(msg`Loading subscriptions...`),
            done: rootCategoryLoaded,
        },
        {
            label: _(msg`Loading tags...`),
            done: tagsLoaded,
        },
    ]

    const loading = steps.some(s => !s.done)
    const loadingPercentage = Math.round((100.0 * steps.filter(s => s.done).length) / steps.length)
    const loadingStepLabel = steps.find(s => !s.done)?.label

    return { steps, loading, loadingPercentage, loadingStepLabel }
}
