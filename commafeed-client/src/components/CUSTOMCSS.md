# CommaFeed CSS Guide

admin/UserEdit.tsx

video
	cf-video	An embedded video

audio
	cf-audio	An embedded audio

content/BasicHtmlStyles
	cf-BasicHtmlStyles	Encloses component to apply basic html styles

content/FeedEntries
	cf-InfiniteScroll	All of the FeedEntries

content/FeedEntry
	cf-FeedEntry-Paper	Entire feed entry
        cf-FeedEntry-Paper-A	Title to feed entry
	cf-FeedEntry-Paper-A-Box	Box around title text; encloses FeedEntryHeader or FeedEntryCompactHeader
	cf-FeedEntry-Paper-Box	Encloses FeedEntryBody
	cf-FeedEntry-Paper-Box-Box	Encloses FeedEntryBody
	cf-FeedEntry-Paper-Box-Divider	Divider line at bottom of FeedEntry

content/FeedEntryBody
        cf-FeedEntryBody-Box	Entire FeedEntryBody
        cf-FeedEntryBody-Box-Box	Encloses Content
        cf-FeedEntryBody-Box-EnclosureBox	(Possible) Enclosure at end of FeedEntryBody
        cf-FeedEntryBody-Box-MediaBox	(Possible) Media at end of FeedEntryBody

content/Content
	cf-Content	Entire content

content/FeedEntryFooter
	cf-FeedEntryFooter	Entire FeedEntry footer
	cf-FeedEntryFooter-Group	Group within footer
	cf-FeedEntryFooter-Group-ActionButton	All action buttons within the footer
	cf-FeedEntryFooter-Group-ActionButton-Mail	Mail action button
	cf-FeedEntryFooter-Group-ActionButton-Star	Star action button
	cf-FeedEntryFooter-Group-OpenLink	HTML link enclosing open link
	cf-FeedEntryFooter-Group-ActionButton-OpenLink	Open link action button
	cf-FeedEntryFooter-Group-ActionButton-MarkEntries	Mark entries action button
	
content/ShareButtons
	cf-ShareButton-ActionIcon	Sharing button action icon
	cf-ShareButton-ActionIcon-Box	Box within the action icon
	cf-ShareButtons-SimpleGrid-Native	Grid of native sharing buttons
	cf-Sharebuttons-Divider	Divider between native sharing buttons and site sharing buttons
	cf-ShareButtons-SimpleGrid-Sharing	Grid of site sharing buttons

content/header/FeedEntryHeader
	cf-FeedEntryHeader	Overall FeedEntry header
	cf-FeedEntryHeader-Flex1	First flex box inside FeedEntryHeader ("main")
	cf-FeedEntryHeader-Flex1-Flex	Nested flex box inside FeedEntryHeader
	cf-FeedEntryHeader-Flex1-Flex-Box	Box enclosing star icon within FeedEntryHeader
	cf-FeedEntryHeader-Flex2	Second flex box inside FeedEntryHeader ("details")
	cf-FeedEntryHeader-Flex2-Text	Feed name and relative date
	cf-FeedEntryHeader-Box	Box containing feed details (if expanded)
	cf-FeedEntryHeader-Box-Text	Text of author and categories

content/header/FeedEntryCompactHeader
	cf-FeedEntryCompactHeader	Overall FeedEntry compact header
	cf-FeedEntryCompactHeader-Box1	Box enclosing the FeedFavicon within the header
	cf-FeedEntryCompactHeader-Text1 (On Desktop) Feed name within the header
	cf-FeedEntryCompactHeader-Box2	Box enclosing the FeedEntryTitle within the header
	cf-FeedEntryCompactHeader-Text2	(On Desktop) Date within the header

components/header/Header
	cf-Header-Divider	Divider between button groups in the header toolbar
	cf-Header-Toolbar-Box	(On desktop) Box for the header toolbar
	cf-Header-Toolbar-Group	(On mobile) Group for the header toolbar
	cf-Header-Center	Overall center element for header
	cf-Header-Toolbar-ActionButton	All header toolbar action buttons
	cf-Header-Toolbar-ActionButton-ArrowUp	Header toolbar arrow up action button
	cf-Header-Toolbar-ActionButton-ArrowDown	Header toolbar arrow down action button
	cf-Header-Toolbar-ActionButton-Refresh	Header toolbar refresh action button
	cf-Header-Toolbar-ActionButton-MarkAllRead	Header toolbar mark all read action button
	cf-Header-Toolbar-ActionButton-UnReadToggle	Header toolbar toggle all/unread action button
	cf-Header-Toolbar-ActionButton-Order	Header toolbar ascending/descending action button
	cf-Header-Toolbar-ActionButton-Search	Header toolbar popover search action button
	cf-Header-Toolbar-Popover-Form	Header toolbar popover search form
	cf-Header-Toolbar-Popover-Form-TextInput	Header toolbar popover search form text input field
	cf-Header-Toolbar-ActionButton-Profile	Header toolbar profile action button
	cf-Header-Toolbar-ActionButton-ExtensionOptions	Header toolbar extension options action button
	cf-Header-Toolbar-ActionButton-OpenCommaFeed	Header toolbar open CommaFeed action button

components/header/ProfileMenu
	cf-ProfileMenuControlItem-Group	Group around a profile menu item
	cf-ProfileMenuControlItem-Box	Box around the label in a profile menu item
	cf-ProfileMenu-Item	All profile menu items
	cf-ProfileMenu-Item-Settings	Profile menu settings item
	cf-ProfileMenu-Item-Refresh	Profile menu refresh feeds item
	cf-ProfileMenu-Divider	Divider between profile menu items
	cf-ProfileMenu-Label	Profile menu label
	cf-ProfileMenu-SegmentedControl-Theme	Theme control
	cf-ProfileMenu-SegmentedControl-ViewMode	ViewMode control
	cf-ProfileMenu-Item-Admin Profile menu admin item
	cf-ProfileMenu-Item-Metrics	Profile menu metrics item
	cf-ProfileMenu-Item-Donate	Profile menu donate item
	cf-ProfileMenu-Item-About	Profile menu about item
	cf-ProfileMenu-Item-Logout	Profile menu logout item
	