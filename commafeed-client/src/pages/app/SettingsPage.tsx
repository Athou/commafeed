import { Trans } from "@lingui/macro"
import { Container, Tabs } from "@mantine/core"
import { DisplaySettings } from "components/settings/DisplaySettings"
import { ProfileSettings } from "components/settings/ProfileSettings"
import { TbBoxMargin, TbPhoto, TbUser } from "react-icons/tb"
import { CustomCss } from "../../components/settings/CustomCss"

export function SettingsPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="display">
                <Tabs.List>
                    <Tabs.Tab value="display" icon={<TbPhoto size={16} />}>
                        <Trans>Display</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="customCss" icon={<TbBoxMargin size={16} />}>
                        <Trans>Custom CSS</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="profile" icon={<TbUser size={16} />}>
                        <Trans>Profile</Trans>
                    </Tabs.Tab>
                </Tabs.List>

                <Tabs.Panel value="display" pt="xl">
                    <DisplaySettings />
                </Tabs.Panel>

                <Tabs.Panel value="customCss" pt="xl">
                    <CustomCss />
                </Tabs.Panel>

                <Tabs.Panel value="profile" pt="xl">
                    <ProfileSettings />
                </Tabs.Panel>
            </Tabs>
        </Container>
    )
}
