import mousetrap, { ExtendedKeyboardEvent } from "mousetrap"
import { useEffect, useRef } from "react"

type Callback = (e: ExtendedKeyboardEvent, combo: string) => void

export const useMousetrap = (key: string | string[], callback: Callback) => {
    // use a ref to avoid unbinding/rebinding every time the callback changes
    const callbackRef = useRef(callback)
    callbackRef.current = callback

    useEffect(() => {
        mousetrap.bind(key, (event, combo) => {
            callbackRef.current(event, combo)

            // prevent default behavior
            return false
        })
        return () => {
            mousetrap.unbind(key)
        }
    }, [key])
}
