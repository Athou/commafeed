import { DEFAULT_THEME } from "@mantine/core"

export const Constants = {
    categoryIds: {
        all: "all",
    },
    layout: {
        mobileBreakpoint: DEFAULT_THEME.breakpoints.md,
        headerHeight: 60,
        sidebarWidth: 350,
        isTopVisible: (div: HTMLDivElement) => div.getBoundingClientRect().top >= Constants.layout.headerHeight,
        isBottomVisible: (div: HTMLDivElement) => div.getBoundingClientRect().bottom <= window.innerHeight,
    },
    dom: {
        mainScrollAreaId: "main-scroll-area-id",
    },
}
