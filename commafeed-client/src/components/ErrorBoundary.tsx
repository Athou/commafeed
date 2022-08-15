import { ErrorPage } from "pages/ErrorPage"
import React, { ReactNode } from "react"

interface ErrorBoundaryProps {
    children?: ReactNode
}

interface ErrorBoundaryState {
    error?: Error
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
    constructor(props: ErrorBoundaryProps) {
        super(props)
        this.state = {}
    }

    componentDidCatch(error: Error) {
        this.setState({ error })
    }

    render() {
        if (this.state.error) return <ErrorPage error={this.state.error} />
        return this.props.children
    }
}
