/* eslint-disable import/first */
import { beforeEach, describe, expect, it, vi } from "vitest"
import { DeepMockProxy, mockDeep, mockReset } from "vitest-mock-extended"

vi.doMock("app/client", () => ({ client: mockDeep() }))

import { configureStore } from "@reduxjs/toolkit"
import { client } from "app/client"
import { reducers } from "app/store"
import { Entries, Entry } from "app/types"
import { AxiosResponse } from "axios"
import { loadEntries, loadMoreEntries, markAllEntries, markEntry } from "./entries"

describe("entries", () => {
    const mockClient = client as DeepMockProxy<typeof client>

    beforeEach(() => {
        mockReset(mockClient)
    })

    it("loads entries", async () => {
        mockClient.feed.getEntries.mockResolvedValue({
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
                sourceType: "feed",
                req: {
                    id: "feed-id",
                },
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
        mockClient.category.getEntries.mockResolvedValue({
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
                },
            },
        })
        const promise = store.dispatch(loadMoreEntries())

        await promise
        expect(store.getState().entries.entries).toStrictEqual([{ id: "3" }, { id: "4" }])
        expect(store.getState().entries.hasMore).toBe(false)
    })

    it("marks an entry as read", async () => {
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
                },
            },
        })

        store.dispatch(markEntry({ entry: { id: "3" } as Entry, read: true }))
        expect(store.getState().entries.entries).toStrictEqual([
            { id: "3", read: true },
            { id: "4", read: false },
        ])
        expect(mockClient.entry.mark).toHaveBeenCalledWith({ id: "3", read: true })
    })

    it("marks all entries as read", async () => {
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
                },
            },
        })

        store.dispatch(markAllEntries({ sourceType: "category", req: { id: "all", read: true } }))
        expect(store.getState().entries.entries).toStrictEqual([
            { id: "3", read: true },
            { id: "4", read: true },
        ])
        expect(mockClient.category.markEntries).toHaveBeenCalledWith({ id: "all", read: true })
    })
})
