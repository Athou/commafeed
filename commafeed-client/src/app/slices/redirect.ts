import { createAsyncThunk, createSlice, PayloadAction } from "@reduxjs/toolkit"
import { Constants } from "app/constants"
import { RootState } from "app/store"

interface RedirectState {
    to?: string
}

const initialState: RedirectState = {}

export const redirectToLogin = createAsyncThunk("redirect/login", (_, thunkApi) => thunkApi.dispatch(redirectTo("/login")))
export const redirectToRegistration = createAsyncThunk("redirect/register", (_, thunkApi) => thunkApi.dispatch(redirectTo("/register")))
export const redirectToPasswordRecovery = createAsyncThunk("redirect/passwordRecovery", (_, thunkApi) =>
    thunkApi.dispatch(redirectTo("/passwordRecovery"))
)
export const redirectToSelectedSource = createAsyncThunk<void, void, { state: RootState }>("redirect/selectedSource", (_, thunkApi) => {
    const { source } = thunkApi.getState().entries
    thunkApi.dispatch(redirectTo(`/app/${source.type}/${source.id}`))
})
export const redirectToCategory = createAsyncThunk("redirect/category", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/category/${id}`))
)
export const redirectToRootCategory = createAsyncThunk("redirect/category/root", (_, thunkApi) =>
    thunkApi.dispatch(redirectToCategory(Constants.categoryIds.all))
)
export const redirectToCategoryDetails = createAsyncThunk("redirect/category/details", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/category/${id}/details`))
)
export const redirectToFeed = createAsyncThunk("redirect/feed", (id: string | number, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/feed/${id}`))
)
export const redirectToFeedDetails = createAsyncThunk("redirect/feed/details", (id: string, thunkApi) =>
    thunkApi.dispatch(redirectTo(`/app/feed/${id}/details`))
)
export const redirectToAdd = createAsyncThunk("redirect/add", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/add")))
export const redirectToSettings = createAsyncThunk("redirect/settings", (_, thunkApi) => thunkApi.dispatch(redirectTo("/app/settings")))
export const redirectToAdminUsers = createAsyncThunk("redirect/admin/users", (_, thunkApi) =>
    thunkApi.dispatch(redirectTo("/app/admin/users"))
)

export const redirectSlice = createSlice({
    name: "redirect",
    initialState,
    reducers: {
        redirectTo: (state, action: PayloadAction<string | undefined>) => {
            state.to = action.payload
        },
    },
})

export const { redirectTo } = redirectSlice.actions
export default redirectSlice.reducer
