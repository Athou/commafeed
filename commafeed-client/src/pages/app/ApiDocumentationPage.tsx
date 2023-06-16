import { Box } from "@mantine/core"
import SwaggerUI from "swagger-ui-react"
import "swagger-ui-react/swagger-ui.css"

function ApiDocumentationPage() {
    return (
        // force white background because swagger is unreadable with dark theme
        <Box style={{ backgroundColor: "#fff" }}>
            <SwaggerUI url="swagger/swagger.json" />
        </Box>
    )
}

export default ApiDocumentationPage
