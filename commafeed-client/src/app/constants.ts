import { t } from "@lingui/macro"
import { DEFAULT_THEME } from "@mantine/core"
import { Category } from "./types"

const categories: { [key: string]: Category } = {
    all: {
        id: "all",
        name: t`All`,
        expanded: false,
        children: [],
        feeds: [],
        position: 0,
    },
    starred: {
        id: "starred",
        name: t`Starred`,
        expanded: false,
        children: [],
        feeds: [],
        position: 1,
    },
}
export const Constants = {
    categories,
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
