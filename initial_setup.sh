#!/bin/sh

database_configured=0;
build_props_configured=0;
email_configured=0;
encryption_configured=0;

setup_dir=setup;
template_dir=$setup_dir/templates;
database_dir=$setup_dir/sql;
resource_dir=src/main/java/se/nordicehealth/res;
webapp_dir=src/main/webapp

query_user() {
    printf "$1: ";
    read $2;
}

query_user_secret() {
    printf "$1: ";
    stty_orig=`stty -g`		# store tty config
    stty -echo			# disable echo
    read $2;
    stty $stty_orig		# restore tty config
    printf "\n";		# newline after password was entered
}

su_exec() {
    if [ $(command -v sudo) ]; then
	printf "Executing using sudo... \n"
	sudo su -c "sh -c \"$1\"";
    else
	printf "Executing as superuser...\n"
	su -c "sh -c \"$1\"";
    fi
}

user_exec() {
    sh -c "$1";
}

ppc_create_database() {
    db_name="prom_prem_db";
    db_template="$database_dir/prom_prem_db.sql";
    printf "creating database $db_name and importing tables from $db_template.\n";

    su_exec "mysql -u $1 -p$2 -e 'drop database if exists $db_name;create database $db_name;use $db_name;source $db_template;'";
}

ppc_database_setup() {
    printf "~MySQL setup~\n";
    query_user "MySql root username" ppc_db_root_name;
    query_user_secret "MySQL root password" ppc_db_root_passwd;
    ppc_create_database $ppc_db_root_name $ppc_db_root_passwd;
    user_exec 'mkdir -p '$webapp_dir'/META-INF; cat '$template_dir'/context.xml.template | sed -e "s,PPC_DB_ROOT_NAME,"\"'$ppc_db_root_name'\"",g" | sed -e "s,PPC_DB_ROOT_PASSWD,"\"'$ppc_db_root_passwd'\"",g" > '$webapp_dir'/META-INF/context.xml';
    database_configured=1;
}

ppc_build_props_setup() {
    printf "~Build properties setup~\n";
    query_user "Enter the full path to the tomcat directory (e.g. /usr/share/tomcat)" ppc_catalina_home;
    printf "The servlet will be accessed from APP_WEBSITE_URL/PROM_PREM_Collector\n";
    query_user "Enter the URL to the website (e.g. http://localhost:8080)" ppc_app_website_url;
    query_user "Entert Tomcat root user" ppc_tomcat_root_name;
    query_user_secret "Enter Tomcat root password" ppc_tomcat_root_passwd;
    query_user "Enter the /path/to/the/logs directory where the server logs will be placed" ppc_server_logs_dir;
    
    user_exec 'cat '$template_dir'/build.properties.template | sed -e "s,CATALINA_HOME,"'$ppc_catalina_home'",g" | sed -e "s,APP_WEBSITE_URL,"'$ppc_app_website_url'",g" | sed -e "s,MANAGER_USERNAME,"'$ppc_tomcat_root_name'",g" | sed -e "s,MANAGER_PASSWORD,"'$ppc_tomcat_root_passwd'",g" > build.properties;';
    user_exec 'mkdir -p '$ppc_server_logs_dir'; cat '$template_dir'/settings.ini.template | sed -e "s,PPC_SERVER_LOGS_DIR,"'$ppc_server_logs_dir'",g" | sed -e "s,PPC_APP_WEBSITE_URL,"'$ppc_app_website_url'",g" > '$resource_dir'/settings.ini;';
    build_props_configured=1;
}


ppc_email_setup() {
    printf "~Email setup~\n";
    printf "When a user wants to register an account the server will send an email to an administrator who will process the request. This requires a server and admin email account.\n";
    query_user "Enter the smtp host for the server email (e.g. smtp.gmail.com)" ppc_servlet_email_host;
    query_user "Enter the port for the smtp host of the server email (e.g. 587)" ppc_servlet_email_port;
    user_exec 'cat '$template_dir'/email_settings.txt.template | sed -e "s,PPC_SERVLET_EMAIL_HOST,"'$ppc_servlet_email_host'",g" | sed -e "s,PPC_SERVLET_EMAIL_PORT,"'$ppc_servlet_email_port'",g" > '$resource_dir'/email_settings.txt;';
    
    query_user "Enter the email address of the administrator" ppc_admin_email_address;
    query_user "Enter the email address of the server" ppc_server_email_address;
    query_user_secret "Enter the password of email address of the server" ppc_server_email_password;
    user_exec 'cat '$template_dir'/email_accounts.ini.template | sed -e "s,PPC_ADMIN_EMAIL_ADDRESS,"'$ppc_admin_email_address'",g" | sed -e "s,PPC_SERVER_EMAIL_ADDRESS,"'$ppc_server_email_address'",g" | sed -e "s,PPC_SERVER_EMAIL_PASSWORD,"'$ppc_server_email_password'",g" > '$resource_dir'/email_accounts.ini;';
    email_configured=1;
}

