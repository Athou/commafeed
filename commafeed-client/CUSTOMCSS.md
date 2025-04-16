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

components/sidebar/Tree
	cf-Tree-Stack	Tree of all feeds in sidebar
	cf-Tree-Stack-Box	Box within tree of all feeds

components/sidebar/TreeNode
	cf-TreeNode-Box	Tree node
	cf-TreeNode-Box-Box1	Tree node properties box
	cf-TreeNode-Box-Box1-Center	Tree node properties icon
	cf-TreeNode-Box-Box2	Tree node name
	cf-TreeNode-Box-Box3	Tree node unread count

components/sidebar/TreeSearch
	cf-TreeSearch-TextInput	Tree search text input field
	cf-TreeSearch-Spotlight	Tree search spotlight

comoponents/sidebar/UnreadCount
	cf-UnreadCount	Tree unread count tooltip
	cf-UnreadCount-Badge	Tree unread count badge

pages/PageTitle
	cf-PageTitle-Center	Page title container
	cf-PageTitle-Center-Title	Title text


pages/WelcomePage
	cf-WelcomePage-Container	Welcome page container
	cf-WelcomePage-Container-Center	Welcome page title centering
	cf-WelcomePage-Container-Center-Title	Welcome page title text
	cf-WelcomePage-Container-Divider1	Welcome page first divider
	cf-WelcomePage-Container-Image	Welcome page image
	cf-WelcomePage-Container-Divider2	Welcome page second divider
	cf-WelcomePage-Container-Space	Welcome page space after footer

pages/LoadingPage
	cf-LoadingPage-Container	Loading page container
	cf-LoadingPage-Container-Center	Centering element for loading ring
	cf-LoadingPage-Container-Center-RingProgress Progress ring
	cf-LoadingPage-Container-Center-RingProgress-Text	Loading percentage text
	cf-LoadingPage-Container-StepLabel	Step label

pages/LoadingPage
	cf-ErrorPage-div	Error page outer div
	cf-ErrorPage-Container	Error page container
	cf-ErrorPage-Container-Box	Error page box containing "Oops!"
	cf-ErrorPage-Container-Title	Error title
	cf-ErrorPage-Container-Text	Error text
	cf-ErrorPage-Container-Group	Refresh page button group
	cf-ErrorPage-Container-Button	Refresh page button

pages/app/AboutPage
	cf-AboutPage-Section-Box	Top level about page box section
	cf-AboutPage-Section-Box-Box1	Title box in about page section
	cf-AboutPage-Section-Box-Box1-Title	About page section title
	cf-AboutPage-Section-Box-Box2	Remainder of section
	cf-AboutPage-NextUnreadBookmarklet	Next unread bookmarklet
	cf-AboutPage-Container	About page container
	cf-AboutPage-Container-SimpleGrid	About page grid
	cf-AboutPage-Container-SimpleGrid-Section-Box Box within section
	cf-AboutPage-Container-SimpleGrid-Section-List List within section
	cf-AboutPage-Container-SimpleGrid-Section-List-Item List item within section

pages/app/AddPage
	cf-AddPage-Container	Top level add page container
	cf-AddPage-Container-Tabs	Tabs within add page container

pages/app/ApiDocumentation
	cf-ApiDocumentation-Box	Top level box on API Documentation page

page/app/CategoryDetailsPage
	cf-CategoryDetails-Container	Top level container on Category Details page
	cf-CategoryDetails-Container-Box1 Modify category error box
	cf-CategoryDetails-Container-Box2 Delete category error box
	cf-CategoryDetails-Container-Form	Category details form
	
page/app/DonatePage
	cf-DonatePage-Container	Top level container on Donate page
	cf-DonatePage-Container-Group	Group enclosing heart
	cf-DonatePage-Container-Group-Heart	Heart
	cf-DonatePage-Container-Group-Title	Title within heart group
	cf-DonatePage-Container-Box	Box containing donation message
	cf-DonatePage-Container-Group-Box-List	List of donation methods
	
