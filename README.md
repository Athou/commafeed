# CommaFeed [![Build Status](https://travis-ci.org/Athou/commafeed.svg?branch=master)](https://travis-ci.org/Athou/commafeed)

Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on Dropwizard and AngularJS.
CommaFeed is now considered feature-complete and is in maintenance mode.

## Related open-source projects


Android apps: [News+ extension](https://github.com/Athou/commafeed-newsplus)

Browser extensions: [Chrome](https://github.com/Athou/commafeed-chrome) - [Firefox](https://github.com/Athou/commafeed-firefox) - [Opera](https://github.com/Athou/commafeed-opera) - [Safari](https://github.com/Athou/commafeed-safari)

## Deployment on your own server

### The very short version (download precompiled package)

    mkdir commafeed && cd commafeed
    wget https://github.com/Athou/commafeed/releases/download/2.4.0/commafeed.jar
    wget https://raw.githubusercontent.com/Athou/commafeed/2.4.0/config.yml.example -O config.yml
    vi config.yml
    java -Djava.net.preferIPv4Stack=true -jar commafeed.jar server config.yml 

### The short version (build from sources)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    ./mvnw clean package
    cp config.yml.example config.yml
    vi config.yml
    java -Djava.net.preferIPv4Stack=true -jar target/commafeed.jar server config.yml 

### The long version (same as the short version, but more detailed)

CommaFeed 2.0 has been rewritten to use Dropwizard and gulp instead of using tomee and wro4j. The latest version of the 1.x branch is available [here](https://github.com/Athou/commafeed/tree/1.x).

For storage, you can either use an embedded H2 database (use it only to test CommaFeed) or an external MySQL, PostgreSQL or SQLServer database.
You also need the Java 1.8+ JDK  in order to build the application.

To install the required packages to build CommaFeed on Ubuntu, issue the following commands

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
    
Copy `config.yml.example` to `config.yml` then edit the file to your liking.
Issue the following command to run the app, the server will listen by default on `http://localhost:8082`. The default user is `admin` and the default password is `admin`.

	java -Djava.net.preferIPv4Stack=true -jar target/commafeed.jar server config.yml

You can use a proxy http server such as nginx or apache.

## Translate CommaFeed into your language

Files for internationalization are located [here](https://github.com/Athou/commafeed/tree/master/src/main/app/i18n).

To add a new language, create a new file in that directory.
The name of the file should be the two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
The language has to be referenced in the `src/main/app/js/i18n.js` file to be picked up.

## Themes

To create a theme, create a new file  `src/main/app/sass/themes/_<theme>.scss`. Your styles should be wrapped in a `#theme-<theme>` element and use the [SCSS format](http://sass-lang.com/) which is a superset of CSS.

Don't forget to reference your theme in `src/main/app/sass/app.scss` and in `src/main/app/js/controllers.js` (look for `$scope.themes`).

See [_test.scss](https://github.com/Athou/commafeed/blob/master/src/main/app/sass/themes/_test.scss) for an example.


## Local development

Steps to configuring a development environment for CommaFeed may include, but may not be limited to:

1. `git clone https://github.com/Athou/CommaFeed` into some folder to get the project files.
2. Install Eclipse Luna (or latest) from http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/lunasr1 or your repo if available.
3. In Eclipse, Window → Preferences → Maven → Annotation Processing. Check "Automatically configure JDT APT"
    * You may have to install the m2e-apt connector to have "Annotation Processing" as an option. Do so from Window → Preferences → Maven → Discovery → Open Catalog → type "m2e-apt" in the search box
        * If you have installed Eclipse EE instead of Luna, you may have trouble installing m2e-apt
4. Install Lombok into Eclipse from http://projectlombok.org/download.html
    * You may have to run `java -jar lombok.jar` as an administrator if your eclipse installation is not in your home folder 
5. In Eclipse, File → Import → Maven → Existing Maven Projects. Navigate to where you cloned the CommaFeed files into, and select that as the root directory. Click Finish.
    * You may notice some errors along the lines of "Plugin execution not covered by lifecycle configuration". These are inconsequential.
6. Find the file "CommaFeedApplication.java" under the navigation pane. 
7. Right click it to bring up the context menu → Debug as... → Debug Configurations
8. Type `server config.dev.yml` under "Program arguments" in the "Arguments" tab for the Java Application setting "CommaFeedApplication"
9. Apply and hit "Debug"
10. The debugger is now working. To connect to it, open a terminal (or command prompt) and navigate to the directory where you cloned the CommaFeed files.
11. Issue the command `gulp dev` on Unix based systems or `gulp.cmd dev` in Windows.
12. The development server is now running at http://localhost:8082 and is proxying REST requests to dropwizard on port 8083.
13. Connect to the server from your browser; you should have functional breakpoints and watches on assets.
14. When you're done developing, create a fork at the top of https://github.com/Athou/CommaFeed page and commit your changes to it.
15. If you'd like to contribute to CommaFeed, create a pull request from your repository to https://github.com/Athou/CommaFeed when your changes are ready. There's a button to do so at the top of https://github.com/Athou/CommaFeed.

## Copyright and license

Copyright 2013-2016 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
