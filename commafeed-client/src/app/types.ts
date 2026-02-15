export type ReadingMode = "all" | "unread"

export type ReadingOrder = "asc" | "desc"

export type ViewMode = "title" | "cozy" | "detailed" | "expanded"

export type ScrollMode = "always" | "never" | "if_needed"

export type IconDisplayMode = "always" | "never" | "on_desktop" | "on_mobile"

export interface AddCategoryRequest {
    name: string
    parentId?: string
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
    position: number
    newestItemTime?: number
    filter?: string
    filterLegacy?: string
    notifyOnNewEntries: boolean
}

export interface Category {
    id: string
    parentId?: string
    parentName?: string
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
    notifyOnNewEntries?: boolean
}

export interface GetEntriesRequest {
    id: string
    readType?: ReadingMode
    newerThan?: number
    order?: ReadingOrder
    keywords?: string
    excludedSubscriptionIds?: string
    tag?: string
}

export interface GetEntriesPaginatedRequest extends GetEntriesRequest {
    offset: number
    limit: number
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
    insertedBefore?: number
    keywords?: string
    excludedSubscriptions?: number[]
}

export interface MetricCounter {
    count: number
}

export interface MetricGauge {
    value: number
}

export interface MetricMeter {
    count: number
    m15_rate: number
    m1_rate: number
    m5_rate: number
    mean_rate: number
    units: string
}

export interface MetricTimer {
    count: number
    max: number
    mean: number
    min: number
    p50: number
    p75: number
    p95: number
    p98: number
    p99: number
    p999: number
    stddev: number
    m15_rate: number
    m1_rate: number
    m5_rate: number
    mean_rate: number
    duration_units: string
    rate_units: string
}

export interface Metrics {
    counters: Record<string, MetricCounter>
    gauges: Record<string, MetricGauge>
    meters: Record<string, MetricMeter>
    timers: Record<string, MetricTimer>
}

export interface MultipleMarkRequest {
    requests: MarkRequest[]
}

export interface PasswordResetRequest {
    email: string
}

export interface PasswordResetConfirmationRequest {
    email: string
    token: string
    password: string
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

export interface InitialSetupRequest {
    name: string
    password: string
    email?: string
}

export interface ServerInfo {
    announcement?: string
    version: string
    gitCommit: string
    allowRegistrations: boolean
    emailAddressRequired: boolean
    smtpEnabled: boolean
    demoAccountEnabled: boolean
    websocketEnabled: boolean
    websocketPingInterval: number
    treeReloadInterval: number
    forceRefreshCooldownDuration: number
    initialSetupRequired: boolean
    minimumPasswordLength: number
}

export interface SharingSettings {
    email: boolean
    gmail: boolean
    facebook: boolean
    twitter: boolean
    tumblr: boolean
    pocket: boolean
    instapaper: boolean
    buffer: boolean
}

export type NotificationService = "disabled" | "ntfy" | "gotify" | "pushover"

export interface NotificationSettings {
    enabled: boolean
    type?: "ntfy" | "gotify" | "pushover"
    serverUrl?: string
    token?: string
    userKey?: string
    topic?: string
}

export interface Settings {
    language?: string
    readingMode: ReadingMode
    readingOrder: ReadingOrder
    showRead: boolean
    scrollMarks: boolean
    customCss?: string
    customJs?: string
    scrollSpeed: number
    scrollMode: ScrollMode
    entriesToKeepOnTopWhenScrolling: number
    starIconDisplayMode: IconDisplayMode
    externalLinkIconDisplayMode: IconDisplayMode
    markAllAsReadConfirmation: boolean
    markAllAsReadNavigateToNextUnread: boolean
    customContextMenu: boolean
    mobileFooter: boolean
    unreadCountTitle: boolean
    unreadCountFavicon: boolean
    disablePullToRefresh: boolean
    primaryColor?: string
    sharingSettings: SharingSettings
    notificationSettings: NotificationSettings
}

export interface LocalSettings {
    viewMode: ViewMode
    sidebarWidth: number
    announcementHash: string
    fontSizePercentage: number
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
    notifyOnNewEntries: boolean
}

export interface TagRequest {
    entryId: number
    tags: string[]
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
    lastForceRefresh?: number
}

export interface AdminSaveUserRequest {
    id?: number
    name: string
    email?: string
    password?: string
    enabled: boolean
    admin: boolean
}

export interface AuthenticationError {
    message: string
    allowRegistrations: boolean
}
