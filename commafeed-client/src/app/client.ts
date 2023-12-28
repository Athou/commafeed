import axios from "axios"
import {
    type AddCategoryRequest,
    type AdminSaveUserRequest,
    type Category,
    type CategoryModificationRequest,
    type CollapseRequest,
    type Entries,
    type FeedInfo,
    type FeedInfoRequest,
    type FeedModificationRequest,
    type GetEntriesPaginatedRequest,
    type IDRequest,
    type LoginRequest,
    type MarkRequest,
    type Metrics,
    type MultipleMarkRequest,
    type PasswordResetRequest,
    type ProfileModificationRequest,
    type RegistrationRequest,
    type ServerInfo,
    type Settings,
    type StarRequest,
    type SubscribeRequest,
    type Subscription,
    type TagRequest,
    type UserModel,
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
        login: async (req: LoginRequest) => await axiosInstance.post("user/login", req),
        register: async (req: RegistrationRequest) => await axiosInstance.post("user/register", req),
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
        saveUser: async (req: AdminSaveUserRequest) => await axiosInstance.post("admin/user/save", req),
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

    if (axios.isAxiosError(err)) {
        if (err.response) {
            const { data } = err.response
            if (typeof data === "string") strings.push(data)
            if (typeof data === "object" && data.message) strings.push(data.message as string)
            if (typeof data === "object" && data.errors) strings = [...strings, ...data.errors]
        }
    }

    return strings
}
