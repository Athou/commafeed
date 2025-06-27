import { createAsyncThunk } from "@reduxjs/toolkit"
import type { AppDispatch, RootState } from "@/app/store"

export const createAppAsyncThunk = createAsyncThunk.withTypes<{
    state: RootState
    dispatch: AppDispatch
}>()
