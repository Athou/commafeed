import { Helmet } from "react-helmet"

export const DisablePullToRefresh = () => {
    return (
        <Helmet>
            <style type="text/css">
                {`
                    html, body {
                        overscroll-behavior: none;
                    }
                `}
            </style>
        </Helmet>
    )
}
