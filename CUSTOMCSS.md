# Custom CSS Guide

On the Admin settings page, there is a tab for "Custom Code" where you can enter [CSS](https://en.wikipedia.org/wiki/CSS) to customize the look & feel of CommaFeed.  To facilitate this, many of the HTML elements of CommaFeed have been given specific class names.  For example, the header of a feed entry is enclosed in an HTML `<div>` element with the class name `cf-FeedEntryHeader`.  So if you wanted to modify the appearance of all feed entry headers, you could use that specific class name to select all feed entry headers and apply whatever changes you desired.  For example, if you want to make change the background color of the headers, you could add this to the custom CSS code:

```
.cf-header {background-color: lightblue;}
```

The table below lists all of the CommaFeed specific class names and the corresponding page elements.  These elements are selected to provide a good "starting point" within each portion of the feed reading page.  To modify some elements of the page, you will have to start at one of the CommaFeed specific class names and then "drill down" to the HTML element you wish to change.  A useful approach is to use your web browser's Inspector to find the element you want to change.  (Typically you can hit F12 to bring up the web developer tools.)  Find the element you want to change.  If it has a "cf-" class name from the table below, then you can modify it directly using the formula illustrated above with "cf-header".  If it doesn't have a "cf-" classname, then search upwards through the enclosing elements until you find one that does.  Use that name as your starting point and extend your CSS selector down to the element you want to modify.

If you're having problems finding an element, the rule above that changes the background color can be helpful to see what element your selector is selecting -- or if it is not selecting anything!  

## Extended Example
The extended example below modifies the CommaFeed interface to be more like the original Google Reader interface and illustrates a variety of uses of the CommaFeed class names and CSS.  There are examples of styling CommaFeed classes directly as well as examples of more complicated selection rules.

```
/* GENERAL (changes applied to everything) */
main {font-size: 14px; font-family: sans-serif; line-height: 1.35; padding-top:  calc(1rem * 2.5) !important;}
/* Don't force font-size on blockquotes and make them italic */
blockquote {font-size: unset !important; font-style: italic;}
/* Make all the button icons black */
header svg {stroke: black !important; }
main > svg {stroke: black !important; }
/* Make links in articles light blue with a hover underline */ 
article a:not([class]) { color:#428bca; text-decoration:none; }
article a:not([class]):hover { text-decoration:underline; }
/* Make HTML headers the (same) reasonable size */
h3 {font-size: 16px !important;}
h2 {font-size: 16px !important;}
h1 {font-size: 16px !important;}
/* Make buttons actual size  */
main > button {min-width: unset !important; min-height: unset !important;}

/* HEADER (tool bar at the top of the page) */
/* Make the header more compact */
header > div > div {padding-bottom: 0 !important; padding-top: 0 !important;}
/* Let the toolbar pull to the left */
.cf-toolbar-wrapper {justify-content: unset !important;}
/* Minimize height of the toolbar */
header {height: unset !important;}
/* Move buttons closer together */
header img {width: calc(1rem) !important;}
/* No button labels, even if there's room. */
.cf-toolbar-wrapper .mantine-Button-label {display: none;}

/* SIDEBAR (where the feeds are listed) */
/* Specific font and layout changes for the entire sidebar */
.cf-tree {font-size: 14px; font-weight: 700; font-family: sans-serif; line-height: 150%; top: 30px !important;}
.cf-treenode {margin-right: 0}
/* Make unread category names black */
.cf-treenode-category {color: black !important;}
/* Remove the favicons for the feeds in the sidebar */
.cf-treenode-icon {display: none;}
/* Make the unread counts lighter, gray and in parens */
.cf-badge {display: flex; font-weight: 300; color: gray; background-color: unset; align-items: unset;}
.cf-badge::before {content: "(";}
.cf-badge::after {content: ")";}

/* FEED ENTRIES */
/* Only changes Detailed and Expanded display */
/* Remove subtitle and details in feed entries, just leaving the title */
.cf-header-subtitle {display: none;}
.cf-header-details {display: none;}
/* Remove the divider and button bar at the bottom of feed entries */
.cf-footer-divider {display: none;}
.cf-footer {display: none;}

/* MISCELLANEOUS */
/* An example of changing the content: Add an extra space before the submitted line on Reddit feed entries. */
article span > div::after {content: "\A"; white-space: pre;}
```

## CommaFeed Useful Elements
The table below shows some elements of the CommaFeed main page that are useful for applying custom CSS.  Note that these are elements, not class names, so you must use these with the leading period used to reference a class name.  For example:

```
article {background-color: lightblue;}
```

|Element Name|Element Description|
|---|---|
|main|The entire web page|
|header|The header area (logo and toolbar)|
|nav|The entire sidebar|
|footer|The footer area at the bottom of the page|
|article|Entire feed entry|
|h3, h2, h1|HTML headers|


## CommaFeed Class Names
The table below shows the CommaFeed specific class names.  To reference a class name in a CSS rule, use a leading period.For example:

```
.cf-header {background-color: lightblue;}
```

|Class Name|Element Description|
|---|---|
|cf-logo-title|The CommaFeed logo and title in upper left of page|
|cf-logo|The CommaFeed logo|
|cf-title|The CommaFeed title|
|cf-toolbar|The entire toolbar of action buttons at the top of the page|
|cf-action-button|Each button within the toolbar. (Note: also used in feed entry footer.)|
|cf-treesearch|The search box at the top of the sidebar|
|cf-tree|The entire feed tree in the sidebar|
|cf-treenode|All nodes in the feed tree|
|cf-treenode-category|Category nodes in the feed tree|
|cf-treenode-feed|Feed nodes in the feed tree|
|cf-treenode-icon|Icon within feed nodes|
|cf-treenode-unread-count|Unread count within feed nodes|
|cf-badge|The badge for the unread count|
|cf-entries-title|Title of feed currently displayed in the content area|
|cf-entries|All of the feed entries being displayed in the content area|
|cf-header|The header of a feed entry|
|cf-header-title|The first line in the header of a feed entry (the entry title)|
|cf-header-subtitle|The second line in the header of a feed entry (feed name and time of entry)|
|cf-header-details|The third line in the header of a feed entry (typically author, subject, etc.)|
|cf-content|The content (body) of a feed entry|
|cf-footer-divider|The divider between the feed entry content and the feed entry footer|
|cf-footer|The feed entry footer (buttons to share, star, etc.)|
|cf-action-button|Each button within the feed entry footer. (note: also used in toolbar.)|