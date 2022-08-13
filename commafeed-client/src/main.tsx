import "@fontsource/open-sans"
import { store } from "app/store"
import dayjs from "dayjs"
import relativeTime from "dayjs/plugin/relativeTime"
import React from "react"
import ReactDOM from "react-dom/client"
import { Provider } from "react-redux"
import { App } from "./App"

dayjs.extend(relativeTime)

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <React.StrictMode>
        <Provider store={store}>
            <App />
        </Provider>
    </React.StrictMode>
)
