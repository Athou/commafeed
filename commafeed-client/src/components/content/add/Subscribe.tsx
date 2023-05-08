import { Trans } from "@lingui/macro"
import { Box, Button, Group, Stack, Stepper, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { client, errorToStrings } from "app/client"
import { Constants } from "app/constants"
import { redirectToFeed, redirectToSelectedSource } from "app/slices/redirect"
import { reloadTree } from "app/slices/tree"
import { useAppDispatch } from "app/store"
import { FeedInfoRequest, SubscribeRequest } from "app/types"
import { Alert } from "components/Alert"
import { useState } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbRss } from "react-icons/tb"
import { CategorySelect } from "./CategorySelect"

export function Subscribe() {
    const [activeStep, setActiveStep] = useState(0)
    const dispatch = useAppDispatch()

    const step0Form = useForm<FeedInfoRequest>({
        initialValues: {
            url: "",
        },
    })

    const step1Form = useForm<SubscribeRequest>({
        initialValues: {
            url: "",
            title: "",
            categoryId: Constants.categories.all.id,
        },
    })

    const fetchFeed = useAsyncCallback(client.feed.fetchFeed, {
        onSuccess: ({ data }) => {
            step1Form.setFieldValue("url", data.url)
            step1Form.setFieldValue("title", data.title)
            setActiveStep(step => step + 1)
        },
    })
    const subscribe = useAsyncCallback(client.feed.subscribe, {
        onSuccess: sub => {
            dispatch(reloadTree())
            dispatch(redirectToFeed(sub.data))
        },
    })

    const previousStep = () => {
        if (activeStep === 0) dispatch(redirectToSelectedSource())
        else setActiveStep(activeStep - 1)
    }
    const nextStep = (e: React.FormEvent<HTMLFormElement>) => {
        if (activeStep === 0) {
            step0Form.onSubmit(fetchFeed.execute)(e)
        } else if (activeStep === 1) {
            step1Form.onSubmit(subscribe.execute)(e)
        }
    }

    return (
        <>
            {fetchFeed.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(fetchFeed.error)} />
                </Box>
            )}

            {subscribe.error && (
                <Box mb="md">
                    <Alert messages={errorToStrings(subscribe.error)} />
                </Box>
            )}

            <form onSubmit={nextStep}>
                <Stepper active={activeStep} onStepClick={setActiveStep}>
                    <Stepper.Step
                        label={<Trans>Analyze feed</Trans>}
                        description={<Trans>Check that the feed is working</Trans>}
                        allowStepSelect={activeStep === 1}
                    >
                        <TextInput
                            label={<Trans>Feed URL</Trans>}
                            placeholder="http://www.mysite.com/rss"
                            description={
                                <Trans>
                                    The URL for the feed you want to subscribe to. You can also use the website's url directly and CommaFeed
                                    will try to find the feed in the page.
                                </Trans>
                            }
                            required
                            autoFocus
                            {...step0Form.getInputProps("url")}
                        />
                    </Stepper.Step>
                    <Stepper.Step
                        label={<Trans>Subscribe</Trans>}
                        description={<Trans>Subscribe to the feed</Trans>}
                        allowStepSelect={false}
                    >
                        <Stack>
                            <TextInput label={<Trans>Feed URL</Trans>} {...step1Form.getInputProps("url")} disabled />
                            <TextInput label={<Trans>Feed name</Trans>} {...step1Form.getInputProps("title")} required autoFocus />
                            <CategorySelect label={<Trans>Category</Trans>} {...step1Form.getInputProps("categoryId")} clearable />
                        </Stack>
                    </Stepper.Step>
                </Stepper>

                <Group position="center" mt="xl">
                    <Button variant="default" onClick={previousStep}>
                        <Trans>Back</Trans>
                    </Button>
                    {activeStep === 0 && (
                        <Button type="submit" loading={fetchFeed.loading}>
                            <Trans>Next</Trans>
                        </Button>
                    )}
                    {activeStep === 1 && (
                        <Button type="submit" leftIcon={<TbRss size={16} />} loading={fetchFeed.loading || subscribe.loading}>
                            <Trans>Subscribe</Trans>
                        </Button>
                    )}
                </Group>
            </form>
        </>
    )
}
