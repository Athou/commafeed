import { store } from "app/store"
import { describe, expect, it } from "vitest"
import { redirectToCategory } from "./redirect"

describe("redirects", () => {
    it("redirects to category", async () => {
        await store.dispatch(redirectToCategory("1"))
        expect(store.getState().redirect.to).toBe("/app/category/1")
    })
})
