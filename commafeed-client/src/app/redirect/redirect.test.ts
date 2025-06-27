import { describe, expect, it } from "vitest"
import { redirectToCategory } from "@/app/redirect/thunks"
import { store } from "@/app/store"

describe("redirects", () => {
    it("redirects to category", async () => {
        await store.dispatch(redirectToCategory("1"))
        expect(store.getState().redirect.to).toBe("/app/category/1")
    })
})
