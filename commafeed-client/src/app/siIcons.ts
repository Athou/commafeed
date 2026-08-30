import type { IconType } from "react-icons"

// react-icons/si (Simple Icons) has ~3300 exports, so it must never be part of the main
// bundle. This module-level cached promise ensures the dynamic import is only triggered
// once and shared by every consumer (the icon picker and however many custom sharing
// buttons render at once), rather than each one independently re-invoking import().
let cache: Promise<Record<string, IconType>> | undefined

export function loadSiIcons(): Promise<Record<string, IconType>> {
    if (!cache) {
        cache = (import("react-icons/si") as unknown as Promise<Record<string, IconType>>).catch(e => {
            // don't keep a rejected promise cached: a transient chunk load failure (flaky
            // network, or stale asset hashes right after a deploy) would otherwise disable
            // icons for the rest of the session, with a reload as the only way out
            cache = undefined
            throw e
        })
    }
    return cache
}
