export const DisablePullToRefresh = ({ enabled }: { enabled: boolean | undefined }) => {
    return enabled ? <style>{`html, body { overscroll-behavior: none; }`}</style> : null
}
