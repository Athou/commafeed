import { Box, MediaQuery } from "@mantine/core"
import { Constants } from "app/constants"
import React from "react"

export function OnMobile(props: { children: React.ReactNode }) {
    return (
        <MediaQuery largerThan={Constants.layout.mobileBreakpoint} styles={{ display: "none" }}>
            <Box>{props.children}</Box>
        </MediaQuery>
    )
}
