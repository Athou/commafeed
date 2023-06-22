import { useMantineTheme } from "@mantine/core"
import { Editor } from "@monaco-editor/react"

interface RichCodeEditorProps {
    height: number | string
    language: "css" | "javascript"
    value: string
    onChange: (value: string | undefined) => void
}

function RichCodeEditor(props: RichCodeEditorProps) {
    const theme = useMantineTheme()
    const editorTheme = theme.colorScheme === "dark" ? "vs-dark" : "light"

    return (
        <Editor
            height={props.height}
            defaultLanguage={props.language}
            theme={editorTheme}
            options={{ minimap: { enabled: false } }}
            value={props.value}
            onChange={props.onChange}
        />
    )
}

export default RichCodeEditor
