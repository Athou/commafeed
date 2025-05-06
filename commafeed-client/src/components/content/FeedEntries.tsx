import { Trans } from "@lingui/react/macro"
import { Box } from "@mantine/core"
import { openModal } from "@mantine/modals"
import { Constants } from "app/constants"
import type { ExpendableEntry } from "app/entries/slice"
import {
    loadMoreEntries,
    markAllAsReadWithConfirmationIfRequired,
    markEntry,
    reloadEntries,
    selectEntry,
    selectNextEntry,
    selectPreviousEntry,
    starEntry,
} from "app/entries/thunks"
import { redirectToRootCategory } from "app/redirect/thunks"
import { useAppDispatch, useAppSelector } from "app/store"
import { toggleSidebar } from "app/tree/slice"
import { selectNextUnreadTreeItem } from "app/tree/thunks"
import { KeyboardShortcutsHelp } from "components/KeyboardShortcutsHelp"
import { Loader } from "components/Loader"
import { useBrowserExtension } from "hooks/useBrowserExtension"
import { useMousetrap } from "hooks/useMousetrap"
import { useEffect } from "react"
import { useContextMenu } from "react-contexify"
import InfiniteScroll from "react-infinite-scroller"
import { throttle } from "throttle-debounce"
import { FeedEntry } from "./FeedEntry"

export function FeedEntries() {
    const entries = useAppSelector(state => state.entries.entries)
    const selectedEntryId = useAppSelector(state => state.entries.selectedEntryId)
    const hasMore = useAppSelector(state => state.entries.hasMore)
    const loading = useAppSelector(state => state.entries.loading)
    const scrollMarks = useAppSelector(state => state.user.settings?.scrollMarks)
    const scrollingToEntry = useAppSelector(state => state.entries.scrollingToEntry)
    const sidebarVisible = useAppSelector(state => state.tree.sidebarVisible)
    const customContextMenu = useAppSelector(state => state.user.settings?.customContextMenu)
    const viewMode = useAppSelector(state => state.user.localSettings.viewMode)
    const dispatch = useAppDispatch()
    const { openLinkInBackgroundTab } = useBrowserExtension()

    const selectedEntry = entries.find(e => e.id === selectedEntryId)

    const headerClicked = (entry: ExpendableEntry, event: React.MouseEvent) => {
        const middleClick = event.button === 1 || event.ctrlKey || event.metaKey
        if (middleClick || viewMode === "expanded") {
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

    const contextMenu = useContextMenu()
    const headerRightClicked = (entry: ExpendableEntry, event: React.MouseEvent) => {
        if (event.shiftKey || !customContextMenu) return

        event.preventDefault()
        contextMenu.show({
            id: Constants.dom.entryContextMenuId(entry),
            event,
        })
    }

    const bodyClicked = (entry: ExpendableEntry) => {
        if (viewMode !== "expanded") return

        // entry is already selected
        if (entry.id === selectedEntryId) return

        dispatch(
            selectEntry({
                entry,
                expand: true,
                markAsRead: true,
                scrollToEntry: true,
            })
        )
    }

    const swipedLeft = async (entry: ExpendableEntry) => await dispatch(markEntry({ entry, read: !entry.read }))

    // close context menu on scroll
    useEffect(() => {
        const listener = throttle(100, () => contextMenu.hideAll())
        window.addEventListener("scroll", listener)
        return () => window.removeEventListener("scroll", listener)
    }, [contextMenu])

    useEffect(() => {
        const listener = throttle(100, () => {
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
        })
        window.addEventListener("scroll", listener)
        return () => window.removeEventListener("scroll", listener)
    }, [dispatch, entries, viewMode, scrollMarks, scrollingToEntry])

    useMousetrap("r", async () => await dispatch(reloadEntries()))
    useMousetrap(
        "j",
        async () =>
            await dispatch(
                selectNextEntry({
                    expand: true,
                    markAsRead: true,
                    scrollToEntry: true,
                })
            )
    )
    useMousetrap(
        "n",
        async () =>
            await dispatch(
                selectNextEntry({
                    expand: false,
                    markAsRead: false,
                    scrollToEntry: true,
                })
            )
    )
    useMousetrap(
        "k",
        async () =>
            await dispatch(
                selectPreviousEntry({
                    expand: true,
                    markAsRead: true,
                    scrollToEntry: true,
                })
            )
    )
    useMousetrap(
        "p",
        async () =>
            await dispatch(
                selectPreviousEntry({
                    expand: false,
                    markAsRead: false,
                    scrollToEntry: true,
                })
            )
    )
    useMousetrap("shift+j", async () => await dispatch(selectNextUnreadTreeItem({ direction: "forward" })))
    useMousetrap("shift+k", async () => await dispatch(selectNextUnreadTreeItem({ direction: "backward" })))
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
                    window.scrollTo({
                        top: window.scrollY + document.documentElement.clientHeight * 0.8,
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
                    window.scrollTo({
                        top: window.scrollY - document.documentElement.clientHeight * 0.8,
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
        if (!selectedEntry) return
        openLinkInBackgroundTab(selectedEntry.url)
    })
    useMousetrap("m", () => {
        // toggle read status
        if (!selectedEntry) return
        dispatch(markEntry({ entry: selectedEntry, read: !selectedEntry.read }))
    })
    useMousetrap("s", () => {
        // toggle starred status
        if (!selectedEntry) return
        dispatch(starEntry({ entry: selectedEntry, starred: !selectedEntry.starred }))
    })
    useMousetrap("shift+a", () => {
        // mark all entries as read
        dispatch(markAllAsReadWithConfirmationIfRequired())
    })
    useMousetrap("g a", async () => await dispatch(redirectToRootCategory()))
    useMousetrap("f", () => dispatch(toggleSidebar()))
    useMousetrap("?", () =>
        openModal({
            title: <Trans>Keyboard shortcuts</Trans>,
            size: "xl",
            children: <KeyboardShortcutsHelp />,
        })
    )

    return (
        <InfiniteScroll
            id="entries"
            className={`cf-entries cf-view-mode-${viewMode}`}
            initialLoad={false}
            loadMore={async () => await (!loading && dispatch(loadMoreEntries()))}
            hasMore={hasMore}
            loader={<Box key={0}>{loading && <Loader />}</Box>}
        >
            {entries.map(entry => (
                <FeedEntry
                    key={entry.id}
                    entry={entry}
                    expanded={!!entry.expanded || viewMode === "expanded"}
                    selected={entry.id === selectedEntryId}
                    showSelectionIndicator={entry.id === selectedEntryId && (!entry.expanded || viewMode === "expanded")}
                    maxWidth={sidebarVisible ? Constants.layout.entryMaxWidth : undefined}
                    onHeaderClick={event => headerClicked(entry, event)}
                    onHeaderRightClick={event => headerRightClicked(entry, event)}
                    onBodyClick={() => bodyClicked(entry)}
                    onSwipedLeft={async () => await swipedLeft(entry)}
                />
            ))}
        </InfiniteScroll>
    )
}
