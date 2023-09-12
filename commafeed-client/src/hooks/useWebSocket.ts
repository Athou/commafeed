import { setWebSocketConnected } from "app/slices/server"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { useEffect } from "react"
import WebsocketHeartbeatJs from "websocket-heartbeat-js"

export const useWebSocket = () => {
    const dispatch = useAppDispatch()

    useEffect(() => {
        const currentUrl = new URL(window.location.href)
        const wsProtocol = currentUrl.protocol === "http:" ? "ws" : "wss"
        const wsUrl = `${wsProtocol}://${currentUrl.hostname}:${currentUrl.port}/ws`

        const ws = new WebsocketHeartbeatJs({
            url: wsUrl,
            pingMsg: "ping",
            // ping interval, just under a minute to prevent firewalls from closing idle connections
            pingTimeout: 55000,
        })
        ws.onopen = () => dispatch(setWebSocketConnected(true))
        ws.onclose = () => dispatch(setWebSocketConnected(false))
        ws.onmessage = event => {
            const { data } = event
            if (typeof data === "string") {
                if (data.startsWith("new-feed-entries:")) dispatch(reloadTree())
            }
        }

        return () => ws.close()
    }, [dispatch])
}
