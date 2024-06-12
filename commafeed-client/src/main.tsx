import "@fontsource/open-sans"
import "@mantine/core/styles.css"
import "@mantine/notifications/styles.css"
import "@mantine/spotlight/styles.css"
import "react-contexify/ReactContexify.css"
import { App } from "App"
import { store } from "app/store"
import dayjs from "dayjs"
import relativeTime from "dayjs/plugin/relativeTime"
import ReactDOM from "react-dom/client"
import { Provider } from "react-redux"

dayjs.extend(relativeTime)

const root = document.getElementById("root")
root &&
    ReactDOM.createRoot(root).render(
        <Provider store={store}>
            <App />
        </Provider>
    )
