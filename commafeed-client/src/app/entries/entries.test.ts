import { configureStore } from "@reduxjs/toolkit"
import type { AxiosResponse } from "axios"
import { beforeEach, describe, expect, it, vi } from "vitest"
import { client } from "@/app/client"
import { loadEntries, loadMoreEntries, markAllEntries, markEntry } from "@/app/entries/thunks"
import { type RootState, reducers } from "@/app/store"
import type { Entries, Entry } from "@/app/types"

vi.mock(import("@/app/client"))

describe("entries", () => {
    beforeEach(() => {
        vi.resetAllMocks()
    })

    it("loads entries", async () => {
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

        const store = configureStore({ reducer: reducers })
        const promise = store.dispatch(
            loadEntries({
                source: { type: "feed", id: "feed-id" },
                clearSearch: true,
            })
        )

        expect(store.getState().entries.source.type).toBe("feed")
        expect(store.getState().entries.source.id).toBe("feed-id")
        expect(store.getState().entries.entries).toStrictEqual([])
        expect(store.getState().entries.hasMore).toBe(true)
        expect(store.getState().entries.sourceLabel).toBe("")
        expect(store.getState().entries.sourceWebsiteUrl).toBe("")
        expect(store.getState().entries.timestamp).toBeUndefined()

        await promise
        expect(store.getState().entries.source.type).toBe("feed")
        expect(store.getState().entries.source.id).toBe("feed-id")
        expect(store.getState().entries.entries).toStrictEqual([{ id: "3" }])
        expect(store.getState().entries.hasMore).toBe(false)
        expect(store.getState().entries.sourceLabel).toBe("my-feed")
        expect(store.getState().entries.sourceWebsiteUrl).toBe("https://mysite.com/feed")
        expect(store.getState().entries.timestamp).toBe(123)
    })

    it("loads more entries", async () => {
        vi.mocked(client.category.getEntries).mockResolvedValue({
            data: {
                entries: [{ id: "4" } as Entry],
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
                entries: {
                    source: {
                        type: "category",
                        id: "category-id",
                    },
                    sourceLabel: "",
                    sourceWebsiteUrl: "",
                    entries: [{ id: "3" } as Entry],
                    hasMore: true,
                    loading: false,
                    scrollingToEntry: false,
                },
            } as RootState,
        })
        const promise = store.dispatch(loadMoreEntries())

        await promise
        expect(store.getState().entries.entries).toStrictEqual([{ id: "3" }, { id: "4" }])
        expect(store.getState().entries.hasMore).toBe(false)
    })

    it("marks an entry as read", () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                entries: {
                    source: {
                        type: "category",
                        id: "category-id",
                    },
                    sourceLabel: "",
                    sourceWebsiteUrl: "",
                    entries: [{ id: "3", read: false } as Entry, { id: "4", read: false } as Entry],
                    hasMore: true,
                    loading: false,
                    scrollingToEntry: false,
                },
            } as RootState,
        })

        store.dispatch(markEntry({ entry: { id: "3" } as Entry, read: true }))
        expect(store.getState().entries.entries).toStrictEqual([
            { id: "3", read: true },
            { id: "4", read: false },
        ])
        expect(client.entry.mark).toHaveBeenCalledWith({ id: "3", read: true })
    })

    it("marks all entries as read", () => {
        const store = configureStore({
            reducer: reducers,
            preloadedState: {
                entries: {
                    source: {
                        type: "category",
                        id: "category-id",
                    },
                    sourceLabel: "",
                    sourceWebsiteUrl: "",
                    entries: [{ id: "3", read: false } as Entry, { id: "4", read: false } as Entry],
                    hasMore: true,
                    loading: false,
                    scrollingToEntry: false,
                },
            } as RootState,
        })

        store.dispatch(
            markAllEntries({
                sourceType: "category",
                req: { id: "all", read: true },
            })
        )
        expect(store.getState().entries.entries).toStrictEqual([
            { id: "3", read: true },
            { id: "4", read: true },
        ])
        expect(client.category.markEntries).toHaveBeenCalledWith({
            id: "all",
            read: true,
        })
    })
})
