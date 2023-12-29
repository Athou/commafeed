import { createTss } from "tss-react"

const useContext = () => {
    // return anything here that will be accessible in tss.create()
    // we don't need anything right now
    return {}
}

export const { tss } = createTss({ useContext })

export const useStyles = tss.create({})
