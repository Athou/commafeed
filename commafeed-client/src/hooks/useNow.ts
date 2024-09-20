import { useEffect, useState } from "react"

export const useNow = (interval = 1000): Date => {
    const [time, setTime] = useState(new Date())
    useEffect(() => {
        const t = setInterval(() => setTime(new Date()), interval)
        return () => clearInterval(t)
    }, [interval])
    return time
}
