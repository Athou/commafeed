import { createAsyncThunk } from "@reduxjs/toolkit"
import { type AppDispatch, type RootState } from "app/store"

export const createAppAsyncThunk = createAsyncThunk.withTypes<{
    state: RootState
    dispatch: AppDispatch
}>()
