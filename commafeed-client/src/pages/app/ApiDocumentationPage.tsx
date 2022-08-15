import { Loader } from "components/Loader"
import React, { Suspense } from "react"

export function ApiDocumentationPage() {
    // swagger-ui is very large, load only on-demand
    const SwaggerUI = React.lazy(() => import("swagger-ui-react"))
    return (
        <Suspense fallback={<Loader />}>
            <SwaggerUI url="swagger/swagger.json" />
        </Suspense>
    )
}
