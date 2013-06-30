CommaFeed [![Build Status](https://buildhive.cloudbees.com/job/Athou/job/commafeed/badge/icon)](https://buildhive.cloudbees.com/job/Athou/job/commafeed/)
=========
Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on JAX-RS, Wicket and AngularJS.

Deploy on your own server (using TomEE, a lightweight JavaEE6 container based on Tomcat) or even in the cloud for free on OpenShift.

[Android app](https://github.com/doomrobo/CommaFeed-Android-Reader)

[Chrome extension](https://github.com/Athou/commafeed-chrome)

[Firefox extension](https://github.com/Athou/commafeed-firefox)

[Opera extension](https://github.com/Athou/commafeed-opera)

[Safari extension](https://github.com/Athou/commafeed-safari)

Deployment on OpenShift
-----------------------

Hosting an application on OpenShift is free. 
At the moment those instructions are not working because the application takes too long to build on OpenShift and causes a timeout. 
See [here](http://jasonwryan.com/blog/2013/05/25/greader/) for an alternative method.

* Create an account on [OpenShift](http://www.openshift.com/).
* Add an application, select `JBoss Enterprise Application Platform 6.0`.
* For the `Public URL` set the name you want (e.g. `commafeed`).
* For the `Source Code` option, click `Change` and set this repository (`https://github.com/Athou/commafeed.git`).
* Click `Create Application`.
* Click `Add cartridge` and select `MySQL`.
* Wait a couple of minutes and access your application.
* The default user is `admin` and the password is `admin`.

Deployment on your own server
-----------------------------

For storage, you can either use an embedded HSQLDB database or an external MySQL or PostgreSQL database.
You also need Maven 3.x (and a Java JDK) installed in order to build the application.

To install maven and openjdk on Ubuntu, issue the following commands

    sudo add-apt-repository ppa:natecarlson/maven3
    sudo apt-get update
    sudo apt-get install openjdk-7-jdk maven3
    
    Not required but if you don't, use 'mvn3' instead of 'mvn' for the rest of the instructions.
    sudo ln -s /usr/bin/mvn3 /usr/bin/mvn
    
On Windows and other operating systems, just download maven 3.x from the [official site](http://maven.apache.org/), extract it somewhere and add the `bin` directory to your `PATH` environment variable.
    
Download the sources (it doesn't matter where, you can delete the directory when you're done).
If you don't have git you can download the sources as a zip file from [here](https://github.com/Athou/commafeed/archive/master.zip)

    git clone https://github.com/Athou/commafeed.git
    cd commafeed
    
Now build the application

	Embedded HSQL database
    mvn clean package tomee:build -Pprod
    
	External MySQL database:
    mvn clean package tomee:build -Pprod -Pmysql
    
    External PostgreSQL database:
    mvn clean package tomee:build -Pprod -Ppgsql
    
It will generate a zip file at `target/commafeed.zip` with everything you need to run the application.

* Create a directory somewhere (e.g. `/opt/commafeed/`) and extract the generated zip inside this directory.
* Create a directory called `logs` (e.g. `/opt/commafeed/logs`)
* On Linux, create the file `bin/setenv.sh` and put the following in it : `export JAVA_OPTS="-Djava.net.preferIPv4Stack=true -Xmx1024m -XX:MaxPermSize=256m -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC"`
* On Windows, create the file `bin/setenv.bat` and put the following in it : `set JAVA_OPTS=-Djava.net.preferIPv4Stack=true -Xmx1024m -XX:MaxPermSize=256m -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC`
* If you don't use the embedded database, create a database in your external database instance, then uncomment the `Resource` element corresponding to the database engine you use from `conf/tomee.xml` and edit the default credentials.
* If you'd like to change the default port (8082), edit `conf/server.xml` and look for `<Connector port="8082" protocol="HTTP/1.1"`. Change the port to the value you'd like to use.
* CommaFeed will run on the `/commafeed` context. If you'd like to change the context, go to `webapps` and rename `commafeed.war`. Use the special name `ROOT.war` to deploy to the root context.
* To start and stop the application, use `bin/startup.sh` and `bin/shutdown.sh` on Linux (you may need to `chmod +x bin/*.sh`) or `bin\startup.bat` and `bin\shutdown.bat` on Windows.
* To update the application with a newer version, pull the latest changes and use the same command you used to build the complete TomEE package, but without the `tomee:build` part (keep `-Pprod -P<database>`). 
This will generate the file `target/commafeed.war`. Copy this file to your tomee `webapps/` directory.
* The application is online at [http://localhost:8082/commafeed](http://localhost:8082/commafeed). Don't forget to set the public URL in the admin settings.
* The default user is `admin` and the password is `admin`.

Local development
-----------------

Checkout the code and use maven to build and start a local TomEE instance.

 `mvn clean package tomee:run`

The application is online at [http://localhost:8082/commafeed](http://localhost:8082/commafeed). Any change to the source code will be applied immediatly.
The default user is `admin` and the password is `admin`.

Translate CommaFeed into your language
--------------------------------------

Files for internationalization are located [here](https://github.com/Athou/commafeed/tree/master/src/main/resources/i18n).

To add a new language, create a new file in that directory.
The name of the file should be the two-letters [ISO-639-1 language code](http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
The language has to be referenced in the `languages.properties` file to be picked up.

When adding new translations, add them in en.properties then run `mvn -e groovy:execute -Pi18n`. It will parse the english file and add placeholders in the other translation files. 

Themes
---------------------

To create a theme, create a new file  `src/main/webapp/sass/themes/_<theme>.scss`. Your styles should be wrapped in a `#theme-<theme>` element and use the [SCSS format](http://sass-lang.com/) which is a superset of CSS.

Don't forget to reference your theme in `src/main/webapp/sass/app.scss` and in `src/main/webapp/js/controllers.js` (look for `$scope.themes`).

See [_test.scss](https://github.com/Athou/commafeed/blob/master/src/main/webapp/sass/themes/_test.scss) for an example.


Copyright and license
---------------------

Copyright 2013 CommaFeed.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
