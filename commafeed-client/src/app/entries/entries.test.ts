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

    it("stops loading after an initial-load failure and allows a retry", async () => {
        const response = {
            data: {
                entries: [{ id: "3" } as Entry],
                hasMore: true,
                name: "my-feed",
                feedLink: "https://mysite.com/feed",
                timestamp: 123,
            },
        } as AxiosResponse<Entries>
        vi.mocked(client.feed.getEntries)
            .mockRejectedValueOnce(new Error("Network error"))
            .mockResolvedValueOnce(response)

        const store = configureStore({ reducer: reducers })
        const arg = { source: { type: "feed" as const, id: "feed-id" }, clearSearch: true }
        const request = store.dispatch(loadEntries(arg))

        expect(store.getState().entries.loading).toBe(true)
        expect(loadEntries.rejected.match(await request)).toBe(true)
        expect(store.getState().entries.loading).toBe(false)
        expect(store.getState().entries.entries).toStrictEqual([])
        expect(store.getState().entries.hasMore).toBe(true)

        expect(loadEntries.fulfilled.match(await store.dispatch(loadEntries(arg)))).toBe(true)
        expect(store.getState().entries.entries).toStrictEqual(response.data.entries)
        expect(store.getState().entries.loading).toBe(false)
    })

    it("preserves entries after a pagination failure and allows a retry", async () => {
        const response = {
            data: {
                entries: [{ id: "3", read: false } as Entry],
                hasMore: true,
                name: "my-feed",
                feedLink: "https://mysite.com/feed",
                timestamp: 123,
            },
        } as AxiosResponse<Entries>
        vi.mocked(client.feed.getEntries)
            .mockResolvedValueOnce(response)
            .mockRejectedValueOnce(new Error("Network error"))
            .mockResolvedValueOnce({
                data: { ...response.data, entries: [{ id: "4" } as Entry], hasMore: false },
            } as AxiosResponse<Entries>)

        const store = configureStore({ reducer: reducers })
        await store.dispatch(loadEntries({ source: { type: "feed", id: "feed-id" }, clearSearch: true }))
        const previousState = store.getState().entries
        const request = store.dispatch(loadMoreEntries())

        expect(store.getState().entries.loading).toBe(true)
        expect(loadMoreEntries.rejected.match(await request)).toBe(true)
        expect(store.getState().entries.loading).toBe(false)
        expect(store.getState().entries.entries).toStrictEqual(previousState.entries)
        expect(store.getState().entries.hasMore).toBe(true)
        expect(store.getState().entries.timestamp).toBe(123)
        expect(store.getState().entries.sourceLabel).toBe("my-feed")
        expect(store.getState().entries.sourceWebsiteUrl).toBe("https://mysite.com/feed")

        expect(loadMoreEntries.fulfilled.match(await store.dispatch(loadMoreEntries()))).toBe(true)
        expect(store.getState().entries.entries).toStrictEqual([{ id: "3", read: false }, { id: "4" }])
        expect(store.getState().entries.hasMore).toBe(false)
        expect(store.getState().entries.loading).toBe(false)
        expect(vi.mocked(client.feed.getEntries).mock.calls[1][0]).toEqual(
            vi.mocked(client.feed.getEntries).mock.calls[2][0]
        )
    })

    describe.each(["initial load", "pagination"] as const)("overlapping %s requests", olderRequestType => {
        it.each(["initial load", "pagination"] as const)(
            "ignores an older failure while a newer %s is pending",
            async newerRequestType => {
                let rejectOlder!: (reason: Error) => void
                vi.mocked(client.category.getEntries).mockReturnValueOnce(
                    new Promise<AxiosResponse<Entries>>((_, reject) => {
                        rejectOlder = reject
                    })
                )
                vi.mocked(client.category.getEntries).mockReturnValueOnce(new Promise<AxiosResponse<Entries>>(() => {}))

                const store = configureStore({ reducer: reducers })
                const arg = { source: { type: "category" as const, id: "all" }, clearSearch: true }
                const older =
                    olderRequestType === "initial load"
                        ? store.dispatch(loadEntries(arg))
                        : store.dispatch(loadMoreEntries())
                const newer =
                    newerRequestType === "initial load"
                        ? store.dispatch(loadEntries(arg))
                        : store.dispatch(loadMoreEntries())

                rejectOlder(new Error("Older request failed"))
                await older
                expect(store.getState().entries.loading).toBe(true)

                newer.abort()
                await newer
                expect(store.getState().entries.loading).toBe(false)
            }
        )

        it("does not clear a newer request's loading state when an older request succeeds", async () => {
            let resolveOlder!: (response: AxiosResponse<Entries>) => void
            vi.mocked(client.category.getEntries).mockReturnValueOnce(
                new Promise<AxiosResponse<Entries>>(resolve => {
                    resolveOlder = resolve
                })
            )
            vi.mocked(client.category.getEntries).mockReturnValueOnce(new Promise<AxiosResponse<Entries>>(() => {}))

            const store = configureStore({ reducer: reducers })
            const arg = { source: { type: "category" as const, id: "all" }, clearSearch: true }
            const older =
                olderRequestType === "initial load"
                    ? store.dispatch(loadEntries(arg))
                    : store.dispatch(loadMoreEntries())
            const newer = store.dispatch(loadEntries(arg))

            resolveOlder({
                data: {
                    entries: [] as Entry[],
                    hasMore: true,
                    name: "old-category",
                    feedLink: "",
                    timestamp: 123,
                    errorCount: 0,
                    ignoredReadStatus: false,
                },
            } as AxiosResponse<Entries>)
            await older
            expect(store.getState().entries.loading).toBe(true)

            newer.abort()
            await newer
            expect(store.getState().entries.loading).toBe(false)
        })

        it("ignores an older failure after the newer request succeeds", async () => {
            let rejectOlder!: (reason: Error) => void
            vi.mocked(client.category.getEntries).mockReturnValueOnce(
                new Promise<AxiosResponse<Entries>>((_, reject) => {
                    rejectOlder = reject
                })
            )
            vi.mocked(client.category.getEntries).mockResolvedValueOnce({
                data: { entries: [{ id: "4" } as Entry], hasMore: false, name: "new-category", timestamp: 456 },
            } as AxiosResponse<Entries>)

            const store = configureStore({ reducer: reducers })
            const arg = { source: { type: "category" as const, id: "all" }, clearSearch: true }
            const older =
                olderRequestType === "initial load"
                    ? store.dispatch(loadEntries(arg))
                    : store.dispatch(loadMoreEntries())
            await store.dispatch(loadEntries(arg))
            const completedState = store.getState().entries

            rejectOlder(new Error("Older request failed"))
            await older
            expect(store.getState().entries).toStrictEqual(completedState)
        })
    })

    it("does not append entries already present when loading more entries", async () => {
        vi.mocked(client.category.getEntries)
            .mockResolvedValueOnce({
                data: { entries: [{ id: "3" } as Entry], hasMore: true },
            } as AxiosResponse<Entries>)
            .mockResolvedValueOnce({
                data: { entries: [{ id: "3" } as Entry, { id: "4" } as Entry], hasMore: false },
            } as AxiosResponse<Entries>)

        const store = configureStore({ reducer: reducers })
        await store.dispatch(loadEntries({ source: { type: "category", id: "all" }, clearSearch: true }))
        await store.dispatch(loadMoreEntries())

        expect(store.getState().entries.entries).toStrictEqual([{ id: "3" }, { id: "4" }])
        expect(store.getState().entries.loading).toBe(false)
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

    it("flags an entry marked as unread by the user until entries are reloaded", async () => {
        vi.mocked(client.feed.getEntries).mockResolvedValue({
            data: {
                entries: [{ id: "3", read: true, markable: true } as Entry],
                hasMore: false,
                name: "my-feed",
                errorCount: 0,
                feedLink: "https://mysite.com/feed",
                timestamp: 123,
                ignoredReadStatus: false,
            },
        } as AxiosResponse<Entries>)

        const store = configureStore({ reducer: reducers })
        await store.dispatch(loadEntries({ source: { type: "feed", id: "feed-id" }, clearSearch: true }))

        store.dispatch(markEntry({ entry: store.getState().entries.entries[0], read: false }))
        expect(store.getState().entries.entries[0].keepUnread).toBe(true)

        // reloading the entry list resets the flag
        await store.dispatch(loadEntries({ source: { type: "feed", id: "feed-id" }, clearSearch: false }))
        expect(store.getState().entries.entries[0].keepUnread).toBeUndefined()
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
