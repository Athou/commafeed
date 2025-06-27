import "@testing-library/jest-dom"
import { vi } from "vitest"
import { Constants } from "@/app/constants"

// reduce delay for faster tests
Constants.tooltip.delay = 10

// jsdom doesn't mock matchMedia
// https://stackoverflow.com/a/53449595/
Object.defineProperty(window, "matchMedia", {
    writable: true,
    value: vi.fn().mockImplementation(query => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: vi.fn(), // deprecated
        removeListener: vi.fn(), // deprecated
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
        dispatchEvent: vi.fn(),
    })),
})
