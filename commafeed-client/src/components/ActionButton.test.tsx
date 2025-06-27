import type { I18nContext } from "@lingui/react"
import { MantineProvider } from "@mantine/core"
import { fireEvent, render, screen, waitFor } from "@testing-library/react"
import { describe, expect, it, vi } from "vitest"
import { useActionButton } from "@/hooks/useActionButton"
import { ActionButton } from "./ActionButton"

vi.mock(import("@lingui/react"), () => ({
    useLingui: vi.fn().mockReturnValue({
        _: msg => msg,
    } as I18nContext),
}))
vi.mock(import("@/hooks/useActionButton"))

const label = "Test Label"
const icon = "Test Icon"
describe("ActionButton", () => {
    it("renders Button with label on desktop", () => {
        vi.mocked(useActionButton).mockReturnValue({ mobile: false, spacing: 0 })

        render(<ActionButton label={label} icon={icon} />, {
            wrapper: MantineProvider,
        })
        expect(screen.getByText(label)).toBeInTheDocument()
        expect(screen.getByText(icon)).toBeInTheDocument()
    })

    it("renders ActionIcon with tooltip on mobile", async () => {
        vi.mocked(useActionButton).mockReturnValue({ mobile: true, spacing: 0 })

        render(<ActionButton label={label} icon={icon} />, {
            wrapper: MantineProvider,
        })
        expect(screen.queryByText(label)).not.toBeInTheDocument()
        expect(screen.getByText(icon)).toBeInTheDocument()

        fireEvent.mouseEnter(screen.getByRole("button"))
        const tooltip = await waitFor(() => screen.getByRole("tooltip"))
        expect(tooltip).toContainHTML(label)
    })

    it("calls onClick handler when clicked", () => {
        vi.mocked(useActionButton).mockReturnValue({ mobile: false, spacing: 0 })
        const clickListener = vi.fn()

        render(<ActionButton label={label} icon={icon} onClick={clickListener} />, {
            wrapper: MantineProvider,
        })
        fireEvent.click(screen.getByRole("button"))

        expect(clickListener).toHaveBeenCalled()
    })
})
