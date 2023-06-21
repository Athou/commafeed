import { Box } from "@mantine/core"
import { useMobile } from "hooks/useMobile"
import React from "react"

export function OnDesktop(props: { children: React.ReactNode }) {
    const mobile = useMobile()
    return <Box>{!mobile && props.children}</Box>
}
