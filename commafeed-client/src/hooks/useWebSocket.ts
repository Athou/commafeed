import { setWebSocketConnected } from "app/slices/server"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch, useAppSelector } from "app/store"
import { useEffect } from "react"
import WebsocketHeartbeatJs from "websocket-heartbeat-js"

export const useWebSocket = () => {
    const websocketEnabled = useAppSelector(state => state.server.serverInfos?.websocketEnabled)
    const websocketPingInterval = useAppSelector(state => state.server.serverInfos?.websocketPingInterval)
    const dispatch = useAppDispatch()

    useEffect(() => {
        let ws: WebsocketHeartbeatJs | undefined

        if (websocketEnabled && websocketPingInterval) {
            const currentUrl = new URL(window.location.href)
            const wsProtocol = currentUrl.protocol === "http:" ? "ws" : "wss"
            const wsUrl = `${wsProtocol}://${currentUrl.hostname}:${currentUrl.port}/ws`

            ws = new WebsocketHeartbeatJs({
                url: wsUrl,
                pingMsg: "ping",
                pingTimeout: websocketPingInterval,
            })
            ws.onopen = () => dispatch(setWebSocketConnected(true))
            ws.onclose = () => dispatch(setWebSocketConnected(false))
            ws.onmessage = event => {
                const { data } = event
                if (typeof data === "string") {
                    if (data.startsWith("new-feed-entries:")) dispatch(reloadTree())
                }
            }
        }

        return () => ws?.close()
    }, [dispatch, websocketEnabled, websocketPingInterval])
}
