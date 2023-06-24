import { throttle } from "throttle-debounce"
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

export const scrollToWithCallback = ({ options, onScrollEnded }: { options: ScrollToOptions; onScrollEnded: () => void }) => {
    const offset = (options.top ?? 0).toFixed()

    const onScroll = () => {
        if (window.scrollY.toFixed() === offset) {
            window.removeEventListener("scroll", throttleListener)
            onScrollEnded()
        }
    }
    const throttleListener = throttle(100, onScroll)
    window.addEventListener("scroll", throttleListener)

    // scrollTo does not trigger if there's nothing to do, trigger it manually
    throttleListener()

    window.scrollTo(options)
}

export const truncate = (str: string, n: number) => (str.length > n ? `${str.slice(0, n - 1)}\u2026` : str)
