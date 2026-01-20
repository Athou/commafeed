import { msg } from "@lingui/core/macro"
import { useLingui } from "@lingui/react"
import { useAppSelector } from "@/app/store"

export function useValidationRules() {
    const minimumPasswordLength = useAppSelector(state => state.server.serverInfos?.minimumPasswordLength)
    const { _ } = useLingui()

    return {
        password: (value: string | undefined) =>
            value && minimumPasswordLength && value.length < minimumPasswordLength
                ? _(msg`Password must be at least ${minimumPasswordLength} characters`)
                : null,
        passwordConfirmation: (newPasswordConfirmation: string | undefined, newPassword: string | undefined) =>
            newPasswordConfirmation && newPasswordConfirmation !== newPassword ? _(msg`Passwords do not match`) : null,
    }
}
