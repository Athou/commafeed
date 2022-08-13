import { Trans } from "@lingui/macro"
import { Container, Tabs } from "@mantine/core"
import { DisplaySettings } from "components/settings/DisplaySettings"
import { ProfileSettings } from "components/settings/ProfileSettings"
import { TbPhoto, TbUser } from "react-icons/tb"

export function SettingsPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="display">
                <Tabs.List>
                    <Tabs.Tab value="display" icon={<TbPhoto />}>
                        <Trans>Display</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="profile" icon={<TbUser />}>
                        <Trans>Profile</Trans>
                    </Tabs.Tab>
                </Tabs.List>

                <Tabs.Panel value="display" pt="xl">
                    <DisplaySettings />
                </Tabs.Panel>

                <Tabs.Panel value="profile" pt="xl">
                    <ProfileSettings />
                </Tabs.Panel>
            </Tabs>
        </Container>
    )
}