ppc_rsa_key_setup() {
    printf "~Encryption key setup~\n";
    printf "In order to encrypt sensitive information RSA is used to generate public and private keys. This operation uses OpenSSL which needs to be installed.\n";
    if [ -z $(command -v openssl) ]; then
	printf "It does not appear that OpenSSL is installed.\n";
	encryption_configured=0;
	return;
    else
	private_key_file=private_key.pem;
	key_settings_file=$resource_dir/keys.ini;
	if [ -e $private_key_file ]; then
	    user_exec "chmod 700 $private_key_file $key_settings_file; mv $private_key_file $private_key_file.old; rm $key_settings_file;"
	    printf "The file $private_key_file exists and have been renamed to $private_key_file.old\n"
	    user_exec "chmod 400 $private_key_file.old;"
	fi
	printf "Generating keys...\n";
	user_exec "openssl genpkey -algorithm RSA -out $private_key_file -pkeyopt rsa_keygen_bits:2048";
	printf "Extracting keys...\n";
	keys=`grep -v -- ----- $private_key_file | tr -d '\n' | base64 -d | openssl asn1parse -inform DER -i -strparse 22 | sed -e 's/[ +]//g' | sed -E 's/^(.+?)INTEGER://'`;
	ppc_modulus=`echo ${keys} | cut -d' ' -f3`;
	ppc_public_exponent=`echo ${keys} | cut -d' ' -f4`;
	ppc_private_exponent=`echo ${keys} | cut -d' ' -f5`;
	printf "Storing keys...\n";
	user_exec 'cat '$template_dir'/keys.ini.template | sed -e "s,PPC_MODULUS,"'$ppc_modulus'",g" | sed -e "s,PPC_PRIVATE_EXPONENT,"'$ppc_private_exponent'",g" | sed -e "s,PPC_PUBLIC_EXPONENT,"'$ppc_public_exponent'",g" > '$key_settings_file'; chmod 400 private_key.pem '$key_settings_file'';
	printf "Operation completed.\n";
	printf "The generated keys have been stored in $private_key_file. Keep it Secret, Keep it Safe.\n";
	printf "The keys can be viewed by executing the command 'openssl rsa -text -in $private_key_file'\n";
	printf "Please verify that the information in keys.ini is correct.\n";
	encryption_configured=1;
    fi
}

printf "Welcome to the PROM/PREM Collector setup guide.\n\n"
printf "This script will configure the servlet according to the information you supply.\n"

printf "The setup is divided into 4 sections:\n\t0. Database setup,\n\t1. Build properties,\n\t2. Email setup,\n\t3. Encryption setup.\n"
printf "\nBefore proceding with the setup, make sure that MariaDB and Tomcat\nis running and that you have the superuser password.\n"
printf "Also make sure that MariaDB and Tomcat have been\nconfigured and that you have the root user and password for both.\n"
printf "In addition, ensure that OpenSSL is installed. This is required for basic encryption.\n\n"

current_var=0
while [ $database_configured -eq 0 -o $build_props_configured -eq 0 -o $email_configured -eq 0 -o $encryption_configured -eq 0 ]; do
    query_user "Do you wish to proceed? Press return to continue. Type anything to exit" user_decision;
    if [ ! -z $user_decision ]; then
	break;
    fi
    case $current_var in
	0)
	    if [ $database_configured -eq 0 ]; then
		ppc_database_setup;
	    fi
	    if [ $database_configured -ne 0 ]; then
		current_var=1;
	    fi
	    ;;
	1)
	    if [ $build_props_configured -eq 0 ]; then
		ppc_build_props_setup;
	    fi
	    if [ $build_props_configured -ne 0 ]; then
		current_var=2;
	    fi
	    ;;
	2)
	    if [ $email_configured -eq 0 ]; then
		ppc_email_setup;
	    fi
	    if [ $email_configured -ne 0 ]; then
		current_var=3;
	    fi
	    ;;
	3)
	    if [ $encryption_configured -eq 0 ]; then
		ppc_rsa_key_setup;
	    fi
	    if [ $encryption_configured -ne 0 ]; then
		current_var=0;
	    fi
	    ;;
	*)
	    ;;
    esac
done


if [ $database_configured -ne 0 -a $build_props_configured -ne 0 -a $email_configured -ne 0 -a $encryption_configured -ne 0 ]; then
    printf "\nThe setup is now complete and the servlet is ready for install.\n";
    query_user "Press return to compile ant install the servlet" user_choice;
    if [ -z $user_choice ]; then
	user_exec "ant clean >/dev/null 2>&1";
	user_exec "ant compile";
	user_exec "ant remove >/dev/null 2>&1";
	user_exec "ant install";
    fi
else
    printf "\nThe setup is incomplete. You many rerun this script to complete the setup.\n";
fi
