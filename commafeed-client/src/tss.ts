import { useMantineTheme } from "@mantine/core"
import { useColorScheme } from "hooks/useColorScheme"
import { createTss } from "tss-react"

const useContext = () => {
    // return anything here that will be accessible in tss.create()

    const theme = useMantineTheme()
    const colorScheme = useColorScheme()

    return { theme, colorScheme }
}

export const { tss } = createTss({ useContext })
