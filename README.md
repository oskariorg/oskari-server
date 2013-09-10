# Oskari server libraries and example servlet implementation

## A. Overview

This repository contains an example servlet implementation and server component libraries (services) used by
[https://github.com/nls-oskari/oskari] javascript framework. The default setup uses HSQL database for
backend application data and Jetty servlet for web services. All services cannot be demonstrated using hsql
and further examples will be added in near future.

## B. Preparation - What you need to get started

* Clone `git clone https://github.com/nls-oskari/oskari-server.git` (backend) or download it as a zip as provided by github

* Clone `git clone https://github.com/nls-oskari/oskari.git` (frontend) or download it as a zip as provided by github

* The server code is packaged as Maven modules. Get Maven from here [http://maven.apache.org/] and install it.

* Both oskari-server and oskari folders need to be on the same folder level for example `/work/oskari-server/` and `/work/Oskari/`
    * This is configurable in the jetty webAppConfig resource paths

* Note! Oskari frontend folder needs to be renamed to start with a capital O (oskari -> Oskari)

* Note! both repositories have `master` and `develop` branches, master is the stable version and develop is the next version that is under development

## Quick Start

* Run all Maven commands listed in `oskari-server/external-libs/mvn-install.txt`
    * This adds maven dependencies not found in common repositories to your local maven repository

* Rename `jetty-env.sample.xml` to `jetty-env.xml` in `oskari-server/servlet-map/src/main/webapp/WEB-INF`

* Run `mvn -f servlet-map-pom.xml install` in the `oskari-server` folder
    * This compiles all the modules and starts a jetty webserver hosting the example servlet

* After compilation has completed and jetty has started - go to [http://localhost:2373/] with your browser

* You can login with username "user" and password "user" as a normal user or "admin"/"oskari" as an admin user (no real difference yet)
    * These are configured in `oskari-server/servlet-map/src/main/resources/fi/nls/oskari/user/` user.json (and role.json)
    * Login handling is implemented in `fi.nls.oskari.user.StandaloneUserService`
    * The Java class handling user related operations is configurable in `oskari-server/servlet-map/src/main/resources/fi/nls/oskari/map/servlet/oskari.properties`



[Instructions for modifying the initial demo data](ModifyingInitialDemoData.md)

[Instructions for adding your own ajax endpoints/action routes](service-control/README.md)