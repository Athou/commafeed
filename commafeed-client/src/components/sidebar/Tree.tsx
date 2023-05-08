import { Trans } from "@lingui/macro"
import { Box, Stack } from "@mantine/core"
import { Constants } from "app/constants"
import {
    redirectToCategory,
    redirectToCategoryDetails,
    redirectToFeed,
    redirectToFeedDetails,
    redirectToTag,
    redirectToTagDetails,
} from "app/slices/redirect"
import { collapseTreeCategory } from "app/slices/tree"
import { useAppDispatch, useAppSelector } from "app/store"
import { Category, Subscription } from "app/types"
import { categoryUnreadCount, flattenCategoryTree } from "app/utils"
import { Loader } from "components/Loader"
import { OnDesktop } from "components/responsive/OnDesktop"
import React from "react"
import { TbChevronDown, TbChevronRight, TbInbox, TbStar, TbTag } from "react-icons/tb"
import { TreeNode } from "./TreeNode"
import { TreeSearch } from "./TreeSearch"

const allIcon = <TbInbox size={16} />
const starredIcon = <TbStar size={16} />
const tagIcon = <TbTag size={16} />
const expandedIcon = <TbChevronDown size={16} />
const collapsedIcon = <TbChevronRight size={16} />

const errorThreshold = 9

export function Tree() {
    const root = useAppSelector(state => state.tree.rootCategory)
    const source = useAppSelector(state => state.entries.source)
    const tags = useAppSelector(state => state.user.tags)
    const showRead = useAppSelector(state => state.user.settings?.showRead)
    const dispatch = useAppDispatch()

    const feedClicked = (e: React.MouseEvent, id: string) => {
        if (e.detail === 2) dispatch(redirectToFeedDetails(id))
        else dispatch(redirectToFeed(id))
    }
    const categoryClicked = (e: React.MouseEvent, id: string) => {
        if (e.detail === 2) {
            dispatch(redirectToCategoryDetails(id))
        } else {
            dispatch(redirectToCategory(id))
        }
    }
    const categoryIconClicked = (e: React.MouseEvent, category: Category) => {
        e.stopPropagation()

        dispatch(
            collapseTreeCategory({
                id: +category.id,
                collapse: category.expanded,
            })
        )
    }
    const tagClicked = (e: React.MouseEvent, id: string) => {
        if (e.detail === 2) dispatch(redirectToTagDetails(id))
        else dispatch(redirectToTag(id))
    }

    const allCategoryNode = () => (
        <TreeNode
            id={Constants.categories.all.id}
            name={<Trans>All</Trans>}
            icon={allIcon}
            unread={categoryUnreadCount(root)}
            selected={source.type === "category" && source.id === Constants.categories.all.id}
            expanded={false}
            level={0}
            hasError={false}
            onClick={categoryClicked}
        />
    )
    const starredCategoryNode = () => (
        <TreeNode
            id={Constants.categories.starred.id}
            name={<Trans>Starred</Trans>}
            icon={starredIcon}
            unread={0}
            selected={source.type === "category" && source.id === Constants.categories.starred.id}
            expanded={false}
            level={0}
            hasError={false}
            onClick={categoryClicked}
        />
    )

    const categoryNode = (category: Category, level = 0) => {
        const unreadCount = categoryUnreadCount(category)
        if (unreadCount === 0 && !showRead) return null

        const hasError = !category.expanded && flattenCategoryTree(category).some(c => c.feeds.some(f => f.errorCount > errorThreshold))
        return (
            <TreeNode
                id={category.id}
                name={category.name}
                icon={category.expanded ? expandedIcon : collapsedIcon}
                unread={unreadCount}
                selected={source.type === "category" && source.id === category.id}
                expanded={category.expanded}
                level={level}
                hasError={hasError}
                onClick={categoryClicked}
                onIconClick={e => categoryIconClicked(e, category)}
                key={category.id}
            />
        )
    }

    const feedNode = (feed: Subscription, level = 0) => {
        if (feed.unread === 0 && !showRead) return null

        return (
            <TreeNode
                id={String(feed.id)}
                name={feed.name}
                icon={feed.iconUrl}
                unread={feed.unread}
                selected={source.type === "feed" && source.id === String(feed.id)}
                level={level}
                hasError={feed.errorCount > errorThreshold}
                onClick={feedClicked}
                key={feed.id}
            />
        )
    }

    const tagNode = (tag: string) => (
        <TreeNode
            id={tag}
            name={tag}
            icon={tagIcon}
            unread={0}
            selected={source.type === "tag" && source.id === tag}
            level={0}
            hasError={false}
            onClick={tagClicked}
            key={tag}
        />
    )

    const recursiveCategoryNode = (category: Category, level = 0) => (
        <React.Fragment key={`recursiveCategoryNode-${category.id}`}>
            {categoryNode(category, level)}
            {category.expanded && category.children.map(c => recursiveCategoryNode(c, level + 1))}
            {category.expanded && category.feeds.map(f => feedNode(f, level + 1))}
        </React.Fragment>
    )

    if (!root) return <Loader />
    const feeds = flattenCategoryTree(root).flatMap(c => c.feeds)
    return (
        <Stack>
            <OnDesktop>
                <TreeSearch feeds={feeds} />
            </OnDesktop>
            <Box>
                {allCategoryNode()}
                {starredCategoryNode()}
                {root.children.map(c => recursiveCategoryNode(c))}
                {root.feeds.map(f => feedNode(f))}
                {tags?.map(tag => tagNode(tag))}
            </Box>
        </Stack>
    )
}
