import { Trans } from "@lingui/react/macro"
import { Box, Button, Checkbox, Group, Stack, Stepper, TextInput } from "@mantine/core"
import { useForm } from "@mantine/form"
import { useState } from "react"
import { useAsyncCallback } from "react-async-hook"
import { TbRss } from "react-icons/tb"
import { client, errorToStrings } from "@/app/client"
import { Constants } from "@/app/constants"
import { redirectToFeed, redirectToSelectedSource } from "@/app/redirect/thunks"
import { useAppDispatch } from "@/app/store"
import { reloadTree } from "@/app/tree/thunks"
import type { FeedInfoRequest, SubscribeRequest } from "@/app/types"
import { Alert } from "@/components/Alert"
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
            notifyOnNewEntries: true,
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
            dispatch(reloadTree()).then(() => dispatch(redirectToFeed(sub.data)))
        },
    })

    const previousStep = () => {
        if (activeStep === 0) {
            dispatch(redirectToSelectedSource())
        } else {
            setActiveStep(activeStep - 1)
        }
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
                            placeholder="https://www.mysite.com/rss"
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
                            <Checkbox
                                label={<Trans>Receive notifications</Trans>}
                                {...step1Form.getInputProps("notifyOnNewEntries", { type: "checkbox" })}
                            />
                        </Stack>
                    </Stepper.Step>
                </Stepper>

                <Group justify="center" mt="xl">
                    <Button variant="default" onClick={previousStep}>
                        <Trans>Back</Trans>
                    </Button>
                    {activeStep === 0 && (
                        <Button type="submit" loading={fetchFeed.loading}>
                            <Trans>Next</Trans>
                        </Button>
                    )}
                    {activeStep === 1 && (
                        <Button type="submit" leftSection={<TbRss size={16} />} loading={fetchFeed.loading || subscribe.loading}>
                            <Trans>Subscribe</Trans>
                        </Button>
                    )}
                </Group>
            </form>
        </>
    )
}
