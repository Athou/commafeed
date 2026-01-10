import axios, { type AxiosError } from "axios"
import type {
    AddCategoryRequest,
    AdminSaveUserRequest,
    AuthenticationError,
    Category,
    CategoryModificationRequest,
    CollapseRequest,
    Entries,
    FeedInfo,
    FeedInfoRequest,
    FeedModificationRequest,
    GetEntriesPaginatedRequest,
    IDRequest,
    InitialSetupRequest,
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
        if (isAuthenticationError(error) && window.location.hash !== "#/login") {
            const data = error.response?.data
            window.location.hash = data?.allowRegistrations ? "/welcome" : "/login"
            window.location.reload()
        }
        throw error
    }
)

function isAuthenticationError(error: unknown): error is AxiosError<AuthenticationError> {
    return axios.isAxiosError(error) && error.response?.status === 401
}

export const client = {
    category: {
        getRoot: async () => await axiosInstance.get<Category>("category/get"),
        modify: async (req: CategoryModificationRequest) => await axiosInstance.post("category/modify", req),
        collapse: async (req: CollapseRequest) => await axiosInstance.post("category/collapse", req),
        getEntries: async (req: GetEntriesPaginatedRequest) => await axiosInstance.get<Entries>("category/entries", { params: req }),
        markEntries: async (req: MarkRequest) => await axiosInstance.post("category/mark", req),
        add: async (req: AddCategoryRequest) => await axiosInstance.post("category/add", req),
        delete: async (req: IDRequest) => await axiosInstance.post("category/delete", req),
    },
    entry: {
        mark: async (req: MarkRequest) => await axiosInstance.post("entry/mark", req),
        markMultiple: async (req: MultipleMarkRequest) => await axiosInstance.post("entry/markMultiple", req),
        star: async (req: StarRequest) => await axiosInstance.post("entry/star", req),
        getTags: async () => await axiosInstance.get<string[]>("entry/tags"),
        tag: async (req: TagRequest) => await axiosInstance.post("entry/tag", req),
    },
    feed: {
        get: async (id: string) => await axiosInstance.get<Subscription>(`feed/get/${id}`),
        modify: async (req: FeedModificationRequest) => await axiosInstance.post("feed/modify", req),
        getEntries: async (req: GetEntriesPaginatedRequest) => await axiosInstance.get<Entries>("feed/entries", { params: req }),
        markEntries: async (req: MarkRequest) => await axiosInstance.post("feed/mark", req),
        fetchFeed: async (req: FeedInfoRequest) => await axiosInstance.post<FeedInfo>("feed/fetch", req),
        refreshAll: async () => await axiosInstance.get("feed/refreshAll"),
        subscribe: async (req: SubscribeRequest) => await axiosInstance.post<number>("feed/subscribe", req),
        unsubscribe: async (req: IDRequest) => await axiosInstance.post("feed/unsubscribe", req),
        importOpml: async (req: File) => {
            const formData = new FormData()
            formData.append("file", req)
            return await axiosInstance.post("feed/import", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                },
            })
        },
    },
    user: {
        login: async (req: LoginRequest) => {
            const formData = new URLSearchParams()
            formData.append("j_username", req.name)
            formData.append("j_password", req.password)
            return await axiosInstance.post("j_security_check", formData, {
                baseURL: ".",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded",
                },
            })
        },
        register: async (req: RegistrationRequest) => await axiosInstance.post("user/register", req),
        initialSetup: async (req: InitialSetupRequest) => await axiosInstance.post("user/initialSetup", req),
        passwordReset: async (req: PasswordResetRequest) => await axiosInstance.post("user/passwordReset", req),
        getSettings: async () => await axiosInstance.get<Settings>("user/settings"),
        saveSettings: async (settings: Settings) => await axiosInstance.post("user/settings", settings),
        getProfile: async () => await axiosInstance.get<UserModel>("user/profile"),
        saveProfile: async (req: ProfileModificationRequest) => await axiosInstance.post("user/profile", req),
        deleteProfile: async () => await axiosInstance.post("user/profile/deleteAccount"),
    },
    server: {
        getServerInfos: async () => await axiosInstance.get<ServerInfo>("server/get"),
    },
    admin: {
        getAllUsers: async () => await axiosInstance.get<UserModel[]>("admin/user/getAll"),
        saveUser: async (req: AdminSaveUserRequest) => await axiosInstance.post<number>("admin/user/save", req),
        deleteUser: async (req: IDRequest) => await axiosInstance.post("admin/user/delete", req),
        getMetrics: async () => await axiosInstance.get<Metrics>("admin/metrics"),
    },
}

/**
 * transform an error object to an array of strings that can be displayed to the user
 * @param err an error object (e.g. from axios)
 * @returns an array of messages to show the user
 */
export const errorToStrings = (err: unknown) => {
    let strings: string[] = []

    if (axios.isAxiosError(err) && err.response) {
        if (typeof err.response.data === "string") strings.push(err.response.data)
        if (isMessageError(err)) strings.push(err.response.data.message)
        if (isMessageArrayError(err)) strings = [...strings, ...err.response.data.errors]
    }

    return strings
}

function isMessageError(err: AxiosError): err is AxiosError<{ message: string }> {
    return !!err.response && !!err.response.data && typeof err.response.data === "object" && "message" in err.response.data
}

function isMessageArrayError(err: AxiosError): err is AxiosError<{ errors: string[] }> {
    return !!err.response && !!err.response.data && typeof err.response.data === "object" && "errors" in err.response.data
}
