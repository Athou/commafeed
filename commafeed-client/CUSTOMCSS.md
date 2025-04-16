# Custom CSS Guide

On the Admin settings page, there is a tab for "Custom Code" where you can enter [CSS](https://en.wikipedia.org/wiki/CSS) to customize the look & feel of CommaFeed.  To facilitate this, many of the HTML elements of CommaFeed have been given specific class names.  For example, the header of a feed entry is enclosed in an HTML `<div>` element with the class name `cf-FeedEntryHeader`.  So if you wanted to modify the appearance of all feed entry headers, you could use that specific class name to select all feed entry headers and apply whatever changes you desired.  For example, if you want to make change the background color of the headers, you could add this to the custom CSS code:

```
.cf-FeedEntryHeader {background-color: lightblue;}
```

The tables below list all of the CommaFeed specific class names and the corresponding page elements.  The names are hierarchical, so a name like `cf-FeedEntryHeader-Flex1-Flex-Box` refers to a Box element within a Flex element within another Flex element within the cf-FeedEntryHeader element.

The elements within these class names (e.g., Box, Flex, etc.) follow the naming scheme in the CommaFeed source code (and hence, the naming scheme in the [Mantine library](https://mantine.dev/).  The actual HTML elements in the rendered page are often different.  For example, a Mantine Box element becomes an HTML `<div>`.

When you want to modify the CommaFeed interface, you may be able to browse the tables below and find the right element to change.  However, a more useful approach is to use your web browser's built-in tools to find the class name of the element you want to change.  Typically you can hit F12 to bring up the web developer tools and then use the Inspector to look at the HTML element you want to change.  On that element you should see the class name you can use to write your custom CSS.

(If the element doesn't have a usable class name, the easiest approach is to find an enclosing element that does have a class name and write a CSS selector starting at that element.)

## Extended Example
The extended example below modifies the CommaFeed interface to be simpler and more compact and illustrates a variety of uses of the CommaFeed class names and CSS.

```
/* General changes applied to everything */
main {font-size: 14px; font-family: sans-serif; line-height: 1.35; padding-top:
/* Don't force font-size on blockquotes and make them italic */
blockquote {font-size: unset !important; font-style: italic;}
/* Specific font and layout changes for the entire navbar */
.cf-Layout-Box-AppShell-Navbar {font-size: 14px; font-weight: 700; font-family: sans-serif; line-height: 150%; top: 30px !important;}
/* Make unread category names black */
.cf-TreeNode-Box {color: black !important;}
/* Remove the favicons for the feeds in the navbar */
.cf-Layout-Box-AppShell-Navbar-Section-Box img {width: 0; height: 0;}
/* Remove the divider and button bar at the bottom of feed entries */
.cf-FeedEntry-Paper-Box-Divider {display: none;}
.cf-FeedEntryFooter {display: none;}
/* Remove unwanted header lines in feeds */
.cf-FeedEntryHeader-Flex2 {display: none;}
.cf-FeedEntryHeader-Box {display: none;}
/* Make the unread counts lighter, gray and in parens */
.cf-UnreadCount-Badge {display: flex; font-weight: 300; color: gray; background-color: unset; align-items: unset;}
.cf-UnreadCount-Badge::before {content: "(";}
.cf-UnreadCount-Badge::after {content: ")";}
.cf-TreeNode-Box-Box1 {margin-right: 0}
/* Make the header much more compact */
.cf-Layout-Box-AppShell-Header > div > div {padding-bottom: 0 !important; padding-top: 0 !important;}
.cf-Header-Center {justify-content: unset !important;}
.cf-Layout-Box-AppShell-Header {height: unset !important;}
.cf-Layout-Box-AppShell-Header img {width: calc(1rem) !important;}
.cf-Layout-Center-Title {font-size: 16px !important;}
/* Some examples where CommaFeed-specific class names are not available */ 
/* Make headers a reasonable size */
h3 {font-size: 16px !important;}
h2 {font-size: 16px !important;}
h1 {font-size: 16px !important;}
/* Let buttons be small
main > button {min-width: unset !important; min-height: unset !important;}
/* Add an extra space before the submitted line on Reddit. */
article span > div::after {content: "\A"; white-space: pre;}
/* Make all the button icons black */
header svg {stroke: black !important; }
main > svg {stroke: black !important; }
/* Make links in articles light blue with a hover underline */ 
article a:not([class]) { color:#428bca; text-decoration:none; }
article a:not([class]):hover { text-decoration:underline; }
```

## CommaFeed Class Names

File:video
|Class Name|Element Description|
|---|---|
|cf-video|An embedded video|

File:audio
|Class Name|Element Description|
|---|---|
|cf-audio|An embedded audio|

File:content/BasicHtmlStyles
|Class Name|Element Description|
|---|---|
|cf-BasicHtmlStyles|Encloses component to apply basic html styles|

File:content/FeedEntries
|Class Name|Element Description|
|---|---|
|cf-InfiniteScroll|All of the FeedEntries|

File:content/FeedEntry
|Class Name|Element Description|
|---|---|
|cf-FeedEntry-Paper|Entire feed entry|
|cf-FeedEntry-Paper-A|Title to feed entry|
|cf-FeedEntry-Paper-A-Box|Box around title text; encloses FeedEntryHeader or FeedEntryCompactHeader|
|cf-FeedEntry-Paper-Box|Encloses FeedEntryBody|
|cf-FeedEntry-Paper-Box-Box|Encloses FeedEntryBody|
|cf-FeedEntry-Paper-Box-Divider|Divider line at bottom of FeedEntry|

File:content/FeedEntryBody
|Class Name|Element Description|
|---|---|
|cf-FeedEntryBody-Box|Entire FeedEntryBody|
|cf-FeedEntryBody-Box-Box|Encloses Content|
|cf-FeedEntryBody-Box-EnclosureBox|(Possible) Enclosure at end of FeedEntryBody|
|cf-FeedEntryBody-Box-MediaBox|(Possible) Media at end of FeedEntryBody|

File:content/Content
|Class Name|Element Description|
|---|---|
|cf-Content|Entire content|

File:content/FeedEntryFooter
|Class Name|Element Description|
|---|---|
|cf-FeedEntryFooter|Entire FeedEntry footer|
|cf-FeedEntryFooter-Group|Group within footer|
|cf-FeedEntryFooter-Group-ActionButton|All action buttons within the footer|
|cf-FeedEntryFooter-Group-ActionButton-Mail|Mail action button|
|cf-FeedEntryFooter-Group-ActionButton-Star|Star action button|
|cf-FeedEntryFooter-Group-OpenLink|HTML link enclosing open link|
|cf-FeedEntryFooter-Group-ActionButton-OpenLink|Open link action button|
|cf-FeedEntryFooter-Group-ActionButton-MarkEntries|Mark entries action button|
|

File:content/ShareButtons
|Class Name|Element Description|
|---|---|
|cf-ShareButton-ActionIcon|Sharing button action icon|
|cf-ShareButton-ActionIcon-Box|Box within the action icon|
|cf-ShareButtons-SimpleGrid-Native|Grid of native sharing buttons|
|cf-Sharebuttons-Divider|Divider between native sharing buttons and site sharing buttons|
|cf-ShareButtons-SimpleGrid-Sharing|Grid of site sharing buttons|

File:content/header/FeedEntryHeader
|Class Name|Element Description|
|---|---|
|cf-FeedEntryHeader|Overall FeedEntry header|
|cf-FeedEntryHeader-Flex1|First flex box inside FeedEntryHeader ("main")|
|cf-FeedEntryHeader-Flex1-Flex|Nested flex box inside FeedEntryHeader|
|cf-FeedEntryHeader-Flex1-Flex-Box|Box enclosing star icon within FeedEntryHeader|
|cf-FeedEntryHeader-Flex2|Second flex box inside FeedEntryHeader ("details")|
|cf-FeedEntryHeader-Flex2-Text|Feed name and relative date|
|cf-FeedEntryHeader-Box|Box containing feed details (if expanded)|
|cf-FeedEntryHeader-Box-Text|Text of author and categories|

File:content/header/FeedEntryCompactHeader
|Class Name|Element Description|
|---|---|
|cf-FeedEntryCompactHeader|Overall FeedEntry compact header|
|cf-FeedEntryCompactHeader-Box1|Box enclosing the FeedFavicon within the header|
|cf-FeedEntryCompactHeader-Text1 (On Desktop) Feed name within the header|
|cf-FeedEntryCompactHeader-Box2|Box enclosing the FeedEntryTitle within the header|
|cf-FeedEntryCompactHeader-Text2|(On Desktop) Date within the header|

File:components/header/Header
|Class Name|Element Description|
|---|---|
|cf-Header-Divider|Divider between button groups in the header toolbar|
|cf-Header-Toolbar-Box|(On desktop) Box for the header toolbar|
|cf-Header-Toolbar-Group|(On mobile) Group for the header toolbar|
|cf-Header-Center|Overall center element for header|
|cf-Header-Toolbar-ActionButton|All header toolbar action buttons|
|cf-Header-Toolbar-ActionButton-ArrowUp|Header toolbar arrow up action button|
|cf-Header-Toolbar-ActionButton-ArrowDown|Header toolbar arrow down action button|
|cf-Header-Toolbar-ActionButton-Refresh|Header toolbar refresh action button|
|cf-Header-Toolbar-ActionButton-MarkAllRead|Header toolbar mark all read action button|
|cf-Header-Toolbar-ActionButton-UnReadToggle|Header toolbar toggle all/unread action button|
|cf-Header-Toolbar-ActionButton-Order|Header toolbar ascending/descending action button|
|cf-Header-Toolbar-ActionButton-Search|Header toolbar popover search action button|
|cf-Header-Toolbar-Popover-Form|Header toolbar popover search form|
|cf-Header-Toolbar-Popover-Form-TextInput|Header toolbar popover search form text input field|
|cf-Header-Toolbar-ActionButton-Profile|Header toolbar profile action button|
|cf-Header-Toolbar-ActionButton-ExtensionOptions|Header toolbar extension options action button|
|cf-Header-Toolbar-ActionButton-OpenCommaFeed|Header toolbar open CommaFeed action button|

File:components/header/ProfileMenu
|Class Name|Element Description|
|---|---|
|cf-ProfileMenuControlItem-Group|Group around a profile menu item|
|cf-ProfileMenuControlItem-Box|Box around the label in a profile menu item|
|cf-ProfileMenu-Item|All profile menu items|
|cf-ProfileMenu-Item-Settings|Profile menu settings item|
|cf-ProfileMenu-Item-Refresh|Profile menu refresh feeds item|
|cf-ProfileMenu-Divider|Divider between profile menu items|
|cf-ProfileMenu-Label|Profile menu label|
|cf-ProfileMenu-SegmentedControl-Theme|Theme control|
|cf-ProfileMenu-SegmentedControl-ViewMode|ViewMode control|
|cf-ProfileMenu-Item-Admin Profile menu admin item|
|cf-ProfileMenu-Item-Metrics|Profile menu metrics item|
|cf-ProfileMenu-Item-Donate|Profile menu donate item|
|cf-ProfileMenu-Item-About|Profile menu about item|
|cf-ProfileMenu-Item-Logout|Profile menu logout item|

File:components/sidebar/Tree
|Class Name|Element Description|
|---|---|
|cf-Tree-Stack|Tree of all feeds in sidebar|
|cf-Tree-Stack-Box|Box within tree of all feeds|

File:components/sidebar/TreeNode
|Class Name|Element Description|
|---|---|
|cf-TreeNode-Box|Tree node|
|cf-TreeNode-Box-Box1|Tree node properties box|
|cf-TreeNode-Box-Box1-Center|Tree node properties icon|
|cf-TreeNode-Box-Box2|Tree node name|
|cf-TreeNode-Box-Box3|Tree node unread count|

File:components/sidebar/TreeSearch
|Class Name|Element Description|
|---|---|
|cf-TreeSearch-TextInput|Tree search text input field|
|cf-TreeSearch-Spotlight|Tree search spotlight|

File:components/sidebar/UnreadCount
|Class Name|Element Description|
|---|---|
|cf-UnreadCount|Tree unread count tooltip|
|cf-UnreadCount-Badge|Tree unread count badge|

File:pages/PageTitle
|Class Name|Element Description|
|---|---|
|cf-PageTitle-Center|Page title container|
|cf-PageTitle-Center-Title|Title text|

File:pages/WelcomePage
|Class Name|Element Description|
|---|---|
|cf-WelcomePage-Container|Welcome page container|
|cf-WelcomePage-Container-Center|Welcome page title centering|
|cf-WelcomePage-Container-Center-Title|Welcome page title text|
|cf-WelcomePage-Container-Divider1|Welcome page first divider|
|cf-WelcomePage-Container-Image|Welcome page image|
|cf-WelcomePage-Container-Divider2|Welcome page second divider|
|cf-WelcomePage-Container-Space|Welcome page space after footer|

File:pages/LoadingPage
|Class Name|Element Description|
|---|---|
|cf-LoadingPage-Container|Loading page container|
|cf-LoadingPage-Container-Center|Centering element for loading ring|
|cf-LoadingPage-Container-Center-RingProgress|Progress ring|
|cf-LoadingPage-Container-Center-RingProgress-Text|Loading percentage text|
|cf-LoadingPage-Container-StepLabel|Step label|

File:pages/ErrorPage
|Class Name|Element Description|
|---|---|
|cf-ErrorPage-div|Error page outer div|
|cf-ErrorPage-Container|Error page container|
|cf-ErrorPage-Container-Box|Error page box containing "Oops!"|
|cf-ErrorPage-Container-Title|Error title|
|cf-ErrorPage-Container-Text|Error text|
|cf-ErrorPage-Container-Group|Refresh page button group|
|cf-ErrorPage-Container-Button|Refresh page button|

File:pages/app/AboutPage
|Class Name|Element Description|
|---|---|
|cf-AboutPage-Section-Box|Top level about page box section|
|cf-AboutPage-Section-Box-Box1|Title box in about page section|
|cf-AboutPage-Section-Box-Box1-Title|About page section title|
|cf-AboutPage-Section-Box-Box2|Remainder of section|
|cf-AboutPage-NextUnreadBookmarklet|Next unread bookmarklet|
|cf-AboutPage-Container|About page container|
|cf-AboutPage-Container-SimpleGrid|About page grid|
|cf-AboutPage-Container-SimpleGrid-Section-Box|Box within section|
|cf-AboutPage-Container-SimpleGrid-Section-List|List within section|
|cf-AboutPage-Container-SimpleGrid-Section-List-Item|List item within section|

File:pages/app/AddPage
|Class Name|Element Description|
|---|---|
|cf-AddPage-Container|Top level add page container|
|cf-AddPage-Container-Tabs|Tabs within add page container|

File:pages/app/ApiDocumentation
|Class Name|Element Description|
|---|---|
|cf-ApiDocumentation-Box|Top level box on API Documentation page|

File:page/app/CategoryDetailsPage
|Class Name|Element Description|
|---|---|
|cf-CategoryDetails-Container|Top level container on Category Details page|
|cf-CategoryDetails-Container-Box1|Modify category error box|
|cf-CategoryDetails-Container-Box2|Delete category error box|
|cf-CategoryDetails-Container-Form|Category details form|
