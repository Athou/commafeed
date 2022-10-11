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
import { useEffect, useRef } from "react"
import InfiniteScroll from "react-infinite-scroller"
import { FeedEntry } from "./FeedEntry"

export function FeedEntries() {
    const source = useAppSelector(state => state.entries.source)
    const entries = useAppSelector(state => state.entries.entries)
    const entriesTimestamp = useAppSelector(state => state.entries.timestamp)
    const selectedEntryId = useAppSelector(state => state.entries.selectedEntryId)
    const hasMore = useAppSelector(state => state.entries.hasMore)
    const viewMode = useAppSelector(state => state.user.settings?.viewMode)
    const scrollSpeed = useAppSelector(state => state.user.settings?.scrollSpeed)
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
                })
            )
        }
    }

    // references to entries html elements
    const refs = useRef<{ [id: string]: HTMLDivElement }>({})
    useEffect(() => {
        // remove refs that are not in entries anymore
        Object.keys(refs.current).forEach(k => {
            const found = entries.some(e => e.id === k)
            if (!found) delete refs.current[k]
        })
    }, [entries])

    // scroll to entry when selected entry changes
    useEffect(() => {
        if (!selectedEntryId) return
        if (!selectedEntry?.expanded) return

        const selectedEntryElement = refs.current[selectedEntryId]
        if (Constants.layout.isTopVisible(selectedEntryElement) && Constants.layout.isBottomVisible(selectedEntryElement)) return

        document.getElementById(Constants.dom.mainScrollAreaId)?.scrollTo({
            // having a small gap between the top of the content and the top of the page is sexier
            top: selectedEntryElement.offsetTop - 3,
            behavior: scrollSpeed && scrollSpeed > 0 ? "smooth" : "auto",
        })
    }, [selectedEntryId, selectedEntry?.expanded, scrollSpeed])

    useMousetrap("r", () => {
        dispatch(reloadEntries())
    })
    useMousetrap("j", () => {
        dispatch(
            selectNextEntry({
                expand: true,
                markAsRead: true,
            })
        )
    })
    useMousetrap("n", () => {
        dispatch(
            selectNextEntry({
                expand: false,
                markAsRead: false,
            })
        )
    })
    useMousetrap("k", () => {
        dispatch(
            selectPreviousEntry({
                expand: true,
                markAsRead: true,
            })
        )
    })
    useMousetrap("p", () => {
        dispatch(
            selectPreviousEntry({
                expand: false,
                markAsRead: false,
            })
        )
    })
    useMousetrap("space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const ref = refs.current[selectedEntry.id]
                if (Constants.layout.isBottomVisible(ref)) {
                    dispatch(
                        selectNextEntry({
                            expand: true,
                            markAsRead: true,
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
                    })
                )
            }
        } else {
            dispatch(
                selectNextEntry({
                    expand: true,
                    markAsRead: true,
                })
            )
        }
    })
    useMousetrap("shift+space", () => {
        if (selectedEntry) {
            if (selectedEntry.expanded) {
                const ref = refs.current[selectedEntry.id]
                if (Constants.layout.isTopVisible(ref)) {
                    dispatch(
                        selectPreviousEntry({
                            expand: true,
                            markAsRead: true,
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
    useMousetrap("g a", () => {
        dispatch(redirectToRootCategory())
    })
    useMousetrap("?", () => {
        openModal({ title: t`Keyboard shortcuts`, size: "xl", children: <KeyboardShortcutsHelp /> })
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
            {entries.map(entry => (
                <div
                    key={entry.id}
                    ref={el => {
                        refs.current[entry.id] = el!
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
