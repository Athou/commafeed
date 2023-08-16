import { useMediaQuery } from "@mantine/hooks"
import { Constants } from "app/constants"

export const useMobile = (breakpoint: string = Constants.layout.mobileBreakpoint) =>
    !useMediaQuery(`(min-width: ${breakpoint})`, undefined, {
        getInitialValueInEffect: false,
    })
