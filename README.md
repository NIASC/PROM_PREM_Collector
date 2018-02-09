# PROM_PREM_Collector
This is an application designed to collect PROM (patient reported outcome measures) and PREM (patient reported experience measures).

This application is currently being rewritten into a servlet and applet. The wiki page is likely outdated.

# Servlet prerequisits
Some tomcat libraries needs to be put in the ant library directory. These libs are `catalina-ant.jar` and `catalina-ant.jar`. This is a part of the tomcat configuration.
+ Apache Ant
+ Apache Tomcat 8
+ MariaDB (or equivalent)
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

start mariadb with `# systemctl start mariadb.service`
start tomcat with `# systemctl start tomcat8.service`

stop tomcat with `# systemctl stop tomcat8.service`
stop mariadb with `# systemctl stop mariadb.service`

# Deploy servlet
Currently the application is designed to be accessed via the url `http://localhost:8080/PROM_PREM_Collector/`. This is hard-coded in some places in the code and you may need to change this to make it work. In the future this will be fixed.
+ Rename `build.properties.sample` to `build.properties` and edit the file to match your MySQL and tomcat installation.<br>
+ Rename `web/META-INF/context.xml.sample` to `web/META-INF/context.xml` and edit the file to match your MySQL configuration. You should only need to modify `username`, `password` and `url`.
+ run `ant compile` to compile the project
+ run `ant jar` to create the jar file that will be launched on the website. Optionally remove the `servlet` directory from the jar file you created as it is not needed by the users.
+ run `ant install` to deploy the application (i.e. put it with your tomcat webapps).
