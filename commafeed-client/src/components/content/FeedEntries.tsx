import { Constants } from "app/constants"
import {
    loadMoreEntries,
    markAllEntries,
    markEntry,
    reloadEntries,
    selectEntry,
    selectNextEntry,
    selectPreviousEntry,
} from "app/slices/entries"
import { redirectToRootCategory } from "app/slices/redirect"
import { useAppDispatch, useAppSelector } from "app/store"
import { Loader } from "components/Loader"
import { useMousetrap } from "hooks/useMousetrap"
import { useEffect, useRef } from "react"
import InfiniteScroll from "react-infinite-scroller"
import { FeedEntry } from "./FeedEntry"

export function FeedEntries() {
    const source = useAppSelector(state => state.entries.source)
    const entries = useAppSelector(state => state.entries.entries)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp)
    const selectedEntryId = useAppSelector(state => state.entries.selectedEntryId)
    const hasMore = useAppSelector(state => state.entries.hasMore)
    const dispatch = useAppDispatch()

    const selectedEntry = entries.find(e => e.id === selectedEntryId)

    // references to entries html elements
    const refs = useRef<{ [id: string]: HTMLDivElement }>({})
    useEffect(() => {
        // remove refs that are not in entries anymore
        Object.keys(refs.current).forEach(k => {
            const found = entries.some(e => e.id === k)
            if (!found) delete refs.current[k]
        })
    }, [entries])

    useMousetrap("r", () => {
        dispatch(reloadEntries())
    })
    useMousetrap("j", () => {
        dispatch(selectNextEntry())
    })
    useMousetrap("k", () => {
        dispatch(selectPreviousEntry())
    })
    useMousetrap("space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const ref = refs.current[selectedEntry.id]
                const bottomVisible = ref.getBoundingClientRect().bottom <= window.innerHeight
                if (bottomVisible) {
                    dispatch(selectNextEntry())
                } else {
                    const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)
                    scrollArea?.scrollTo({
                        top: scrollArea.scrollTop + scrollArea.clientHeight * 0.8,
                        behavior: "smooth",
                    })
                }
            } else {
                dispatch(selectEntry(selectedEntry))
            }
        } else {
            dispatch(selectNextEntry())
        }
    })
    useMousetrap("shift+space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const ref = refs.current[selectedEntry.id]
                const topVisible = ref.getBoundingClientRect().top >= Constants.layout.headerHeight
                if (topVisible) {
                    dispatch(selectPreviousEntry())
                } else {
                    const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)
                    scrollArea?.scrollTo({
                        top: scrollArea.scrollTop - scrollArea.clientHeight * 0.8,
                        behavior: "smooth",
                    })
                }
            } else {
                dispatch(selectPreviousEntry())
            }
        }
    })
    useMousetrap(["o", "enter"], () => {
        // toggle expanded status
        if (!selectedEntry) return
        dispatch(selectEntry(selectedEntry))
    })
    useMousetrap("v", () => {
        // open tab in foreground
        if (!selectedEntry) return
        window.open(selectedEntry.url, "_blank", "noreferrer")
    })
    useMousetrap("b", () => {
        // simulate ctrl+click to open tab in background
        if (!selectedEntry) return
        const a = document.createElement("a")
        a.href = selectedEntry.url
        a.rel = "noreferrer"
        a.dispatchEvent(
            new MouseEvent("click", {
                ctrlKey: true,
                metaKey: true,
            })
        )
    })
    useMousetrap("m", () => {
        // toggle read status
        if (!selectedEntry) return
        dispatch(markEntry({ entry: selectedEntry, read: !selectedEntry.read }))
    })
    useMousetrap("shift+a", () => {
        // mark all entries as read
        dispatch(
            markAllEntries({
                sourceType: source.type,
                req: {
                    id: source.id,
                    read: true,
                    olderThan: entriesTimestamp,
                },
            })
        )
    })
    useMousetrap("g a", () => {
        dispatch(redirectToRootCategory())
    })

    if (!entries) return <Loader />
    return (
        <InfiniteScroll
            initialLoad={false}
            loadMore={() => dispatch(loadMoreEntries())}
            hasMore={hasMore}
            loader={<Loader key={0} />}
            useWindow={false}
            getScrollParent={() => document.getElementById(Constants.dom.mainScrollAreaId)}
        >
            {entries.map(e => (
                <div
                    key={e.id}
                    ref={el => {
                        refs.current[e.id] = el!
                    }}
                >
                    <FeedEntry entry={e} expanded={!!e.expanded} />
                </div>
            ))}
        </InfiniteScroll>
    )
}