page/app/FeedDetailsPage
	cf-FeedDetails-Container	Top level container on Feed Details page
	cf-FeedDetails-Container-ModifyFeedError	Box enclosing modify feed error
	cf-FeedDetails-Container-UnsubscribeError	Box enclosing unsubscribe error
	cf-FeedDetails-Container-Form	Feed details form
	cf-FeedDetails-Container-Form-Stack	Stack within form
	cf-FeedDetails-Container-Form-Stack-Title	Feed name at top of form
	cf-FeedDetails-Container-Form-Stack-InputWrapper1	Input wrapper for feed url
	cf-FeedDetails-Container-Form-Stack-InputWrapper1-Box	Box around feed URL
	cf-FeedDetails-Container-Form-Stack-InputWrapper1-Box-Anchor	Link for feed URL
	cf-FeedDetails-Container-Form-Stack-InputWrapper2	Input wrapper for website link
	cf-FeedDetails-Container-Form-Stack-InputWrapper2-Box	Box around website URL
	cf-FeedDetails-Container-Form-Stack-InputWrapper2-Box-Anchor Link for website URL
	cf-FeedDetails-Container-Form-Stack-InputWrapper3	Input wrapper for feed last refresh
	cf-FeedDetails-Container-Form-Stack-InputWrapper3-Box	Box around feed last refresh
	cf-FeedDetails-Container-Form-Stack-InputWrapper4	Input wrapper for last refresh message
	cf-FeedDetails-Container-Form-Stack-InputWrapper4-Box	Box around last refresh message
	cf-FeedDetails-Container-Form-Stack-InputWrapper5	Input wrapper around next refresh date
	cf-FeedDetails-Container-Form-Stack-InputWrapper5-Box	Box around feed nexst refresh
	cf-FeedDetails-Container-Form-Stack-InputWrapper6	Input wrapper for generated feed url
	cf-FeedDetails-Container-Form-Stack-InputWrapper6-Box	Box around generated feed URL
	cf-FeedDetails-Container-Form-Stack-InputWrapper6-Box-Anchor Link for generate feed URL
	cf-FeedDetails-Container-Form-Stack-Divider	Form divider
	cf-FeedDetails-Container-Form-Stack-TextInput1	Text input for feed name
	cf-FeedDetails-Container-Form-Stack-CategorySelect	Category selector
	cf-FeedDetails-Container-Form-Stack-NumberInput	Number input for position
	cf-FeedDetails-Container-Form-Stack-TextInput2	Text input for filtering expression
	f-FeedDetails-Container-Form-Stack-Group	Button group
	cf-FeedDetails-Container-Form-Stack-Group-Button1	Cancel button
	cf-FeedDetails-Container-Form-Stack-Group-Button2	Save button
	cf-FeedDetails-Container-Form-Stack-Group-Divider	Vertical divider
	cf-FeedDetails-Container-Form-Stack-Group-Button3	Unsubscribe button
	
page/app/FeedEntriesPage
	cf-FeedEntries-Box	Top level box on Feed Entries page
	cf-FeedEntries-Box-Group	Group for source website info
	cf-FeedEntries-Box-Group-Link	Link to source website
	cf-FeedEntries-Box-Group-Title	Name of source website
	cf-FeedEntries-Box-Group-Link-ActionIcon	Action icon
	cf-FeedEntries-Box-Group-Divider	Divider before "No more entries"

page/app/Layout
	cf-Layout-Center	Centering around logo and title
	cf-Layout-Center-Title	Title (CommaFeed)
	cf-Layout-Box	Top-level box on Layout page
	cf-Layout-Box-AppShell	Application shell
	cf-Layout-Box-AppShell-Header	App shell header
	cf-Layout-Box-AppShell-Footer	App shell footer
	cf-Layout-Box-AppShell-Navbar	App shell navbar (sidebar)
	cf-Layout-Box-AppShell-Navbar-Section	Scrolling area for sidebar
	cf-Layout-Box-AppShell-Navbar-Section-Box	Box enclosing sidebar content
	
page/app/Settings
	cf-Settings-Container	Top level container for settings page
	cf-Settings-Container-Tabs	Tabs in settings page

page/app/TagDetails
	cf-TagDetails-Container	Top level container for tag details page
	cf-TagDetails-Container-Stack	Stack within container
	cf-TagDetails-Container-Stack-Title	ID
	cf-TagDetails-Container-Stack-InputWrapper	Input wrapper for generated feed url
	cf-TagDetails-Container-Stack-InputWrapper-Box	Box within input wrapper
	cf-TagDetails-Container-Stack-InputWrapper-Box-Anchor	Link for generated feed url
	cf-TagDetails-Container-Stack-Group	Button group
	cf-TagDetails-Container-Stack-Group-Button	Cancel button
