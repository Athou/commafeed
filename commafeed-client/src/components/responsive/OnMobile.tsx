import { Box } from "@mantine/core"
import type React from "react"
import { useMobile } from "@/hooks/useMobile"

export function OnMobile(
    props: Readonly<{
        children: React.ReactNode
    }>
) {
    const mobile = useMobile()
    return <Box>{mobile && props.children}</Box>
}
