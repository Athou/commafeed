import { t } from "@lingui/macro"
import { useAppSelector } from "app/store"

interface Step {
    label: string
    done: boolean
}

export const useAppLoading = () => {
    const profile = useAppSelector(state => state.user.profile)
    const settings = useAppSelector(state => state.user.settings)
    const rootCategory = useAppSelector(state => state.tree.rootCategory)

    const steps: Step[] = [
        {
            label: t`Loading settings...`,
            done: !!settings,
        },
        {
            label: t`Loading profile...`,
            done: !!profile,
        },
        {
            label: t`Loading subscriptions...`,
            done: !!rootCategory,
        },
    ]

    const loading = steps.some(s => !s.done)
    const loadingPercentage = Math.round((100.0 * steps.filter(s => s.done).length) / steps.length)
    const loadingStepLabel = steps.find(s => !s.done)?.label

    return { steps, loading, loadingPercentage, loadingStepLabel }
}
