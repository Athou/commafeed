import { useMantineTheme } from "@mantine/core"
import { useMobile } from "@/hooks/useMobile"

export const useActionButton = () => {
    const theme = useMantineTheme()
    const mobile = useMobile(theme.breakpoints.xl)
    const spacing = mobile ? 14 : 0
    return { mobile, spacing }
}
