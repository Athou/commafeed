import { useEffect } from "react"
import WebsocketHeartbeatJs from "websocket-heartbeat-js"
import { setWebSocketConnected } from "@/app/server/slice"
import { type AppDispatch, useAppDispatch, useAppSelector } from "@/app/store"
import { newFeedEntriesDiscovered } from "@/app/tree/thunks"

const handleMessage = (dispatch: AppDispatch, message: string) => {
    const parts = message.split(":")
    const type = parts[0]
    if (type === "new-feed-entries") {
        dispatch(
            newFeedEntriesDiscovered({
                feedId: +parts[1],
                amount: +parts[2],
            })
        )
    }
}

export const useWebSocket = () => {
    const websocketEnabled = useAppSelector(state => state.server.serverInfos?.websocketEnabled)
    const websocketPingInterval = useAppSelector(state => state.server.serverInfos?.websocketPingInterval)
    const dispatch = useAppDispatch()

    useEffect(() => {
        let ws: WebsocketHeartbeatJs | undefined

        if (websocketEnabled && websocketPingInterval) {
            const currentUrl = new URL(window.location.href)
            const wsProtocol = currentUrl.protocol === "http:" ? "ws" : "wss"
            const wsUrl = `${wsProtocol}://${currentUrl.hostname}:${currentUrl.port}${currentUrl.pathname}ws`

            ws = new WebsocketHeartbeatJs({
                url: wsUrl,
                pingMsg: "ping",
                pingTimeout: websocketPingInterval,
            })
            ws.onopen = () => dispatch(setWebSocketConnected(true))
            ws.onclose = () => dispatch(setWebSocketConnected(false))
            ws.onmessage = event => {
                if (typeof event.data === "string") {
                    handleMessage(dispatch, event.data)
                }
            }
        }

        return () => ws?.close()
    }, [dispatch, websocketEnabled, websocketPingInterval])
}
