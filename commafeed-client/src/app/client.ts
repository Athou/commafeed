import axios from "axios"
import {
    AddCategoryRequest,
    Category,
    CategoryModificationRequest,
    CollapseRequest,
    Entries,
    FeedInfo,
    FeedInfoRequest,
    FeedModificationRequest,
    GetEntriesPaginatedRequest,
    IDRequest,
    LoginRequest,
    MarkRequest,
    Metrics,
    MultipleMarkRequest,
    PasswordResetRequest,
    ProfileModificationRequest,
    RegistrationRequest,
    ServerInfo,
    Settings,
    StarRequest,
    SubscribeRequest,
    Subscription,
    TagRequest,
    UserModel,
} from "./types"

const axiosInstance = axios.create({ baseURL: "./rest", withCredentials: true })
axiosInstance.interceptors.response.use(
    response => response,
    error => {
        if (
            (error.response.status === 401 && error.response.data === "Credentials are required to access this resource.") ||
            (error.response.status === 403 && error.response.data === "You don't have the required role to access this resource.")
        ) {
            window.location.hash = "/welcome"
        }
        throw error
    }
)

export const client = {
    category: {
        getRoot: () => axiosInstance.get<Category>("category/get"),
        modify: (req: CategoryModificationRequest) => axiosInstance.post("category/modify", req),
        collapse: (req: CollapseRequest) => axiosInstance.post("category/collapse", req),
        getEntries: (req: GetEntriesPaginatedRequest) => axiosInstance.get<Entries>("category/entries", { params: req }),
        markEntries: (req: MarkRequest) => axiosInstance.post("category/mark", req),
        add: (req: AddCategoryRequest) => axiosInstance.post("category/add", req),
        delete: (req: IDRequest) => axiosInstance.post("category/delete", req),
    },
    entry: {
        mark: (req: MarkRequest) => axiosInstance.post("entry/mark", req),
        markMultiple: (req: MultipleMarkRequest) => axiosInstance.post("entry/markMultiple", req),
        star: (req: StarRequest) => axiosInstance.post("entry/star", req),
        getTags: () => axiosInstance.get<string[]>("entry/tags"),
        tag: (req: TagRequest) => axiosInstance.post("entry/tag", req),
    },
    feed: {
        get: (id: string) => axiosInstance.get<Subscription>(`feed/get/${id}`),
        modify: (req: FeedModificationRequest) => axiosInstance.post("feed/modify", req),
        getEntries: (req: GetEntriesPaginatedRequest) => axiosInstance.get<Entries>("feed/entries", { params: req }),
        markEntries: (req: MarkRequest) => axiosInstance.post("feed/mark", req),
        fetchFeed: (req: FeedInfoRequest) => axiosInstance.post<FeedInfo>("feed/fetch", req),
        refreshAll: () => axiosInstance.get("feed/refreshAll"),
        subscribe: (req: SubscribeRequest) => axiosInstance.post<number>("feed/subscribe", req),
        unsubscribe: (req: IDRequest) => axiosInstance.post("feed/unsubscribe", req),
        importOpml: (req: File) => {
            const formData = new FormData()
            formData.append("file", req)
            return axiosInstance.post("feed/import", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            })
        },
    },
    user: {
        login: (req: LoginRequest) => axiosInstance.post("user/login", req),
        register: (req: RegistrationRequest) => axiosInstance.post("user/register", req),
        passwordReset: (req: PasswordResetRequest) => axiosInstance.post("user/passwordReset", req),
        getSettings: () => axiosInstance.get<Settings>("user/settings"),
        saveSettings: (settings: Settings) => axiosInstance.post("user/settings", settings),
        getProfile: () => axiosInstance.get<UserModel>("user/profile"),
        saveProfile: (req: ProfileModificationRequest) => axiosInstance.post("user/profile", req),
        deleteProfile: () => axiosInstance.post("user/profile/deleteAccount"),
    },
    server: {
        getServerInfos: () => axiosInstance.get<ServerInfo>("server/get"),
    },
    admin: {
        getAllUsers: () => axiosInstance.get<UserModel[]>("admin/user/getAll"),
        saveUser: (req: UserModel) => axiosInstance.post("admin/user/save", req),
        deleteUser: (req: IDRequest) => axiosInstance.post("admin/user/delete", req),
        getMetrics: () => axiosInstance.get<Metrics>("admin/metrics"),
    },
}

/**
 * transform an error object to an array of strings that can be displayed to the user
 * @param err an error object (e.g. from axios)
 * @returns an array of messages to show the user
 */
export const errorToStrings = (err: unknown) => {
    let strings: string[] = []

    if (axios.isAxiosError(err)) {
        if (err.response) {
            const { data } = err.response
            if (typeof data === "string") strings.push(data)
            if (typeof data === "object" && data.message) strings.push(data.message)
            if (typeof data === "object" && data.errors) strings = [...strings, ...data.errors]
        }
    }

    return strings
}
