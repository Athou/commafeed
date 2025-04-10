import { Input, Textarea } from "@mantine/core"
import RichCodeEditor from "components/code/RichCodeEditor"
import { useMobile } from "hooks/useMobile"
import type { ReactNode } from "react"

interface CodeEditorProps {
    description?: ReactNode
    language: "css" | "javascript"
    value?: string
    onChange: (value: string | undefined) => void
}

export function CodeEditor(props: CodeEditorProps) {
    const mobile = useMobile()

    return mobile ? (
        // monaco mobile support is poor, fallback to textarea
        <Textarea
            autosize
            minRows={4}
            maxRows={15}
            label={props.description}
            styles={{
                input: {
                    fontFamily: "monospace",
                },
            }}
            value={props.value}
            onChange={e => props.onChange(e.currentTarget.value)}
        />
    ) : (
        <Input.Wrapper label={props.description}>
            <RichCodeEditor height="30vh" language={props.language} value={props.value} onChange={props.onChange} />
        </Input.Wrapper>
    )
}
