import { Image } from "@mantine/core"
import logo from "@/assets/logo.svg"

export interface LogoProps {
    size: number
}

export function Logo(props: LogoProps) {
    return <Image src={logo} w={props.size} />
}
