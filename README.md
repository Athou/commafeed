CommaFeed [![Build Status](https://buildhive.cloudbees.com/job/Athou/job/commafeed/badge/icon)](https://buildhive.cloudbees.com/job/Athou/job/commafeed/)
=========
Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on JAX-RS, Wicket and AngularJS.

Deploy on any JavaEE6 container or better yet on OpenShift.

Deployment on OpenShift
-----------------------

Hosting an application on OpenShift is free.

* Create an account on [OpenShift](http://www.openshift.com/).
* Add an application, select `JBoss Enterprise Application Platform 6.0`.
* For the`Public URL` set the name you want (e.g. `commafeed`).
* For the`Source Code` option, click `Change` and set this repository (`https://github.com/Athou/commafeed.git`).
* Click `Create Application`.
* Click `Add cartridge` and select `MySQL`.
* Wait a couple of minutes and access your application.
* The defaut user is `admin` and the password is `admin`.

Deployment on your own server
-----------------------------

For storage, you can either use an embedded HSQLDB database or an external MySQL database.
Support for other databases is coming soon.
You also need maven (and a Java JDK) installed in order to build the application.

To install maven and openjdk on Ubuntu, issue the following commands

    sudo add-apt-repository ppa:natecarlson/maven3
    sudo apt-get update
    sudo apt-get install openjdk-7-jdk maven3
    sudo ln -s /usr/bin/mvn3 /usr/bin/mvn
    
Now build the application

	Embedded HSQL database (not recommended, ok for quick tests):
    mvn clean package tomee:build -Pprod
    
	External MySQL database:
    mvn clean package tomee:build -Pprod -Pmysql
    
It will generate a zip file at `target/commafeed.zip` with everything you need to run the application.

* Create a directory somewhere (e.g. `/opt/commafeed/`) and extract the generated zip inside this directory.
* Create a directory called `logs` (e.g. `/opt/commafeed/logs`)
* If you used the MySQL option, create a database in your MySQL instance, then uncomment the `Resource` element from `conf/tomee.xml` and edit the default credentials.
* If you'd like to change the default port (8082), edit `conf/server.xml` and look for `<Connector port="8082" protocol="HTTP/1.1"`. Change the port to the value you'd like to use.
* CommaFeed will run on the `/commafeed` context. If you'd like to change the context, go to `webapps` and rename `commafeed.war`. Use the special name `ROOT.war` to deploy to the root context.
* To start and stop the application, use `bin/startup.sh` and `bin/shutdown.sh` on Linux (you may need to `chmod +x bin/*.sh`) or `bin\startup.bat` and `bin\shutdown.bat` on Windows.
* To update the application with a newer version, pull the latest changes and use the same command you used to build the complete TomEE package, but without the `tomee:build` part. 
This will generate the file `target/commafeed.war`. Copy this file to your tomee `webapps/` directory.

Local development
-----------------

Checkout the code and use maven to build and start a local TomEE instance.

 `mvn clean package tomee:run`

The application is online at [http://localhost:8082/commafeed](http://localhost:8082/commafeed). Any change to the source code will be applied immediatly.

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