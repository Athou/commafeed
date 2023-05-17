import { Category } from "./types"

export function visitCategoryTree(category: Category, visitor: (category: Category) => void): void {
    visitor(category)
    category.children.forEach(child => visitCategoryTree(child, visitor))
}

export function flattenCategoryTree(category: Category): Category[] {
    const categories: Category[] = []
    visitCategoryTree(category, c => categories.push(c))
    return categories
}

export function categoryUnreadCount(category?: Category): number {
    if (!category) return 0

    return flattenCategoryTree(category)
        .flatMap(c => c.feeds)
        .map(f => f.unread)
        .reduce((total, current) => total + current, 0)
}

export const calculatePlaceholderSize = ({ width, height, maxWidth }: { width?: number; height?: number; maxWidth: number }) => {
    const placeholderWidth = width && Math.min(width, maxWidth)
    const placeholderHeight = height && width && width > maxWidth ? height * (maxWidth / width) : height
    return { width: placeholderWidth, height: placeholderHeight }
}

export const scrollToWithCallback = ({
    element,
    options,
    onScrollEnded,
}: {
    element: HTMLElement
    options: ScrollToOptions
    onScrollEnded: () => void
}) => {
    const offset = (options.top ?? 0).toFixed()

    const onScroll = () => {
        if (element.offsetTop.toFixed() === offset) {
            element.removeEventListener("scroll", onScroll)
            onScrollEnded()
        }
    }

    element.addEventListener("scroll", onScroll)

    // scrollTo does not trigger if there's nothing to do, trigger it manually
    onScroll()

    element.scrollTo(options)
}

export const openLinkInBackgroundTab = (url: string) => {
    // simulate ctrl+click to open tab in background
    const a = document.createElement("a")
    a.href = url
    a.rel = "noreferrer"
    a.dispatchEvent(
        new MouseEvent("click", {
            ctrlKey: true,
            metaKey: true,
        })
    )
}

export const truncate = (str: string, n: number) => (str.length > n ? `${str.slice(0, n - 1)}\u2026` : str)
