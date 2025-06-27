import { configureStore } from "@reduxjs/toolkit"
import type { AxiosResponse } from "axios"
import { beforeEach, describe, expect, it, vi } from "vitest"
import { client } from "@/app/client"
import { loadEntries } from "@/app/entries/thunks"
import { type RootState, reducers } from "@/app/store"
import { newFeedEntriesDiscovered, selectNextUnreadTreeItem } from "@/app/tree/thunks"
import type { Category, Entries, Entry, Subscription } from "@/app/types"

vi.mock(import("@/app/client"))

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

describe("hasNewEntries", () => {
    beforeEach(() => {
        vi.resetAllMocks()
    })

    it("sets and clear flag for a feed", async () => {
        vi.mocked(client.feed.getEntries).mockResolvedValue({
            data: {
                entries: [{ id: "3" } as Entry],
                hasMore: false,
                name: "my-feed",
                errorCount: 3,
                feedLink: "https://mysite.com/feed",
                timestamp: 123,
                ignoredReadStatus: false,
            },
        } as AxiosResponse<Entries>)

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

        // initial state
        expect(store.getState().tree.rootCategory?.children[0].feeds[0].unread).toBe(0)
        expect(store.getState().tree.rootCategory?.children[0].feeds[0].hasNewEntries).toBeFalsy()

        // increments unread count and sets hasNewEntries to true
        await store.dispatch(newFeedEntriesDiscovered({ feedId: 1, amount: 3 }))
        expect(store.getState().tree.rootCategory?.children[0].feeds[0].unread).toBe(3)
        expect(store.getState().tree.rootCategory?.children[0].feeds[0].hasNewEntries).toBe(true)

        // reload entries and sets hasNewEntries to false
        await store.dispatch(loadEntries({ source: { type: "feed", id: "1" }, clearSearch: true }))
        expect(store.getState().tree.rootCategory?.children[0].feeds[0].hasNewEntries).toBe(false)
    })
})
