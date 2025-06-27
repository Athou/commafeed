import { MantineProvider } from "@mantine/core"
import { render } from "@testing-library/react"
import { describe, expect, it } from "vitest"
import { Content } from "@/components/content/Content"

describe("Content component", () => {
    it("renders basic content", () => {
        const { container } = render(<Content content="<p>Hello World</p>" />, { wrapper: MantineProvider })
        expect(container.querySelector("p")).toHaveTextContent("Hello World")
    })

    it("renders highlighted text when highlight prop is provided", () => {
        const { container } = render(<Content content="Hello World" highlight="World" />, { wrapper: MantineProvider })
        expect(container.querySelector("mark")).toHaveTextContent("World")
    })

    it("renders iframe tag when included in content", () => {
        const { container } = render(<Content content='<iframe src="https://example.com"></iframe>' />, { wrapper: MantineProvider })
        expect(container.querySelector("iframe")).toHaveAttribute("src", "https://example.com")
    })

    it("does not render unsupported tags", () => {
        const { container } = render(<Content content='<script>alert("test")</script>' />, { wrapper: MantineProvider })
        expect(container.querySelector("script")).toBeNull()
    })
})
