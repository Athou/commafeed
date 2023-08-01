import { Trans } from "@lingui/macro"
import { Box, Dialog, Text } from "@mantine/core"
import { useAppSelector } from "app/store"
import { Content } from "components/content/Content"
import { useAsync } from "react-async-hook"
import useLocalStorage from "use-local-storage"

const sha256Hex = async (input: string | undefined) => {
    const data = new TextEncoder().encode(input)
    const buffer = await crypto.subtle.digest("SHA-256", data)
    const array = Array.from(new Uint8Array(buffer))
    return array.map(b => b.toString(16).padStart(2, "0")).join("")
}

export function AnnouncementDialog() {
    const announcement = useAppSelector(state => state.server.serverInfos?.announcement)
    const announcementHash = useAsync(sha256Hex, [announcement]).result
    const [localStorageHash, setLocalStorageHash] = useLocalStorage("announcement-hash", "no-hash")

    const opened = !!announcementHash && announcementHash !== localStorageHash
    const onClosed = () => setLocalStorageHash(announcementHash)

    if (!announcement) return null
    return (
        <Dialog opened={opened} withCloseButton onClose={onClosed} size="xl" radius="md">
            <Box>
                <Text weight="bold">
                    <Trans>Announcement</Trans>
                </Text>
            </Box>
            <Box>
                <Content content={announcement} />
            </Box>
        </Dialog>
    )
}
