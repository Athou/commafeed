export interface AddCategoryRequest {
    name: string
    parentId?: string
}

export interface ApplicationSettings {
    publicUrl: string
    allowRegistrations: boolean
    createDemoAccount: boolean
    googleAnalyticsTrackingCode?: string
    googleAuthKey?: string
    backgroundThreads: number
    databaseUpdateThreads: number
    smtpHost?: string
    smtpPort?: number
    smtpTls?: boolean
    smtpUserName?: string
    smtpPassword?: string
    smtpFromAddress?: string
    graphiteEnabled?: boolean
    graphitePrefix?: string
    graphiteHost?: string
    graphitePort?: number
    graphiteInterval?: number
    heavyLoad: boolean
    pubsubhubbub: boolean
    imageProxyEnabled: boolean
    queryTimeout: number
    keepStatusDays: number
    maxFeedCapacity: number
    refreshIntervalMinutes: number
    cache: ApplicationSettingsCache
    announcement?: string
    userAgent?: string
    unreadThreshold?: Date
}

export interface Category {
    id: string
    parentId?: string
    name: string
    children: Category[]
    feeds: Subscription[]
    expanded: boolean
    position: number
}

export interface CategoryModificationRequest {
    id: number
    name?: string
    parentId?: string
    position?: number
}

export interface CollapseRequest {
    id: number
    collapse: boolean
}

export interface Entries {
    name: string
    message?: string
    errorCount: number
    feedLink: string
    timestamp: number
    hasMore: boolean
    offset?: number
    limit?: number
    entries: Entry[]
    ignoredReadStatus: boolean
}

export interface Entry {
    id: string
    guid: string
    title: string
    content: string
    categories?: string
    rtl: boolean
    author?: string
    enclosureUrl?: string
    enclosureType?: string
    mediaDescription?: string
    mediaThumbnailUrl?: string
    mediaThumbnailWidth?: number
    mediaThumbnailHeight?: number
    date: number
    insertedDate: number
    feedId: string
    feedName: string
    feedUrl: string
    feedLink: string
    iconUrl: string
    url: string
    read: boolean
    starred: boolean
    markable: boolean
    tags: string[]
}

export interface FeedInfo {
    url: string
    title: string
}

export interface FeedInfoRequest {
    url: string
}

export interface FeedModificationRequest {
    id: number
    name?: string
    categoryId?: string
    position?: number
    filter?: string
}

export interface IDRequest {
    id: number
}

export interface LoginRequest {
    name: string
    password: string
}

export interface MarkRequest {
    id: string
    read: boolean
    olderThan?: number
    keywords?: string
    excludedSubscriptions?: number[]
}

export interface MultipleMarkRequest {
    requests: MarkRequest[]
}

export interface PasswordResetRequest {
    email: string
}

export interface ProfileModificationRequest {
    currentPassword: string
    email: string
    newPassword?: string
    newApiKey?: boolean
}

export interface RegistrationRequest {
    name: string
    password: string
    email: string
}

export interface ServerInfo {
    announcement?: string
    version: string
    gitCommit: string
    allowRegistrations: boolean
    googleAnalyticsCode?: string
    smtpEnabled: boolean
}

export interface Settings {
    language: string
    readingMode: ReadingMode
    readingOrder: ReadingOrder
    viewMode: ViewMode
    showRead: boolean
    scrollMarks: boolean
    theme?: string
    customCss?: string
    scrollSpeed: number
    email: boolean
    gmail: boolean
    facebook: boolean
    twitter: boolean
    googleplus: boolean
    tumblr: boolean
    pocket: boolean
    instapaper: boolean
    buffer: boolean
    readability: boolean
}

export interface StarRequest {
    id: string
    feedId: number
    starred: boolean
}

export interface SubscribeRequest {
    url: string
    title: string
    categoryId?: string
}

export interface Subscription {
    id: number
    name: string
    message?: string
    errorCount: number
    lastRefresh?: number
    nextRefresh?: number
    feedUrl: string
    feedLink: string
    iconUrl: string
    unread: number
    categoryId?: string
    position?: number
    newestItemTime?: number
    filter?: string
}

export interface TagRequest {
    entryId: number
    tags: string[]
}

export interface GetEntriesRequest {
    id: string
    readType?: ReadingMode
    newerThan?: number
    order?: ReadingOrder
    keywords?: string
    onlyIds?: boolean
    excludedSubscriptionIds?: string
    tag?: string
}

export interface GetEntriesPaginatedRequest extends GetEntriesRequest {
    offset: number
    limit: number
}

export interface UnreadCount {
    feedId?: number
    unreadCount?: number
    newestItemTime?: number
}

export interface UserModel {
    id: number
    name: string
    email?: string
    apiKey?: string
    password?: string
    enabled: boolean
    created: number
    lastLogin?: number
    admin: boolean
}

export type ApplicationSettingsCache = "NOOP" | "REDIS"

export type ReadingMode = "all" | "unread"

export type ReadingOrder = "asc" | "desc"

export type ViewMode = "title" | "expanded"
