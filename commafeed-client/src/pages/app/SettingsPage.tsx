import { Trans } from "@lingui/macro"
import { Container, Tabs } from "@mantine/core"
import { CustomCodeSettings } from "components/settings/CustomCodeSettings"
import { DisplaySettings } from "components/settings/DisplaySettings"
import { ProfileSettings } from "components/settings/ProfileSettings"
import { TbCode, TbPhoto, TbUser } from "react-icons/tb"

export function SettingsPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="display" keepMounted={false}>
                <Tabs.List>
                    <Tabs.Tab value="display" icon={<TbPhoto size={16} />}>
                        <Trans>Display</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="customCode" icon={<TbCode size={16} />}>
                        <Trans>Custom code</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="profile" icon={<TbUser size={16} />}>
                        <Trans>Profile</Trans>
                    </Tabs.Tab>
                </Tabs.List>

                <Tabs.Panel value="display" pt="xl">
                    <DisplaySettings />
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
