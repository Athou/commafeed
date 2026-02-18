# Auto-Mark-As-Read

The **Auto-Mark-As-Read** feature allows you to keep your feed list clean by automatically marking older entries as read once they reach a certain age.
For example, imagine you subscribe to an RSS feed of **Upcoming Local Events** that you use to keep track of what is happening in your area, and to plan for events you want to attend.  Once an entry is a few days old, it isn't much use -- the event it announces may already be over.  By setting the **Auto-ark as read** value to `3` or `5` days, the stale events will automatically disappear from your feed, ensuring your feed always stays focused on *future* events without any manual cleanup required.

Note that older entries are simply marked as `read`, so if you change your mind, you can always switch your feed to show `read` entries to see the older entries.

## How to Use

1.  **Open Feed Details**: Navigate to the feed you want to configure and click on its name or the "Edit" icon.
2.  **Set the Limit**: Look for the **"Auto-mark as read"** field.
3.  **Enter Days**: Input the number of days after which entries should be marked as read. For example, if you enter `7`, any entry older than a week will automatically be marked as read.
4.  **Save**: Click the "Save" button.

### Immediate Effect
When you save a new limit, CommaFeed will immediately scan your existing unread entries for that feed. Any entries that are already older than the limit you just set will be marked as read instantly.

### Background Updates
A background task runs periodically (every hour) to check for and mark expired entries across all your feeds with this setting enabled.

## Disabling the Feature

To disable the feature for a specific feed:
- Set the **"Auto-mark as read"** value back to `0` or clear the field.
- Click **Save**.

This will remove the expiration dates from all existing entries in that feed and prevent future entries from being automatically marked as read.

