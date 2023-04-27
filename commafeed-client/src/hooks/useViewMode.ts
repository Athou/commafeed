import useLocalStorage from "use-local-storage"
import { ViewMode } from "../app/types"

export function useViewMode() {
    const [viewMode, setViewMode] = useLocalStorage<ViewMode>("view-mode", "detailed")
    return { viewMode, setViewMode }
}
