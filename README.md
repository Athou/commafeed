CommaFeed 
=========
Google Reader inspired self-hosted RSS reader, based on Wicket and AngularJS.

Deploy on any JavaEE6 container or better yet on OpenShift.


Deployment on OpenShift
----------------------------------------------

Hosting an application on OpenShift is free.

* Create an account on [OpenShift](http://www.openshift.com/).
* Add an application, select `JBoss Enterprise Application Platform 6.0`.
* For the`Public URL` set the name you want (e.g. `commafeed`).
* For the`Source Code` option, click `Change` and set this repository (`https://github.com/Athou/commafeed.git`).
* Click `Create Application`.
* Click `Add cartridge` and select `MySQL`.
* Wait a couple of minutes and access your application.
* The defaut user is `admin` and the password is `admin`.

Local development
-----------------

Checkout the code and use maven to build and start a local TomEE instance.

 `mvn clean package tomee:run`

The application is online at [http://localhost:8082/commafeed](http://localhost:8082/commafeed). Any change to the source code will be applied immediatly.