import { t } from "@lingui/macro"
import { Alert as MantineAlert, Box } from "@mantine/core"
import { Fragment } from "react"
import { TbAlertCircle, TbAlertTriangle, TbCircleCheck } from "react-icons/tb"

type Level = "error" | "warning" | "success"
export interface ErrorsAlertProps {
    level?: Level
    messages: string[]
}

export function Alert(props: ErrorsAlertProps) {
    let title: string
    let color: string
    let icon: React.ReactNode

    const level = props.level ?? "error"
    switch (level) {
        case "error":
            title = t`Error`
            color = "red"
            icon = <TbAlertCircle />
            break
        case "warning":
            title = t`Warning`
            color = "orange"
            icon = <TbAlertTriangle />
            break
        case "success":
            title = t`Success`
            color = "green"
            icon = <TbCircleCheck />
            break
        default:
            throw Error(`unsupported level: ${level}`)
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
