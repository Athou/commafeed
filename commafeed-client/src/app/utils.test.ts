import { afterEach, beforeEach, describe, expect, it, vi } from "vitest"
import { scrollToWithCallback } from "./utils"

describe("scrollToWithCallback", () => {
    beforeEach(() => {
        vi.useFakeTimers()
        vi.spyOn(document.documentElement, "scrollHeight", "get").mockReturnValue(2000)
        vi.spyOn(document.documentElement, "clientHeight", "get").mockReturnValue(600)
        vi.stubGlobal("scrollY", 300)
        vi.spyOn(window, "scrollTo").mockImplementation((options: ScrollToOptions | number) => {
            if (typeof options !== "object") return
            const maxScroll = Math.max(0, document.documentElement.scrollHeight - document.documentElement.clientHeight)
            const top = Math.max(0, Math.min(options.top ?? 0, maxScroll))
            if (top !== window.scrollY) {
                vi.stubGlobal("scrollY", top)
                window.dispatchEvent(new Event("scroll"))
            }
        })
        vi.spyOn(window, "addEventListener")
        vi.spyOn(window, "removeEventListener")
    })

    afterEach(() => {
        for (const [type, listener] of vi.mocked(window.addEventListener).mock.calls) {
            if (type === "scroll") window.removeEventListener(type, listener)
        }
        vi.clearAllTimers()
        vi.restoreAllMocks()
        vi.unstubAllGlobals()
        vi.useRealTimers()
    })

    it.each([
        { name: "a reachable position", top: 800, start: 300 },
        { name: "the current position", top: 300, start: 300 },
        { name: "a position past the bottom", top: 1900, start: 300 },
        { name: "a position past the bottom when already at the bottom", top: 1900, start: 1400 },
        { name: "a negative position", top: -20, start: 300 },
        { name: "a negative position when already at the top", top: -20, start: 0 },
    ])("completes once and removes its listener for $name", ({ top, start }) => {
        vi.stubGlobal("scrollY", start)
        const onScrollEnded = vi.fn()

        scrollToWithCallback({ options: { top, behavior: "auto" }, onScrollEnded })
        vi.runAllTimers()

        expect(onScrollEnded).toHaveBeenCalledTimes(1)
        const listener = vi.mocked(window.addEventListener).mock.calls.find(([type]) => type === "scroll")?.[1]
        expect(window.removeEventListener).toHaveBeenCalledWith("scroll", listener)

        window.dispatchEvent(new Event("scroll"))
        vi.runAllTimers()
        expect(onScrollEnded).toHaveBeenCalledTimes(1)
    })

    it("completes when the document is shorter than the viewport", () => {
        vi.spyOn(document.documentElement, "scrollHeight", "get").mockReturnValue(400)
        vi.stubGlobal("scrollY", 0)
        const onScrollEnded = vi.fn()

        scrollToWithCallback({ options: { top: 100 }, onScrollEnded })
        vi.runAllTimers()

        expect(onScrollEnded).toHaveBeenCalledTimes(1)
    })

    it("waits for a smooth scroll to reach the bottom", () => {
        vi.mocked(window.scrollTo).mockImplementation(() => {})
        const onScrollEnded = vi.fn()

        scrollToWithCallback({ options: { top: 1900, behavior: "smooth" }, onScrollEnded })
        expect(onScrollEnded).not.toHaveBeenCalled()

        vi.stubGlobal("scrollY", 800)
        window.dispatchEvent(new Event("scroll"))
        vi.runAllTimers()
        expect(onScrollEnded).not.toHaveBeenCalled()

        vi.stubGlobal("scrollY", 1400)
        window.dispatchEvent(new Event("scroll"))
        vi.runAllTimers()
        expect(onScrollEnded).toHaveBeenCalledTimes(1)
    })
})
