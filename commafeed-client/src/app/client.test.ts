import { i18n } from "@lingui/core"
import type { AxiosError } from "axios"
import { afterEach, describe, expect, it, vi } from "vitest"
import { errorToStrings } from "./client"

const axiosError = (status: number, data: unknown) =>
    ({
        isAxiosError: true,
        response: { status, data },
    }) as AxiosError

describe("errorToStrings", () => {
    afterEach(() => vi.restoreAllMocks())

    it("translates known application error types", () => {
        vi.spyOn(i18n, "_").mockReturnValue("Translated authentication error")
        const error = axiosError(401, {
            type: "WRONG_USERNAME_OR_PASSWORD",
            message: "wrong username or password",
        })

        expect(errorToStrings(error)).toEqual(["Translated authentication error"])
    })

    it("preserves backend messages for unexpected errors", () => {
        const error = axiosError(500, { message: "unexpected error" })

        expect(errorToStrings(error)).toEqual(["unexpected error"])
    })

    it("preserves backend messages for unknown application error types", () => {
        const error = axiosError(400, { type: "UNKNOWN_ERROR", message: "unknown error" })

        expect(errorToStrings(error)).toEqual(["unknown error"])
    })
})
