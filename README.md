CommaFeed [![Build Status](https://buildhive.cloudbees.com/job/Athou/job/commafeed/badge/icon)](https://buildhive.cloudbees.com/job/Athou/job/commafeed/)
=========
Sources for [CommaFeed.com](http://www.commafeed.com/).

Google Reader inspired self-hosted RSS reader, based on JAX-RS, Wicket and AngularJS.

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