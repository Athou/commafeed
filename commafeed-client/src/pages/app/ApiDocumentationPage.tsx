import { Box } from "@mantine/core"
import { RedocStandalone } from "redoc"

function ApiDocumentationPage() {
    return (
        // force white background because documentation does not support dark theme
        <Box style={{ backgroundColor: "#fff" }}>
            <RedocStandalone specUrl="openapi/openapi.json" />
        </Box>
    )
}

export default ApiDocumentationPage
