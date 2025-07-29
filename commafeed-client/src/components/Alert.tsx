import { Trans } from "@lingui/react/macro"
import { Box, Alert as MantineAlert } from "@mantine/core"
import { Fragment } from "react"
import { TbAlertCircle, TbAlertTriangle, TbCircleCheck } from "react-icons/tb"

type Level = "error" | "warning" | "success"

export interface ErrorsAlertProps {
    level?: Level
    messages: string[]
}

export function Alert(props: Readonly<ErrorsAlertProps>) {
    let title: React.ReactNode
    let color: string
    let icon: React.ReactNode

    const level = props.level ?? "error"
    switch (level) {
        case "error":
            title = <Trans>Error</Trans>
            color = "red"
            icon = <TbAlertCircle />
            break
        case "warning":
            title = <Trans>Warning</Trans>
            color = "orange"
            icon = <TbAlertTriangle />
            break
        case "success":
            title = <Trans>Success</Trans>
            color = "green"
            icon = <TbCircleCheck />
            break
    }

    return (
        <MantineAlert title={title} color={color} icon={icon}>
            {props.messages.map((m, i) => (
                <Fragment key={m}>
                    <Box>{m}</Box>
                    {i !== props.messages.length - 1 && <br />}
                </Fragment>
            ))}
        </MantineAlert>
    )
}
