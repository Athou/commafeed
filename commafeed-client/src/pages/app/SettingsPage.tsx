import { Trans } from "@lingui/react/macro"
import { Container, Tabs } from "@mantine/core"
import { TbBell, TbCode, TbPhoto, TbUser } from "react-icons/tb"
import { CustomCodeSettings } from "@/components/settings/CustomCodeSettings"
import { DisplaySettings } from "@/components/settings/DisplaySettings"
import { NotificationSettings } from "@/components/settings/NotificationSettings"
import { ProfileSettings } from "@/components/settings/ProfileSettings"

export function SettingsPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="display" keepMounted={false}>
                <Tabs.List>
                    <Tabs.Tab value="display" leftSection={<TbPhoto size={16} />}>
                        <Trans>Display</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="notifications" leftSection={<TbBell size={16} />}>
                        <Trans>Notifications</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="customCode" leftSection={<TbCode size={16} />}>
                        <Trans>Custom code</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="profile" leftSection={<TbUser size={16} />}>
                        <Trans>Profile</Trans>
                    </Tabs.Tab>
                </Tabs.List>

                <Tabs.Panel value="display" pt="xl">
                    <DisplaySettings />
                </Tabs.Panel>

                <Tabs.Panel value="notifications" pt="xl">
                    <NotificationSettings />
                </Tabs.Panel>

                <Tabs.Panel value="customCode" pt="xl">
                    <CustomCodeSettings />
                </Tabs.Panel>

                <Tabs.Panel value="profile" pt="xl">
                    <ProfileSettings />
                </Tabs.Panel>
            </Tabs>
        </Container>
    )
}
