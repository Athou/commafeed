import { createAppAsyncThunk } from "@/app/async-thunk"
import { Constants } from "@/app/constants"
import { redirectTo } from "@/app/redirect/slice"

export const redirectToLogin = createAppAsyncThunk("redirect/login", (_, thunkApi) => thunkApi.dispatch(redirectTo("/login")))

export const redirectToRegistration = createAppAsyncThunk("redirect/register", (_, thunkApi) => thunkApi.dispatch(redirectTo("/register")))

export const redirectToInitialSetup = createAppAsyncThunk("redirect/initialSetup", (_, thunkApi) => thunkApi.dispatch(redirectTo("/setup")))

export const redirectToApiDocumentation = createAppAsyncThunk("redirect/api", () => {
    window.location.href = "api-documentation/"
})

export const redirectToSelectedSource = createAppAsyncThunk("redirect/selectedSource", (_, thunkApi) => {
    const { source } = thunkApi.getState().entries
    thunkApi.dispatch(redirectTo(`/app/${source.type}/${source.id}`))
})

export const redirectToCategory = createAppAsyncThunk("redirect/category", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/category/${id}`))
)

export const redirectToRootCategory = createAppAsyncThunk(
    "redirect/category/root",
    async (_, thunkApi) => await thunkApi.dispatch(redirectToCategory(Constants.categories.all.id))
)

export const redirectToCategoryDetails = createAppAsyncThunk("redirect/category/details", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/category/${id}/details`))
)

export const redirectToFeed = createAppAsyncThunk("redirect/feed", (id: string | number, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/feed/${id}`))
)

export const redirectToFeedDetails = createAppAsyncThunk("redirect/feed/details", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/feed/${id}/details`))
)

export const redirectToTag = createAppAsyncThunk("redirect/tag", (id: string, thunkApi) => thunkApi.dispatch(redirectTo(`/app/tag/${id}`)))

export const redirectToTagDetails = createAppAsyncThunk("redirect/tag/details", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/tag/${id}/details`))
)

export const redirectToAdd = createAppAsyncThunk("redirect/add", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/add")))

export const redirectToSettings = createAppAsyncThunk("redirect/settings", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/settings")))

export const redirectToAdminUsers = createAppAsyncThunk("redirect/admin/users", (_, thunkApi) =>
    thunkApi.dispatch(redirectTo("/app/admin/users"))
)

export const redirectToMetrics = createAppAsyncThunk("redirect/admin/metrics", (_, thunkApi) =>
    thunkApi.dispatch(redirectTo("/app/admin/metrics"))
)

export const redirectToDonate = createAppAsyncThunk("redirect/donate", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/donate")))

export const redirectToAbout = createAppAsyncThunk("redirect/about", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/about")))
