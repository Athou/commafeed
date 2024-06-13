import { Box } from "@mantine/core"
import { HistoryService, RedocStandalone } from "redoc"

// disable redoc url sync because it causes issues with hashrouter
Object.defineProperty(HistoryService.prototype, "replace", {
    value: () => {
        // do nothing
    },
})

function ApiDocumentationPage() {
    return (
        // force white background because documentation does not support dark theme
        <Box style={{ backgroundColor: "#fff" }}>
            <RedocStandalone specUrl="openapi.json" />
        </Box>
    )
}

export default ApiDocumentationPage
