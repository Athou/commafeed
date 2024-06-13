// the color scheme to use to render components
import { useMantineColorScheme } from "@mantine/core"
import { useMediaQuery } from "@mantine/hooks"

export const useColorScheme = () => {
    const systemColorScheme = useMediaQuery(
        "(prefers-color-scheme: dark)",
        // passing undefined will use window.matchMedia(query) as default value
        undefined,
        {
            // get initial value synchronously and not in useEffect to avoid flash of light theme
            getInitialValueInEffect: false,
        }
    )
        ? "dark"
        : "light"

    const { colorScheme } = useMantineColorScheme()
    return colorScheme === "auto" ? systemColorScheme : colorScheme
}
