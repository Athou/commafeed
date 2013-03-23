CommaFeed 
=========
Google Reader inspired self-hosted RSS reader, based on Wicket and AngularJS.

Deploy on any JavaEE6 container or better yet on OpenShift.


Deployment on OpenShift
----------

* Create an account on `http://www.openshift.com/`.
* Add an application, select `JBoss Enterprise Application Platform 6.0``.
* For the`Public URL` set the name you want (e.g. `commafeed`).
* For the`Source Code` option, click ``Change` and set this repository (`https://github.com/Athou/commafeed.git`)`.
* Click `Create Application`.
* Click `Add cartridge` and select `MySQL`.
* Wait a couple of minutes and access your application.
* The defaut user is `admin` and the password is `admin`.

Local development
-----------------

`mvn clean package tomee:run` and access `http://localhost:8082`. Any changes to the source code will be applied immediatly.