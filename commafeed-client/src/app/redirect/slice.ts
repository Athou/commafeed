import { createSlice, type PayloadAction } from "@reduxjs/toolkit"

interface RedirectState {
    to?: string
}

const initialState: RedirectState = {}

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
