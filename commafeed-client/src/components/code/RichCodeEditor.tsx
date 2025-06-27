import { useAsync } from "react-async-hook"
import { Loader } from "@/components/Loader"
import { useColorScheme } from "@/hooks/useColorScheme"

const init = async () => {
    window.MonacoEnvironment = {
        async getWorker(_, label) {
            let worker: typeof import("*?worker")
            if (label === "css") {
                worker = await import("monaco-editor/esm/vs/language/css/css.worker?worker")
            } else if (label === "javascript") {
                worker = await import("monaco-editor/esm/vs/language/typescript/ts.worker?worker")
            } else {
                worker = await import("monaco-editor/esm/vs/editor/editor.worker?worker")
            }
            return new worker.default()
        },
    }

    const monacoReact = await import("@monaco-editor/react")
    const monaco = await import("monaco-editor")
    monacoReact.loader.config({ monaco })
    return monacoReact.Editor
}

interface RichCodeEditorProps {
    height: number | string
    language: "css" | "javascript"
    value?: string
    onChange: (value: string | undefined) => void
}

function RichCodeEditor(props: RichCodeEditorProps) {
    const colorScheme = useColorScheme()
    const editorTheme = colorScheme === "dark" ? "vs-dark" : "light"

    const { result: Editor } = useAsync(init, [])
    if (!Editor) return <Loader />
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
