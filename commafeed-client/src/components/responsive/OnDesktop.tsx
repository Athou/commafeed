import { Box, MediaQuery } from "@mantine/core"
import { Constants } from "app/constants"
import React from "react"

export function OnDesktop(props: { children: React.ReactNode }) {
    return (
        <MediaQuery smallerThan={Constants.layout.mobileBreakpoint} styles={{ display: "none" }}>
            <Box>{props.children}</Box>
        </MediaQuery>
    )
}
