import { Trans } from "@lingui/react/macro"
import { Container, Tabs } from "@mantine/core"
import { TbFileImport, TbFolderPlus, TbRss } from "react-icons/tb"
import { AddCategory } from "@/components/content/add/AddCategory"
import { ImportOpml } from "@/components/content/add/ImportOpml"
import { Subscribe } from "@/components/content/add/Subscribe"

export function AddPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="subscribe">
                <Tabs.List>
                    <Tabs.Tab value="subscribe" leftSection={<TbRss size={16} />}>
                        <Trans>Subscribe</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="category" leftSection={<TbFolderPlus size={16} />}>
                        <Trans>Add category</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="opml" leftSection={<TbFileImport size={16} />}>
                        <Trans>OPML</Trans>
                    </Tabs.Tab>
                </Tabs.List>

                <Tabs.Panel value="subscribe" pt="xl">
                    <Subscribe />
                </Tabs.Panel>

                <Tabs.Panel value="category" pt="xl">
                    <AddCategory />
                </Tabs.Panel>

                <Tabs.Panel value="opml" pt="xl">
                    <ImportOpml />
                </Tabs.Panel>
            </Tabs>
        </Container>
    )
}
