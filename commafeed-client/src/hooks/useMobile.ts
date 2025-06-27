import { useMediaQuery } from "@mantine/hooks"
import { Constants } from "@/app/constants"

export const useMobile = (breakpoint: string | number = Constants.layout.mobileBreakpoint) => {
    const bp = typeof breakpoint === "number" ? `${breakpoint}px` : breakpoint
    return !useMediaQuery(`(min-width: ${bp})`, undefined, {
        getInitialValueInEffect: false,
    })
}
