import { Trans } from "@lingui/react/macro"
import { Box, Dialog, Text } from "@mantine/core"
import { useAsync } from "react-async-hook"
import { useAppDispatch, useAppSelector } from "@/app/store"
import { setAnnouncementHash } from "@/app/user/slice"
import { Content } from "@/components/content/Content"

const sha256Hex = async (input: string | undefined) => {
    const data = new TextEncoder().encode(input)
    const buffer = await crypto.subtle.digest("SHA-256", data)
    const array = Array.from(new Uint8Array(buffer))
    return array.map(b => b.toString(16).padStart(2, "0")).join("")
}

export function AnnouncementDialog() {
    const announcement = useAppSelector(state => state.server.serverInfos?.announcement)
    const announcementHash = useAsync(sha256Hex, [announcement]).result
    const existingAnnouncementHash = useAppSelector(state => state.user.localSettings.announcementHash)
    const dispatch = useAppDispatch()

    const opened = !!announcementHash && announcementHash !== existingAnnouncementHash
    const onClosed = () => announcementHash && dispatch(setAnnouncementHash(announcementHash))

    if (!announcement) return null
    return (
        <Dialog opened={opened} withCloseButton onClose={onClosed} size="xl" radius="md">
            <Box>
                <Text fw="bold">
                    <Trans>Announcement</Trans>
                </Text>
            </Box>
            <Box>
                <Content content={announcement} />
            </Box>
        </Dialog>
    )
}
