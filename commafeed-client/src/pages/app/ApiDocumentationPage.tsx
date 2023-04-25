import SwaggerUI from "swagger-ui-react"
import "swagger-ui-react/swagger-ui.css"

function ApiDocumentationPage() {
    return <SwaggerUI url="swagger/swagger.json" />
}

export default ApiDocumentationPage
