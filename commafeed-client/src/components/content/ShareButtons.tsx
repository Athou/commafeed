import { Trans } from "@lingui/react/macro"
import { ActionIcon, Box, CopyButton, Divider, SimpleGrid } from "@mantine/core"
import type { IconType } from "react-icons"
import { TbCheck, TbCopy, TbDeviceDesktopShare, TbDeviceMobileShare } from "react-icons/tb"
import { Constants } from "@/app/constants"
import { useAppSelector } from "@/app/store"
import type { SharingSettings } from "@/app/types"
import { useBrowserExtension } from "@/hooks/useBrowserExtension"
import { useMobile } from "@/hooks/useMobile"
import { tss } from "@/tss"

type Color = `#${string}`

const useStyles = tss
    .withParams<{
        color: Color
    }>()
    .create(({ theme, colorScheme, color }) => ({
        icon: {
            color,
            backgroundColor: colorScheme === "dark" ? theme.colors.gray[2] : "white",
        },
    }))

function ShareButton({
    icon,
    color,
    onClick,
}: Readonly<{
    icon: IconType
    color: Color
    onClick: () => void
}>) {
    const { classes } = useStyles({
        color,
    })

    return (
        <ActionIcon variant="transparent" radius="xl" size={32}>
            <Box p={6} className={classes.icon} onClick={onClick}>
                {icon({ size: 18 })}
            </Box>
        </ActionIcon>
    )
}

function SiteShareButton({
    url,
    icon,
    color,
}: Readonly<{
    icon: IconType
    color: Color
    url: string
}>) {
    const onClick = () => {
        window.open(url, "", "menubar=no,toolbar=no,resizable=yes,scrollbars=yes,width=800,height=600")
    }

    return <ShareButton icon={icon} color={color} onClick={onClick} />
}

function CopyUrlButton({
    url,
}: Readonly<{
    url: string
}>) {
    return (
        <CopyButton value={url}>
            {({ copied, copy }) => <ShareButton icon={copied ? TbCheck : TbCopy} color="#000" onClick={copy} />}
        </CopyButton>
    )
}

function BrowserNativeShareButton({
    url,
    description,
}: Readonly<{
    url: string
    description: string
}>) {
    const mobile = useMobile()
    const { isBrowserExtensionPopup } = useBrowserExtension()
    const onClick = () => {
        navigator.share({
            title: description,
            url,
        })
    }

    return (
        <ShareButton
            icon={mobile && !isBrowserExtensionPopup ? TbDeviceMobileShare : TbDeviceDesktopShare}
            color="#000"
            onClick={onClick}
        />
    )
}

export function ShareButtons(
    props: Readonly<{
        url: string
        description: string
    }>
) {
    const sharingSettings = useAppSelector(state => state.user.settings?.sharingSettings)
    const enabledSharingSites = (Object.keys(Constants.sharing) as Array<keyof SharingSettings>).filter(site => sharingSettings?.[site])
    const url = encodeURIComponent(props.url)
    const desc = encodeURIComponent(props.description)
    const clipboardAvailable = typeof navigator.clipboard !== "undefined"
    const nativeSharingAvailable = typeof navigator.share !== "undefined"
    const showNativeSection = clipboardAvailable || nativeSharingAvailable
    const showSharingSites = enabledSharingSites.length > 0
    const showDivider = showNativeSection && showSharingSites
    const showNoSharingOptionsAvailable = !showNativeSection && !showSharingSites

    return (
        <>
            {showNativeSection && (
                <SimpleGrid cols={4}>
                    {clipboardAvailable && <CopyUrlButton url={props.url} />}
                    {nativeSharingAvailable && <BrowserNativeShareButton url={props.url} description={props.description} />}
                </SimpleGrid>
            )}

            {showDivider && <Divider my="xs" />}

            {showSharingSites && (
                <SimpleGrid cols={4}>
                    {enabledSharingSites.map(site => (
                        <SiteShareButton
                            key={site}
                            icon={Constants.sharing[site].icon}
                            color={Constants.sharing[site].color}
                            url={Constants.sharing[site].url(url, desc)}
                        />
                    ))}
                </SimpleGrid>
            )}

            {showNoSharingOptionsAvailable && <Trans>No sharing options available.</Trans>}
        </>
    )
}
