import { ViewMode } from "app/types"
import useLocalStorage from "use-local-storage"

export function useViewMode() {
    const [viewMode, setViewMode] = useLocalStorage<ViewMode>("view-mode", "detailed")
    return { viewMode, setViewMode }
}
