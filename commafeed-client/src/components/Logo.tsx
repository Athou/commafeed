import { Image } from "@mantine/core"
import logo from "@/assets/logo.svg"

export interface LogoProps {
    size: number
}

export function Logo(props: Readonly<LogoProps>) {
    return <Image src={logo} w={props.size} />
}
