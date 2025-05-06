import { Trans } from "@lingui/react/macro"
import { Anchor, Box, Code, Container, Group, List, Title } from "@mantine/core"
import { Constants } from "app/constants"
import { TbBrandGithub, TbBrandPaypal, TbCoinBitcoin, TbHeartFilled } from "react-icons/tb"

const iconSize = 24

export function DonatePage() {
    return (
        <Container size="xl" my="xl">
            <Group>
                <TbHeartFilled size={iconSize} color="red" />
                <Title order={3}>
                    <Trans>Donate</Trans>
                </Title>
            </Group>

            <Box my="xl">
                <Trans>
                    <Box>Hey,</Box>
                    <Box mt="xs">
                        I'm Jérémie from Belgium and I've been working on CommaFeed in my free time for over 10 years now. Thanks for taking
                        an interest in helping me continue supporting CommaFeed.
                    </Box>
                </Trans>

                <List mt="lg">
                    <List.Item icon={<TbBrandGithub size={iconSize} />}>
                        <Anchor href="https://github.com/sponsors/Athou" target="_blank" rel="noreferrer">
                            GitHub Sponsors
                        </Anchor>
                    </List.Item>
                    <List.Item icon={<TbBrandPaypal size={iconSize} />}>
                        <Anchor
                            href="https://www.paypal.com/donate/?business=9CNQHMJG2ZJVY&no_recurring=0&item_name=CommaFeed&currency_code=EUR"
                            target="_blank"
                            rel="noreferrer"
                        >
                            Paypal EUR
                        </Anchor>
                    </List.Item>
                    <List.Item icon={<TbBrandPaypal size={iconSize} />}>
                        <Anchor
                            href="https://www.paypal.com/donate/?business=9CNQHMJG2ZJVY&no_recurring=0&item_name=CommaFeed&currency_code=USD"
                            target="_blank"
                            rel="noreferrer"
                        >
                            Paypal USD
                        </Anchor>
                    </List.Item>
                    <List.Item icon={<TbCoinBitcoin size={iconSize} />}>
                        Bitcoin: <Code>{Constants.bitcoinWalletAddress}</Code>
                    </List.Item>
                </List>
            </Box>
        </Container>
    )
}
