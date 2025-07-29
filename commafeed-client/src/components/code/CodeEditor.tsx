import { Input, Textarea } from "@mantine/core"
import type { ReactNode } from "react"
import RichCodeEditor from "@/components/code/RichCodeEditor"
import { useMobile } from "@/hooks/useMobile"

interface CodeEditorProps {
    label?: ReactNode
    description?: ReactNode
    language: "css" | "javascript"
    value?: string
    onChange: (value: string | undefined) => void
}

export function CodeEditor(props: Readonly<CodeEditorProps>) {
    const mobile = useMobile()

    return mobile ? (
        // monaco mobile support is poor, fallback to textarea
        <Textarea
            autosize
            minRows={4}
            maxRows={15}
            label={props.label}
            description={props.description}
            styles={{
                input: {
                    fontFamily: "monospace",
                },
            }}
            value={props.value}
            onChange={e => props.onChange(e.currentTarget.value)}
        />
    ) : (
        <Input.Wrapper label={props.label} description={props.description}>
            <RichCodeEditor height="30vh" language={props.language} value={props.value} onChange={props.onChange} />
        </Input.Wrapper>
    )
}
