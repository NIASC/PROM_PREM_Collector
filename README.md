# PROM_PREM_Collector
This is an application designed to collect PROM (patient reported outcome measures) and PREM (patient reported experience measures).

This application is currently being rewritten into a servlet and applet. The wiki page is likely outdated.

# Servlet prerequisits
Some tomcat libraries needs to be put in the ant library directory. These libs are `catalina-ant.jar` and `catalina-ant.jar`. This is a part of the tomcat configuration.
+ Apache Ant
+ Apache Tomcat (tested with major version 6+)
+ MariaDB (or equivalent)
+ OpenSSL
+ POSIX-compliant shell, e.g. bash (for the setup script)

## Required libraries
The required libraries should be put in $CATALINA_HOME/lib.
+ MySQL Connector/J
+ GNU JavaMail
+ GNU JAF
+ GNU inetlib
+ JSON Simple

In addition it may be required to copy/link some jar files in $CATALINA_HOMA/lib to $APACHE_ANT_HOME/lib. This is a part of the standard tomcat setup and not specific for this software. The jar files are:
+ catalina.jar
+ tomcat-util.jar

# Setup
+ start mariadb. On a system with systemd it can be done with `# systemctl start mariadb.service` (may be `mysqld.service` or `mysql.service` depending on your distro)
+ start tomcat. On a system with systemd it can be done with `# systemctl start tomcatX.service` (where `X` corresponds to your current tomcat major version).

## Guided setup
The servlet can be set up using the script `$ sh initial_setup.sh`.
The setup is divided into four parts: Database setup, Build properties, Email setup and Encryption setup.
Before running the script please ensure that:
+ You have configured tomcat and verified that it is running (e.g. visit the tomcat root page) as well as know the username and password for the tomcate script manager user.
+ You have configured mariadb properly and know the username and password for the mariadb root user. Verify that mysql can be accessed by normal users and not only by the system superuser.
+ You have superuser access. Either via `sudo` or `su`.
+ There is no existing database called prom_prem_db. Any existing database with that name will be removed by the script.
+ You have an email account for the server to use and know its configuration (you will have to enter these).
+ You have an email address to the administrator who will receive registration requests from the server, when users send a registration request through the client.

## Manual Setup
The recommended way to set up the server is using the script `$ sh initial_setup.sh`. If that did not work you may have to check it manually.
Everything required for the setup can be found in the `setup` directory.
The procedure can be extracted from the script.

# Deploy servlet
+ `$ ant clean` to remove any previos builds
+ `$ ant compile` to compile the project
+ `$ ant install` to deploy the application (i.e. put it with your tomcat webapps).
+ `$ ant manage` to create the manager application that will be used for adding users and clinics.
+ restart tomcat.

# Troubleshooting
### `$ ant install` fails with permission denied.
When using tomcat 6 the manager.url value in the `build.xml` file may have to be changed to "${app.website}/manager" instead of "${app.website}/manager/text".
### `$ ant install` fails with permission denied. Changing manager.url did help.
I Have not manage to figure out why this happens. There may be caused by a configuration error in either build.xml or tomcat itself. The servler will have to be deployed manually:
+ `$ ant dist` will create a war file and place it in `dist`.
+ Remove any current version in the tomcat webapp folder.
+ Copy the war file into the tomcat webapp folder.
+ restart tomcat.
### MariaDB can not be accessed by the servlet
This could either be cause by:
+ The mariadb root user you have configured the server can not be accessed by regular users.
+ You have configured MariaDB to be accessed through a different port than the default (3306). Either configure mariadb to be accessed through the default port of change the database url in `src/main/webapp/META-INF/context.xml` to match your mariadb configuration.
### The servlet fails to start
The web application is configured to start in the class `se.nordicehealth.servlet.ServletMain`. If you have moved `ServletMain` you have to change the servlet-class property in `src/main/webapp/WEB-INF/web.xml` to match the new location.
