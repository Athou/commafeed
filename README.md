CommaFeed [![Build Status](https://travis-ci.org/Athou/commafeed.svg?branch=master)](https://travis-ci.org/Athou/commafeed)
=========
Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on Dropwizard and AngularJS.

Related open-source projects
----------------------------

Android apps: [News+ extension](https://github.com/Athou/commafeed-newsplus) - [Android app](https://github.com/doomrobo/CommaFeed-Android-Reader)

Browser extensions: [Chrome](https://github.com/Athou/commafeed-chrome) - [Firefox](https://github.com/Athou/commafeed-firefox) - [Opera](https://github.com/Athou/commafeed-opera) - [Safari](https://github.com/Athou/commafeed-safari)

Deployment on your own server
-----------------------------

CommaFeed 2.0 has been rewritten to use Dropwizard and gulp instead of using tomee and wro4j. The latest version of the 1.x branch is available [here](https://github.com/Athou/commafeed/tree/1.x).

For storage, you can either use an embedded H2 database or an external MySQL, PostgreSQL or SQLServer database.
You also need Maven 3.x (and a Java 1.7+ JDK) installed in order to build the application.

To install maven and openjdk on Ubuntu, issue the following commands

    sudo apt-get install build-essential openjdk-7-jdk maven
    # Make sure java7 is the selected java version
    sudo update-alternatives --config java
    sudo update-alternatives --config javac
    
    
On Windows and other operating systems, just download maven 3.x from the [official site](http://maven.apache.org/), extract it somewhere and add the `bin` directory to your `PATH` environment variable.
    
Clone this repository. If you don't have git you can download the sources as a zip file from [here](https://github.com/Athou/commafeed/archive/master.zip)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    
Now build the application

    mvn clean package
    
Copy `config.yml.example` to `config.yml` then edit the file to your liking.
Issue the following command to run the app, the server will listen by default on `http://localhost:8082`. The default user is `admin` and the default password is `admin`.

	java -jar target/commafeed.jar server config.yml

You can use a proxy http server such as nginx or apache.

Deployment on OpenShift
-----------------------------

 [OpenShift](https://openshift.redhat.com) is Red Hat's Platform-as-a-Service (PaaS) that allows developers to quickly develop, host, and scale applications in a cloud environment. CommaFeed runs perfectly on OpenShift and can even be used in the free tier. Follow the [Getting Started](https://developers.openshift.com/en/getting-started-overview.html) guide and after you sign up and install the Command Line Tools (RHC), do:

	rhc create-app commafeed diy-0.1 mysql-5.5
	cd commafeed
	git remote add upstream -m master https://github.com/Athou/commafeed.git
	git pull -s recursive -X theirs upstream master
	git push

Local development
-----------------

Steps to configuring a development environment for CommaFeed may include, but may not be limited to:
01. git clone https://github.com/Athou/CommaFeed into some folder to get the project files.
02. Install Eclipse Luna (or latest) from http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/lunasr1 or your repo if available
03. In Eclipse, Window -> Preferences -> Maven -> Annotation Processing
    Check "Automatically configure JDT APT"
    03a. You may have to install the m2e-apt connector to have "Annotation Processing" as an option. Do so from Window -> Preferences -> Maven -> Discovery -> Open Catalog -> type "m2e-apt" in the search box
        03ai. If you have installed Eclipse EE instead of Luna, you may have trouble installing m2e-apt
04. Install Lombok into Eclipse from http://projectlombok.org/download.html
    04a. You may have to run `java -jar lombok.jar` as an administrator if your eclipse installation is not in your home folder 
05. In Eclipse, File -> Import -> Maven -> Existing Maven Projects
    navigate to where you cloned the CommaFeed files into, and select that as the root directory. Click Finish.
    05a. You may notice some errors along the lines of "Plugin execution not covered by lifecycle configuration". These are inconsequential.
06. Find the file "CommaFeedApplication.java" under the navigation pane
    06a. right click it to bring up the context menu -> Debug as... -> Debug Configurations
07. Type `server config.dev.yml` under "Program arguments" in the "Arguments" tab for the Java Application setting "CommaFeedApplication"
08. Apply and hit "Debug"
09. The debugger is now working. To connect to it, open a terminal (or command prompt) and navigate to the directory where you cloned the CommaFeed files.
10. Issue the command `gulp dev` on Unix based systems or `gulp.cmd dev` in Windows.
11. The development server is now running at http://localhost:8082 and is proxying REST requests to dropwizard on port 8083.
12. Connect to the server from your browser; you should have functional breakpoints and watches on assets.
13. When you're done developing, create a fork at the top of https://github.com/Athou/CommaFeed page and commit your changes to it.
14. If you'd like to contribute to CommaFeed, create a pull request from your repository to https://github.com/Athou/CommaFeed when your changes are ready.

Translate CommaFeed into your language
--------------------------------------

Files for internationalization are located [here](https://github.com/Athou/commafeed/tree/master/src/main/app/i18n).

To add a new language, create a new file in that directory.
The name of the file should be the two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
The language has to be referenced in the `src/main/app/js/i18n.js` file to be picked up.

Themes
---------------------

To create a theme, create a new file  `src/main/webapp/sass/themes/_<theme>.scss`. Your styles should be wrapped in a `#theme-<theme>` element and use the [SCSS format](http://sass-lang.com/) which is a superset of CSS.

Don't forget to reference your theme in `src/main/webapp/sass/app.scss` and in `src/main/webapp/js/controllers.js` (look for `$scope.themes`).

See [_test.scss](https://github.com/Athou/commafeed/blob/master/src/main/webapp/sass/themes/_test.scss) for an example.


Copyright and license
---------------------

Copyright 2013-2014 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
