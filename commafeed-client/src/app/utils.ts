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
