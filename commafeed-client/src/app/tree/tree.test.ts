import { configureStore } from "@reduxjs/toolkit"
import { type RootState, reducers } from "app/store"
import { selectNextUnreadTreeItem } from "app/tree/thunks"
import type { Category, Subscription } from "app/types"
import { describe, expect, it } from "vitest"

const createCategory = (id: string): Category => ({
    id,
    name: id,
    children: [],
    feeds: [],
    expanded: true,
    position: 0,
})

const createFeed = (id: number, unread: number): Subscription => ({
    id,
    name: String(id),
    unread,
    errorCount: 0,
    position: 0,
    feedUrl: "",
    feedLink: "",
    iconUrl: "",
})

const root = createCategory("root")

const catA = createCategory("catA")
catA.feeds.push(createFeed(1, 0), createFeed(2, 0), createFeed(3, 1))

const catB = createCategory("catB")

const catC = createCategory("catC")
catC.feeds.push(createFeed(4, 1))

root.children.push(catA, catB, catC)

describe("selectNextUnreadTreeItem", () => {
    it("selects the next unread category", async () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                tree: {
                    rootCategory: root,
                },
                entries: {
                    source: {
                        type: "category",
                        id: "catA",
                    },
                },
            } as RootState,
        })

        await store.dispatch(selectNextUnreadTreeItem({ direction: "forward" }))
        expect(store.getState().redirect.to).toBe("/app/category/catC")
    })

    it("selects the previous unread category", async () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                tree: {
                    rootCategory: root,
                },
                entries: {
                    source: {
                        type: "category",
                        id: "catC",
                    },
                },
            } as RootState,
        })

        await store.dispatch(selectNextUnreadTreeItem({ direction: "backward" }))
        expect(store.getState().redirect.to).toBe("/app/category/catA")
    })

    it("selects the next unread feed", async () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                tree: {
                    rootCategory: root,
                },
                entries: {
                    source: {
                        type: "feed",
                        id: "1",
                    },
                },
            } as RootState,
        })

        await store.dispatch(selectNextUnreadTreeItem({ direction: "forward" }))
        expect(store.getState().redirect.to).toBe("/app/feed/3")
    })

    it("selects the previous unread feed", async () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                tree: {
                    rootCategory: root,
                },
                entries: {
                    source: {
                        type: "feed",
                        id: "4",
                    },
                },
            } as RootState,
        })

        await store.dispatch(selectNextUnreadTreeItem({ direction: "backward" }))
        expect(store.getState().redirect.to).toBe("/app/feed/3")
    })
})
