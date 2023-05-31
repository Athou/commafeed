# CommaFeed

Google Reader inspired self-hosted RSS reader, based on Dropwizard and React/TypeScript.

![preview](https://user-images.githubusercontent.com/1256795/184886828-1973f148-58a9-4c6d-9587-ee5e5d3cc2cb.png)

## Features

- 4 different layouts
- Dark theme
- Fully responsive
- Keyboard shortcuts for almost everything
- Support for right-to-left feeds
- Translated in 25+ languages
- Supports thousands of users and millions of feeds
- OPML import/export
- REST API
- [Browser extension](https://github.com/Athou/commafeed-browser-extension)

## Deployment on your own server

### Docker

Docker images are built automatically and are available at https://hub.docker.com/r/athou/commafeed

### Download precompiled package

    mkdir commafeed && cd commafeed
    wget https://github.com/Athou/commafeed/releases/latest/download/commafeed.jar
    wget https://github.com/Athou/commafeed/releases/latest/download/config.yml.example -O config.yml
    java -Djava.net.preferIPv4Stack=true -jar commafeed.jar server config.yml

The server will listen on http://localhost:8082. The default
user is `admin` and the default password is `admin`.

### Build from sources

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    ./mvnw clean package
    cp commafeed-server/config.yml.example config.yml
    java -Djava.net.preferIPv4Stack=true -jar commafeed-server/target/commafeed.jar server config.yml

The server will listen on http://localhost:8082. The default
user is `admin` and the default password is `admin`.

## Translation

Files for internationalization are
located [here](https://github.com/Athou/commafeed/tree/master/commafeed-client/src/locales).

To add a new language:

- add the new locale to the `locales` array in:
    - `commafeed-client/.linguirc`
    - `commafeed-client/src/i18n.ts`
- run `npm run i18n:extract`
- add translations to the newly created `commafeed-client/src/locales/[locale]/messages.po` file

The name of the locale should be the
two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).

## Local development

### Backend

- Open `commafeed-server` in your preferred Java IDE.
    - CommaFeed uses Lombok, you need the Lombok plugin for your IDE.
- Start `CommaFeedApplication.java` in debug mode with `server config.dev.yml` as arguments

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`

The frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on
port 8083

## Copyright and license

Copyright 2013-2023 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
