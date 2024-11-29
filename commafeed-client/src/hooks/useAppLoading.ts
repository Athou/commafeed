import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { useAppSelector } from "app/store"

interface Step {
    label: string
    done: boolean
}

export const useAppLoading = () => {
    const profile = useAppSelector(state => state.user.profile)
    const settings = useAppSelector(state => state.user.settings)
    const rootCategory = useAppSelector(state => state.tree.rootCategory)
    const tags = useAppSelector(state => state.user.tags)
    const { _ } = useLingui()

    const steps: Step[] = [
        {
            label: _(msg`Loading settings...`),
            done: !!settings,
        },
        {
            label: _(msg`Loading profile...`),
            done: !!profile,
        },
        {
            label: _(msg`Loading subscriptions...`),
            done: !!rootCategory,
        },
        {
            label: _(msg`Loading tags...`),
            done: !!tags,
        },
    ]

    const loading = steps.some(s => !s.done)
    const loadingPercentage = Math.round((100.0 * steps.filter(s => s.done).length) / steps.length)
    const loadingStepLabel = steps.find(s => !s.done)?.label

    return { steps, loading, loadingPercentage, loadingStepLabel }
}
