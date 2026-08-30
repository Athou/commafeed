import { describe, expect, it } from "vitest"
import { buildCustomSharingUrl } from "@/app/customSharing"

describe("buildCustomSharingUrl", () => {
    it("replaces url and title placeholders", () => {
        // biome-ignore lint/suspicious/noTemplateCurlyInString: ${url}/${title} are the literal placeholder syntax, not a forgotten template string
        const result = buildCustomSharingUrl("https://example.com/save?url=${url}&title=${title}", "https://a.com/x", "My Title")
        expect(result).toBe("https://example.com/save?url=https%3A%2F%2Fa.com%2Fx&title=My%20Title")
    })

    it("handles a pattern using only one of the two placeholders", () => {
        // biome-ignore lint/suspicious/noTemplateCurlyInString: ${url} is the literal placeholder syntax, not a forgotten template string
        const result = buildCustomSharingUrl("https://example.com/save?url=${url}", "https://a.com/x", "My Title")
        expect(result).toBe("https://example.com/save?url=https%3A%2F%2Fa.com%2Fx")
    })

    it("handles multiple occurrences of the same placeholder", () => {
        // biome-ignore lint/suspicious/noTemplateCurlyInString: ${url} is the literal placeholder syntax, not a forgotten template string
        const result = buildCustomSharingUrl("https://example.com/${url}/again/${url}", "https://a.com/x", "My Title")
        expect(result).toBe("https://example.com/https%3A%2F%2Fa.com%2Fx/again/https%3A%2F%2Fa.com%2Fx")
    })

    it("passes through patterns with no placeholders unchanged", () => {
        const result = buildCustomSharingUrl("https://example.com/save", "https://a.com/x", "My Title")
        expect(result).toBe("https://example.com/save")
    })

    it("encodes special characters without double-encoding", () => {
        // biome-ignore lint/suspicious/noTemplateCurlyInString: ${url} is the literal placeholder syntax, not a forgotten template string
        const result = buildCustomSharingUrl("https://example.com/save?url=${url}", "https://a.com/x?a=1&b=2 space", "Title & More")
        expect(result).toBe("https://example.com/save?url=https%3A%2F%2Fa.com%2Fx%3Fa%3D1%26b%3D2%20space")
    })
})
