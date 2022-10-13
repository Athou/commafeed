import { t } from "@lingui/macro"
import { openModal } from "@mantine/modals"
import { Constants } from "app/constants"
import {
    ExpendableEntry,
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
import { KeyboardShortcutsHelp } from "components/KeyboardShortcutsHelp"
import { Loader } from "components/Loader"
import { useMousetrap } from "hooks/useMousetrap"
import throttle from "lodash/throttle"
import { useEffect } from "react"
import InfiniteScroll from "react-infinite-scroller"
import { FeedEntry } from "./FeedEntry"

export function FeedEntries() {
    const source = useAppSelector(state => state.entries.source)
    const entries = useAppSelector(state => state.entries.entries)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp)
    const selectedEntryId = useAppSelector(state => state.entries.selectedEntryId)
    const hasMore = useAppSelector(state => state.entries.hasMore)
    const viewMode = useAppSelector(state => state.user.settings?.viewMode)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const scrollingToEntry = useAppSelector(state => state.entries.scrollingToEntry)
    const dispatch = useAppDispatch()

    const selectedEntry = entries.find(e => e.id === selectedEntryId)

    const headerClicked = (entry: ExpendableEntry, event: React.MouseEvent) => {
        if (event.button === 1 || event.ctrlKey || event.metaKey) {
            // middle click
            dispatch(markEntry({ entry, read: true }))
        } else if (event.button === 0) {
            // main click
            // don't trigger the link
            event.preventDefault()

            dispatch(
                selectEntry({
                    entry,
                    expand: !entry.expanded,
                    markAsRead: !entry.expanded,
                    scrollToEntry: true,
                })
            )
        }
    }

    useEffect(() => {
        const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)

        const listener = () => {
            if (viewMode !== "expanded") return
            if (scrollingToEntry) return

            const currentEntry = entries
                // use slice to get a copy of the array because reverse mutates the array in-place
                .slice()
                .reverse()
                .find(e => {
                    const el = document.getElementById(Constants.dom.entryId(e))
                    return el && !Constants.layout.isTopVisible(el)
                })
            if (currentEntry) {
                dispatch(
                    selectEntry({
                        entry: currentEntry,
                        expand: false,
                        markAsRead: !!scrollMarks,
                        scrollToEntry: false,
                    })
                )
            }
        }
        const throttledListener = throttle(listener, 100)
        scrollArea?.addEventListener("scroll", throttledListener)
        return () => scrollArea?.removeEventListener("scroll", throttledListener)
    }, [dispatch, entries, viewMode, scrollMarks, scrollingToEntry])

    useMousetrap("r", () => dispatch(reloadEntries()))
    useMousetrap("j", () =>
        dispatch(
            selectNextEntry({
                expand: true,
                markAsRead: true,
                scrollToEntry: true,
            })
        )
    )
    useMousetrap("n", () =>
        dispatch(
            selectNextEntry({
                expand: false,
                markAsRead: false,
                scrollToEntry: true,
            })
        )
    )
    useMousetrap("k", () =>
        dispatch(
            selectPreviousEntry({
                expand: true,
                markAsRead: true,
                scrollToEntry: true,
            })
        )
    )
    useMousetrap("p", () =>
        dispatch(
            selectPreviousEntry({
                expand: false,
                markAsRead: false,
                scrollToEntry: true,
            })
        )
    )
    useMousetrap("space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const entryElement = document.getElementById(Constants.dom.entryId(selectedEntry))
                if (entryElement && Constants.layout.isBottomVisible(entryElement)) {
                    dispatch(
                        selectNextEntry({
                            expand: true,
                            markAsRead: true,
                            scrollToEntry: true,
                        })
                    )
                } else {
                    const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)
                    scrollArea?.scrollTo({
                        top: scrollArea.scrollTop + scrollArea.clientHeight * 0.8,
                        behavior: "smooth",
                    })
                }
            } else {
                dispatch(
                    selectEntry({
                        entry: selectedEntry,
                        expand: true,
                        markAsRead: true,
                        scrollToEntry: true,
                    })
                )
            }
        } else {
            dispatch(
                selectNextEntry({
                    expand: true,
                    markAsRead: true,
                    scrollToEntry: true,
                })
            )
        }
    })
    useMousetrap("shift+space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const entryElement = document.getElementById(Constants.dom.entryId(selectedEntry))
                if (entryElement && Constants.layout.isTopVisible(entryElement)) {
                    dispatch(
                        selectPreviousEntry({
                            expand: true,
                            markAsRead: true,
                            scrollToEntry: true,
                        })
                    )
                } else {
                    const scrollArea = document.getElementById(Constants.dom.mainScrollAreaId)
                    scrollArea?.scrollTo({
                        top: scrollArea.scrollTop - scrollArea.clientHeight * 0.8,
                        behavior: "smooth",
                    })
                }
            } else {
                dispatch(
                    selectPreviousEntry({
                        expand: true,
                        markAsRead: true,
                        scrollToEntry: true,
                    })
                )
            }
        }
    })
    useMousetrap(["o", "enter"], () => {
        // toggle expanded status
        if (!selectedEntry) return
        dispatch(
            selectEntry({
                entry: selectedEntry,
                expand: !selectedEntry.expanded,
                markAsRead: !selectedEntry.expanded,
                scrollToEntry: true,
            })
        )
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
    useMousetrap("g a", () => dispatch(redirectToRootCategory()))
    useMousetrap("?", () => openModal({ title: t`Keyboard shortcuts`, size: "xl", children: <KeyboardShortcutsHelp /> }))

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
            {entries.map(entry => (
                <div
                    key={entry.id}
                    ref={el => {
                        if (el) el.id = Constants.dom.entryId(entry)
                    }}
                >
                    <FeedEntry
                        entry={entry}
                        expanded={!!entry.expanded || viewMode === "expanded"}
                        showSelectionIndicator={entry.id === selectedEntryId && (!entry.expanded || viewMode === "expanded")}
                        onHeaderClick={event => headerClicked(entry, event)}
                    />
                </div>
            ))}
        </InfiniteScroll>
    )
}
