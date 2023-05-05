# Changelog

## [3.1.1]

- restore the welcome page
- only apply hover effect for unread entries (same as commafeed v2)
- move notifications at the bottom of the screen
- always use https for sharing urls
- add support for redis ACLs
- transition to google analytics v4

## [3.1.0]

- add an even more compact layout
- restore hover effect from commafeed 2.x
- view mode (compact, expanded, ...) is now stored on the device so you can have a different view mode on desktop and
  mobile
- fix for the "Illegal attempt to associate a collection with two open sessions." error
- feed fetching workflow is now orchestrated with rxjava, removing a lot of code

## [3.0.1]

- allow env variable substitution in config.yml
    - e.g. having a custom config.yml file with `app.session.path=${SOME_ENV_VAR}` will substitute `SOME_ENV_VAR` with
      its value
- allow env variable prefixed with `CF_` to override config.yml properties
    - e.g. setting `CF_APP_ALLOWREGISTRATIONS=true` will set `app.allowRegistrations` to `true`

## [3.0.0]

- complete overhaul of the UI
- backend and frontend are now in separate maven modules
- no changes to the api or the database
- Docker images are now automatically built and available at https://hub.docker.com/r/athou/commafeed

## [2.6.0]

- add support for media content as a backup for missing content (useful for youtube feeds)
- correctly follow http error code 308 redirects
- fixed a bug that prevented users from deleting their account
- fixed a bug that made commafeed store entry contents multiple times
- fixed a bug that prevented the app to be used as an installed app on mobile devices if the context path of commafeed
  was not "/"
- fixed a bug that prevented entries from being "marked as read older than xxx" for a feed that was just added
- removed support for google+ and readability as those services no longer exist
- removed support for deploying on openshift
- removed alphabetical sorting of entries because of really poor performance (title cannot be indexed)
- improve performance for instances with the heavy load setting enabled by preventing CommaFeed from fetching feeds from
  users that did not log in for a long time
- various dependencies upgrades (notably dropwizard from 1.3 to 2.1)
- add support for mariadb
- add support for java17+ runtime
- various security improvements

## [2.5.0]

- unread count is now displayed in a favicon badge when supported
- the user agent string for the bot fetching feeds is now configurable
- feed parsing performance improvements
- support for java9+ runtime
- can now properly start from an empty postgresql database

## [2.4.0]

- users were not able to change password or delete account
- fix api key generation
- feed entries can now be sorted alphabetically
- fix facebook sharing
- fix layout on iOS
- postgresql driver update (fix for postgres 9.6)
- various internationalization fixes
- security fixes

## [2.3.0]

- dropwizard upgrade 0.9.1
- feed enclosures are hidden if they already displayed in the content
- fix youtube favicons
- various internationalization fixes

## [2.2.0]

- fix youtube and instagram favicon fetching
- mark as read filter was lost when a feed was rearranged with drag&drop
- feed entry categories are now displayed if available
- various performance and dependencies upgrades
- java8 is now required

## [2.1.0]

- dropwizard upgrade to 0.8.0
- you have to remove the "app.contextPath" setting from your yml file, you can optionally use
  server.applicationContextPath instead
- new setting app.maxFeedCapacity for deleting old entries
- ability to set filtering expressions for subscriptions to automatically mark new entries as read based on title,
  content, author or url.
- ability to use !keyword or -keyword to exclude a keyword from a search query
- facebook feeds now show user favicon instead of facebook favicon
- new dark theme 'nightsky'

## [2.0.3]

- internet explorer ajax cache workaround
- categories are now deletable again
- openshift support is back
- youtube feeds now show user favicon instead of youtube favicon

## [2.0.2]

- api using the api key is now working again
- context path is now configurable in config.yml (see app.contextPath in config.yml.example)
- fix login on firefox when fields are autofilled by the browser
- fix scrolling of subscriptions list on mobile
- user is now logged in after registration
- fix link to documentation on home page and about page
- fields autocomplete is disabled on the profile page
- users are able to delete their account again
- chinese and malaysian translation files are now correctly loaded
- software version in user-agent when fetching feeds is no longer hardcoded
- admin settings page is now read only, settings are configured in config.yml
- added link to metrics on the admin settings page
- Rome (rss library) upgrade to 1.5.0

## [2.0.1]

- the redis pool no longer throws an exception when it is unable to aquire a new connection

## [2.0.0]

- The backend has been completely rewritten using Dropwizard instead of TomEE, resulting in a lot less memory
  consumption and better overall performances.
  See the README on how to build CommaFeed from now on.
- CommaFeed should no longer fetch the same feed multiple times in a row
- Users can use their username or email to log in  
