import type { AxiosError } from "axios"
import { describe, expect, it } from "vitest"
import { loginErrorToStrings } from "./client"

const axiosError = (status: number, data: unknown) =>
    ({
        isAxiosError: true,
        response: { status, data },
    }) as AxiosError

describe("loginErrorToStrings", () => {
    it("uses the translated message for authentication errors", () => {
        const error = axiosError(401, { message: "wrong username or password" })

        expect(loginErrorToStrings(error, "Translated authentication error")).toEqual(["Translated authentication error"])
    })

    it("preserves backend messages for unexpected errors", () => {
        const error = axiosError(500, { message: "unexpected error" })

        expect(loginErrorToStrings(error, "Translated authentication error")).toEqual(["unexpected error"])
    })
})
