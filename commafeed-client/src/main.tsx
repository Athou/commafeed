import "@fontsource/open-sans"
import { App } from "App"
import { store } from "app/store"
import dayjs from "dayjs"
import relativeTime from "dayjs/plugin/relativeTime"
import "main.css"
import "react-contexify/ReactContexify.css"
import ReactDOM from "react-dom/client"
import { Provider } from "react-redux"

dayjs.extend(relativeTime)

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <Provider store={store}>
        <App />
    </Provider>
)
