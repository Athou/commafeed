import "@fontsource/open-sans"
import { store } from "app/store"
import dayjs from "dayjs"
import relativeTime from "dayjs/plugin/relativeTime"
import "react-contexify/ReactContexify.css"
import ReactDOM from "react-dom/client"
import { Provider } from "react-redux"
import { App } from "./App"

dayjs.extend(relativeTime)

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
    <Provider store={store}>
        <App />
    </Provider>
)
