# CommaFeed

Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on Dropwizard and AngularJS.
CommaFeed is now considered feature-complete and is in maintenance mode.

## Related open-source projects

Browser extensions:

- [Chrome](https://github.com/Athou/commafeed-chrome)
- [Firefox](https://github.com/Athou/commafeed-firefox)
- [Opera](https://github.com/Athou/commafeed-opera)
- [Safari](https://github.com/Athou/commafeed-safari)

## Deployment on your own server

### The very short version (download precompiled package)

    mkdir commafeed && cd commafeed
    wget https://github.com/Athou/commafeed/releases/download/3.0.0/commafeed.jar
    wget https://raw.githubusercontent.com/Athou/commafeed/3.0.0/commafeed-server/config.yml.example -O config.yml
    vi config.yml
    java -Djava.net.preferIPv4Stack=true -jar commafeed.jar server config.yml

### The short version (build from sources)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    ./mvnw clean package
    cp commafeed-server/config.yml.example config.yml
    vi config.yml
    java -Djava.net.preferIPv4Stack=true -jar commafeed-server/target/commafeed.jar server config.yml

### The long version (same as the short version, but more detailed)

CommaFeed 2.0 has been rewritten to use Dropwizard and gulp instead of using tomee and wro4j. The latest version of the 1.x branch is available [here](https://github.com/Athou/commafeed/tree/1.x).

For storage, you can either use an embedded file-based H2 database or an external MySQL, PostgreSQL or SQLServer database.
You also need the Java 1.8+ JDK in order to build the application.

To install the required packages to build CommaFeed on Ubuntu, issue the following commands

    # if this commands works and returns a version >= 1.8.0 you're good to go and you can skip JDK installation
    javac -version

    # if openjdk-8-jdk is not available on your ubuntu version (14.04 LTS), add the following repo first
    sudo add-apt-repository ppa:openjdk-r/ppa
    sudo apt-get update

    sudo apt-get install g++ build-essential openjdk-8-jdk

    # Make sure java8 is the selected java version
    sudo update-alternatives --config java
    sudo update-alternatives --config javac

Clone this repository. If you don't have git you can download the sources as a zip file from [here](https://github.com/Athou/commafeed/archive/master.zip)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed

Now build the application

    ./mvnw clean package

Copy `commafeed-server/config.yml.example` to `./config.yml` then edit the file to your liking.
Issue the following command to run the app, the server will listen by default on `http://localhost:8082`. The default user is `admin` and the default password is `admin`.

    java -Djava.net.preferIPv4Stack=true -jar commafeed-server/target/commafeed.jar server config.yml

You can use a proxy http server such as nginx or apache.

## Translate CommaFeed into your language

Files for internationalization are located [here](https://github.com/Athou/commafeed/tree/master/commafeed-client/src/locales).

To add a new language:

- edit `commafeed-client/src/i18n.ts` and
  - add the new locale to the `locales` array.
  - import the dayjs locale
- edit `commafeed-client/.linguirc` and add the new locale to the `locales` array.

The name of the locale should be the two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).

## Local development

- `git clone https://github.com/Athou/CommaFeed`

### Backend

- Open `commafeed-server` in your preferred Java IDE.
  - CommaFeed uses Lombok, you need the Lombok plugin for your IDE.
  - If using Eclipse, Go to Window → Preferences → Maven → Annotation Processing and check "Automatically configure JDT APT"
- Start `CommaFeedApplication.java` in debug mode with `server config.dev.yml` as arguments

### Frontend

- Open `commafeed-client` in your preferred JavaScript IDE.
- run `npm install`
- run `npm run dev`
- the frontend server is now running at http://localhost:8082 and is proxying REST requests to the backend running on port 8083

## Copyright and license

Copyright 2013-2022 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
