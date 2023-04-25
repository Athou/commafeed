import { Trans } from "@lingui/macro"
import { Container, Tabs } from "@mantine/core"
import { AddCategory } from "components/content/add/AddCategory"
import { ImportOpml } from "components/content/add/ImportOpml"
import { Subscribe } from "components/content/add/Subscribe"
import { TbFileImport, TbFolderPlus, TbRss } from "react-icons/tb"

export function AddPage() {
    return (
        <Container size="sm" px={0}>
            <Tabs defaultValue="subscribe">
                <Tabs.List>
                    <Tabs.Tab value="subscribe" icon={<TbRss />}>
                        <Trans>Subscribe</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="category" icon={<TbFolderPlus />}>
                        <Trans>Add category</Trans>
                    </Tabs.Tab>
                    <Tabs.Tab value="opml" icon={<TbFileImport />}>
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
