# PROM_PREM_Collector
This is an application designed to collect PROM (patient reported outcome measures) and PREM (patient reported experience measures).

## Getting Started
It is recommended to use `git` for downloading the source code, but it is possible to download the source as a zip.
The prerequisits are labled as
* __[Core]__: Required to run/build the software.
* __[Impl]__: Required for the default implementation.
* __[incl]__: Dependency is included in the software.
### Prerequisits
* __[Core]__ [Java Development Kit](http://openjdk.java.net/) version 7 or later. For the JVM and compiler.
* __[Core]__ [Apache Ant](https://ant.apache.org/). For building the program.
* __[Impl]__ [XAMPP](https://www.apachefriends.org/index.html) version 7.1.4 (or similar). For Starting MySQL.
* __[Impl, incl]__ [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (version 5.1.42 included). For MySQL interface to Java.
* __[Impl, incl]__ [GNU JavaMail](https://www.gnu.org/software/classpathx/javamail/javamail.html) (version 1.1.2 included). For email to work.
* __[Impl, incl]__ [GNU JAF](https://www.gnu.org/software/classpathx/jaf/jaf.html) (version 1.1.1 included). For email to work.
* __[Impl, incl]__ [GNU inetlib](https://www.gnu.org/software/classpathx/inetlib/inetlib.html) (version 1.1.1 included). For email to work.

### Downloading the files (XAMPP)
Download the project (as zip or using `git clone`) to you _htdocs_ directory (On a GNU+Linux machine with XAMPP this should be located in _/opt/lampp/_).

### Configuration
These configuration steps are needed for the current implementation. You are free to replace this implementation with your own as long as you implement the interfaces located in _src/implement/_.
#### If using MySQL
* Start MySQL (by starting XAMPP) and create a new database.
* Import the data from _sql/prom_prem_db.sql_ to your new database.
* In _src/implement/settings.ini_: change `url` to the url of your database; change `db_login` to your database login name; change `db_password` to your database password.

#### If using email-based registration
* In _src/implement/mail_settings.txt modify the settings to match your mail server.
* In _src/implement/mailaccount_settings.ini: change `admin_email` to the email address that will administer registration requests; change `server_email` and `server_password` to match the credentials of the server's email account that will send registration requests to the administrators.

### Build and run
Navigate to the project folder (it should contain a file called _build.xml_). The build file supports the following operations:
* `ant init`: Creates the _build/_ directory which has the same structure as _src/_ and copies settings files from _src_ to their corresponding location in _build/_.
* `ant build` Compiles the source from _src/_ and places it in _build/_ and links the external libraries located in _libs/_.
* `ant clean` Deletes the _build/_ directory and its subdirectories; "cleans up the build process."
* `ant jar` Builds the project and puts it in a _java archive_ (jar file).
* `ant Manage` Starts the part of the program that allows you to add clinics and users to the database. For administration purpose only.
* `ant Main` Starts the questionaire application (Main program).

## Writing your own implementation
__Note: This software is still in early development so it is discouraged to implement your own implementation at this time.__
Your implementation must implement all of the interfaces located in _src/implement/_ in order to work.
### Known implementation-specific issues
* Error and information messages are called by their name or code as they are defined in the MySQL database.
