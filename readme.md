**Table of Contents**  

- [DCHQ - Docker Java Example ](#DCHQ - Docker Java Example)
- [A Step by Step Guide for Deploying & Managing a Java Application on Docker-based WebSphere, JBoss, Tomcat and Jetty Application Servers](#DCHQ - Docker Java Example)
	- [Background](#Background)
	- [Configuring the web.xml and webapp-config.xml files in the Java application](#Configuring the web.xml and webapp-config.xml files in the Java application)
	- [Using the liquibase bean to initialize the connected database](#)
	- [Building the YAML-based application templates that can re-used on any Linux host running anywhere](#)
		- [3-Tier Java (Nginx – Tomcat – MySQL)](#)
		- [3-Tier Java (Nginx – Jetty – MySQL)](#)
		- [3-Tier Java (Nginx – JBoss – MySQL)](#)
		- [2-Tier Java (WebSphere – MySQL)](#)
		- [3-Tier Java (Nginx – Tomcat – PostgreSQL)](#)
		- [3-Tier Java (Nginx – Jetty – PostgreSQL)](#)
		- [3-Tier Java (Nginx – JBoss – PostgreSQL)](#)
		- [2-Tier Java (WebSphere – PostgreSQL)](#)
		- [3-Tier Java (Nginx – Tomcat – Oracle-XE)](#)
		- [3-Tier Java (Nginx – Jetty – Oracle-XE)](#)
		- [3-Tier Java (Nginx – JBoss – Oracle-XE)](#)
		- [2-Tier Java (WebSphere – Oracle-XE)](#)
		- [Invoking a plug-in to initialize the database separately on a 3-Tier Java (Nginx – Tomcat – MySQL)](#)
		- [Using your script or deployment plan](#)
	- [Provisioning & Auto-Scaling the Underlying Infrastructure on Any Cloud](#)
	- [Deploying the Multi-Tier Java Application on the Rackspace Cluster](#)
	- [Accessing The In-Browser Terminal For The Running Containers](#)
	- [Monitoring the CPU, Memory & I/O Utilization of the Running Containers](#)
	- [Enabling the Continuous Delivery Workflow with Jenkins to Update the WAR File of the Running Application when a Build is Triggered](#)
	- [Scaling out the Tomcat Application Server Cluster](#)
	- [Conclusion](#)
 

DCHQ - Docker Java Example 
===========================

 

A Step by Step Guide for Deploying & Managing a Java Application on Docker-based WebSphere, JBoss, Tomcat and Jetty Application Servers
=======================================================================================================================================
 

Background
----------

Containerizing enterprise Java applications is still a challenge mostly because existing application composition frameworks do not address complex dependencies, external integrations or auto-scaling workflows post-provision. Moreover, the ephemeral design of containers meant that developers had to spin up new containers and re-create the complex dependencies & external integrations with every version update.

DCHQ, available in hosted and on-premise versions, addresses all of these challenges and simplifies the containerization of enterprise Java applications through an advance application composition framework that extends Docker Compose with cross-image environment variable bindings, extensible BASH script plug-ins that can be invoked at request time or post-provision, and application clustering for high availability across multiple hosts or regions with support for auto scaling.

Once an application is provisioned, a user can monitor the CPU, Memory, & I/O of the running containers, get notifications & alerts, and get access to application backups, automatic scale in/out workflows, and plug-in execution workflows to update running containers. Moreover, out-of-box workflows that facilitate Continuous Delivery with Jenkins allow developers to refresh the Java WAR file of a running application without disrupting the existing dependencies & integrations.

In previous blogs, we demonstrated the end-to-end deployment automation of various Java applications (like Pizza Shop and Movie Store apps) on multi-tier Docker-based application stacks across 13 different clouds & virtualization platforms. For full list of these blogs, you can visit this page: <http://dchq.co/docker-java-applications.html>

However many users were still confused on some of the fundamental aspects of application modeling. These questions include:

-   Where do these **environment variables** come from in your YAML-based application template?

-   How is the **database initialized with the proper schemas** needed from my Java application?

-   I already have a deployment plan for my WebLogic Application Server. Can I **run my own script** for deploying a Java application?

To address these questions, we created a sample “Names Directory” Java application in this GitHub project that can be deployed on these application stacks:

-   Nginx (for load balancing)

-   WebSphere, JBoss, Tomcat and Jetty (as the application server)

-   MySQL, PostgreSQL, and Oracle (for the database)

In this project, we will provide a step-by-step guide for configuring, deploying and managing this Java application using different application stacks and on different cloud/virtual infrastructure.

We will cover:

-   Configuring the web.xml and webapp-config.xml files in the Java application

-   Using the liquibase bean to initialize the connected database

-   Building the YAML-based application templates that can re-used on any Linux host running anywhere

-   Provisioning & auto-scaling the underlying infrastructure on any cloud (with Rackspace being the example in this blog)

-   Deploying the multi-tier Java application on the Rackspace cluster

-   Monitoring the CPU, Memory & I/O of the Running Containers

-   Enabling the Continuous Delivery Workflow with Jenkins to update the WAR file of the running applications when a build is triggered

-   Scaling out the Application Server Cluster when the application is resource-constrained

 

Configuring the web.xml and webapp-config.xml files in the Java application
---------------------------------------------------------------------------

You can clone this sample “Names Directory” Java application from GitHub.

**git clone** <https://github.com/dchqinc/dchq-docker-java-example.git>

This is the most important step in “Dockerizing” your Java application. In order to leverage the environment variables you can pass when running containers, you will need to make sure that your application is configured in a way that will allow you to change certain properties at request time – like:

-   The database driver you would like to use

-   The database URL

-   The database credentials

-   Any other parameters that you would like to change at request time (e.g. the min/max connection pool size, idle timeout, etc.)

To achieve this, you will first need to configure **web.xml** to use the bootstrap Servlet to start up the Spring context.

<https://github.com/dchqinc/dchq-docker-java-example/blob/master/src/main/webapp/WEB-INF/web.xml>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <servlet>
        <servlet-name>DispatcherServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/spring/webapp-config.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet> 
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You will notice that the **contextConfigLocation** is referencing **/WEB-INF/spring/webapp-config.xml**

Next, we will need to configure parameters in the **webapp-config.xml** file to reference host environment variables that will be passed at request time.

<https://github.com/dchqinc/dchq-docker-java-example/blob/master/src/main/webapp/WEB-INF/spring/webapp-config.xml>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <bean id="dataSource" class="snaq.db.DBPoolDataSource" destroy-method="release">
        <property name="driverClassName" value="${database_driverClassName}"/>
        <property name="url" value="${database_url}"/>
        <property name="user" value="${database_username}"/>
        <property name="password" value="${database_password}"/>
        <property name="minPool" value="1"/>
        <property name="maxPool" value="10"/>
        <property name="maxSize" value="10"/>
        <property name="idleTimeout" value="60"/>
    </bean>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You will notice that specific dataSource properties are referencing the following environment variables that will be passed on at request time:

-   **database_driverClassName**

-   **database_url**

-   **database_username**

-   **database_password**

 

Using the liquibase bean to initialize the connected database
-------------------------------------------------------------

We typically recommend initializing the database schema as part of the Java application deployment itself. This way, you don’t have to worry about maintaining separate SQL files that need to be executed on the database separately.

However if you already have those SQL files and you still prefer executing them on the database separately – then DCHQ can help you automate this process through its plug-in framework. You can refer to this section for more information.

In order to include the SQL scripts for creating the database tables in the Java application, you will need to configure **webapp-config.xml** file to use **liquibase** bean that checks the dataSource and runs new statements from upgrade.sql. Liquibase tracks which changelog statements have run against each database.

<https://github.com/dchqinc/dchq-docker-java-example/blob/master/src/main/webapp/WEB-INF/spring/webapp-config.xml>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <bean id="liquibase" class="liquibase.integration.spring.SpringLiquibase">
        <property name="dataSource" ref="dataSource" />
        <property name="changeLog" value="/WEB-INF/upgrade/upgrade.sql" />
    </bean>
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Here’s the actual upgrade.sql file with the SQL statements for initializing the schema on the connected MySQL, PostgreSQL or Oracle database.

<https://github.com/dchqinc/dchq-docker-java-example/blob/master/src/main/webapp/WEB-INF/upgrade/upgrade.sql>

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
--liquibase formatted sql

--changeset admin:1 dbms:mysql
CREATE TABLE IF NOT EXISTS `NameDirectory` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `firstName` VARCHAR(50) NOT NULL,
    `lastName` VARCHAR(50) NOT NULL,
    `createdTimestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
)
ENGINE=InnoDB;

--changeset admin:1 dbms:postgresql
CREATE TABLE "NameDirectory" (
    id SERIAL NOT NULL,
    "firstName" VARCHAR(50) NOT NULL,
    "lastName" VARCHAR(50) NOT NULL,
    "createdTimestamp" TIMESTAMP(0) WITHOUT TIME ZONE DEFAULT timestamp 'now ()' NOT NULL,
    PRIMARY KEY(id)
)
    WITH (oids = false);

--changeset admin:1 dbms:oracle
CREATE TABLE NameDirectory (
    id NUMBER(10) NOT NULL,
    firstName VARCHAR2(50) NOT NULL,
    lastName VARCHAR2(50) NOT NULL,
    createdTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT id_pk PRIMARY KEY (id)
);
CREATE SEQUENCE NameDirectory_seq;
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Building the YAML-based application templates that can re-used on any Linux host running anywhere
-------------------------------------------------------------------------------------------------

Once logged in to DCHQ (either the hosted DCHQ.io or on-premise version), a user can navigate to **Manage** > **Templates** and then click on the **+** button to create a new **Docker Compose** template.

We have created 12 application templates **using the official images from Docker Hub** for the same “Names Directory” Java application – but for different application servers and databases.

-   3-Tier Java (Nginx – Tomcat – MySQL)

-   3-Tier Java (Nginx – Jetty – MySQL)

-   3-Tier Java (Nginx – JBoss – MySQL)

-   2-Tier Java (WebSphere – MySQL)

-   3-Tier Java (Nginx – Tomcat – PostgreSQL)

-   3-Tier Java (Nginx – Jetty – PostgreSQL)

-   3-Tier Java (Nginx – JBoss – PostgreSQL)

-   2-Tier Java (WebSphere – PostgreSQL)

-   3-Tier Java (Nginx – Tomcat – Oracle-XE)

-   3-Tier Java (Nginx – Jetty – Oracle-XE)

-   3-Tier Java (Nginx – JBoss – Oracle-XE)

-   2-Tier Java (WebSphere – Oracle-XE)

Across all these application templates, you will notice that Nginx is invoking a BASH script plug-in to add the container IP’s of the application servers in the default.conf file dynamically (or at request time).

The application servers (Tomcat, Jetty, JBoss and WebSphere) are also invoking a BASH script plug-in to deploy the Java WAR file from the accessible GitHub URL.

<https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war>

Tomcat, JBoss and Jetty are invoking the exact same BASH script plug-in – except the WAR file is getting deployed on different directories:

-   Tomcat – dir=/usr/local/tomcat/webapps/ROOT.war

-   Jetty – dir=/var/lib/jetty/webapps/ROOT.war

-   JBoss – dir=/opt/jboss/wildfly/standalone/deployments/ROOT.war

The BASH script plug-in was created by navigating to **Manage** > **Plug-ins** and looks something like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#!/bin/bash

# The sleep command is used to wait for Oracle to fully initialize
sleep 10;
rm –rf $delete_dir
curl -L -o $dir $file_url
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**$delete_dir**, **$dir** and **$file_url** are overrideable arguments that can be defined when creating the plug-ins or when requesting the application.

WebSphere is invoking a different BASH script plug-in that will first execute init-server-env.sh and then deploy the Java WAR file from the accessible GitHub URL

<https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war>

<https://github.com/dchqinc/dchq-docker-java-example/blob/master/docker/init-server-env.sh>

The Java WAR file will be deployed on this directory:

-   WebSphere– dir= /opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect.war

The BASH script plug-in was created by navigating to **Manage** > **Plug-ins** and looks something like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#!/bin/bash

echo database_driverClassName=$database_driverClassName >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_url=$database_url >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_username=$database_username >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_password=$database_password >> /opt/ibm/wlp/usr/servers/defaultServer/server.env

rm -rf $delete_dir
wget $file_url -O $dir
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

**$delete_dir**, **$dir** and **$file_url** are overrideable arguments that can be defined when creating the plug-ins or when requesting the application.

You will notice that the **cluster_size** parameter allows you to specify the number of containers to launch (with the same application dependencies).

The **host** parameter allows you to specify the host you would like to use for container deployments. This is possible if you have selected Weave as the networking layer when creating your clusters. That way you can ensure high-availability for your application server clusters across different hosts (or regions) and you can comply with affinity rules to ensure that the database runs on a separate host for example. Here are the values supported for the host parameter:

-   *host1, host2, host3*, etc. – selects a host randomly within a data-center (or cluster) for container deployments

-   *<IP Address 1, IP Address 2, etc.>* -- allows a user to specify the actual IP addresses to use for container deployments

-   *<Hostname 1, Hostname 2, etc.>* -- allows a user to specify the actual hostnames to use for container deployments

-   *Wildcards* (e.g. “db-*”, or “app-srv-*”) – to specify the wildcards to use within a hostname

Additionally, a user can create cross-image environment variable bindings by making a reference to another image’s environment variable. In this case, we have made several bindings – including database_url=jdbc:mysql://{{MySQL|container_ip}}:3306/{{MySQL|MYSQL_DATABASE}} – in which the database container IP is resolved dynamically at request time and is used to ensure that the application servers can establish a connection with the database.

Here is a list of supported environment variable values:

-   **{{alphanumeric | 8}}** – creates a random 8-character alphanumeric string. This is most useful for creating random passwords.

-   **{{<Image Name> | ip}}** – allows you to enter the host IP address of a container as a value for an environment variable. This is most useful for allowing the middleware tier to establish a connection with the database.

-   **{{<Image Name> | container_ip}}** – allows you to enter the name of a container as a value for an environment variable. This is most useful for allowing the middleware tier to establish a secure connection with the database (without exposing the database port).

-   **{{<Image Name> | container_private_ip}}** – allows you to enter the internal IP of a container as a value for an environment variable. This is most useful for allowing the middleware tier to establish a secure connection with the database (without exposing the database port).

-   **{{<Image Name> | port _<Port Number>}}** – allows you to enter the Port number of a container as a value for an environment variable. This is most useful for allowing the middleware tier to establish a connection with the database. In this case, the port number specified needs to be the internal port number – i.e. not the external port that is allocated to the container. For example, {{PostgreSQL | port_5432}} will be translated to the actual external port that will allow the middleware tier to establish a connection with the database.

-   **{{<Image Name> | <Environment Variable Name>}}** – allows you to enter the value an image’s environment variable into another image’s environment variable. The use cases here are endless – as most multi-tier applications will have cross-image dependencies.

 

### 3-Tier Java (Nginx – Tomcat – MySQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: tomcat:8.0.21-jre8
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=com.mysql.jdbc.Driver
    - database_url=jdbc:mysql://{{MySQL|container_ip}}:3306/{{MySQL|MYSQL_DATABASE}}
    - database_username={{MySQL|MYSQL_USER}}
    - database_password={{MySQL|MYSQL_ROOT_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/usr/local/tomcat/webapps/ROOT.war
        - delete_dir=/usr/local/tomcat/webapps/ROOT
MySQL:
  image: mysql:latest
  host: host1
  mem_min: 400m
  environment:
    - MYSQL_USER=root
    - MYSQL_DATABASE=names
    - MYSQL_ROOT_PASSWORD={{alphanumeric|8}}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – Jetty – MySQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jetty:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=com.mysql.jdbc.Driver
    - database_url=jdbc:mysql://{{MySQL|container_ip}}:3306/{{MySQL|MYSQL_DATABASE}}
    - database_username={{MySQL|MYSQL_USER}}
    - database_password={{MySQL|MYSQL_ROOT_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/var/lib/jetty/webapps/ROOT.war
        - delete_dir=/var/lib/jetty/webapps/ROOT
MySQL:
  image: mysql:latest
  host: host1
  mem_min: 400m
  environment:
    - MYSQL_USER=root
    - MYSQL_DATABASE=names
    - MYSQL_ROOT_PASSWORD={{alphanumeric|8}}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – JBoss – MySQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jboss/wildfly:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=com.mysql.jdbc.Driver
    - database_url=jdbc:mysql://{{MySQL|container_ip}}:3306/{{MySQL|MYSQL_DATABASE}}
    - database_username={{MySQL|MYSQL_USER}}
    - database_password={{MySQL|MYSQL_ROOT_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/jboss/wildfly/standalone/deployments/ROOT.war
        - delete_dir=/opt/jboss/wildfly/standalone/deployments/ROOT
MySQL:
  image: mysql:latest
  host: host1
  mem_min: 400m
  environment:
    - MYSQL_USER=root
    - MYSQL_DATABASE=names
    - MYSQL_ROOT_PASSWORD={{alphanumeric|8}}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 2-Tier Java (WebSphere – MySQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
AppServer:
  image: websphere-liberty:webProfile6
  publish_all: true
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=com.mysql.jdbc.Driver
    - database_url=jdbc:mysql://{{MySQL|container_ip}}:3306/{{MySQL|MYSQL_DATABASE}}
    - database_username={{MySQL|MYSQL_USER}}
    - database_password={{MySQL|MYSQL_ROOT_PASSWORD}}
    - LICENSE=accept
  plugins:
    - !plugin
      id: rPuVb
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect.war
        - delete_dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect
MySQL:
  image: mysql:latest
  host: host1
  mem_min: 400m
  environment:
    - MYSQL_USER=root
    - MYSQL_DATABASE=names
    - MYSQL_ROOT_PASSWORD={{alphanumeric|8}}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

<http://pygments.org/>

### 3-Tier Java (Nginx – Tomcat – PostgreSQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: tomcat:8.0.21-jre8
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=org.postgresql.Driver
    - database_url=jdbc:postgresql://{{Postgres|container_ip}}:5432/{{Postgres|POSTGRES_DB}}
    - database_username={{Postgres|POSTGRES_USER}}
    - database_password={{Postgres|POSTGRES_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/usr/local/tomcat/webapps/ROOT.war
        - delete_dir=/usr/local/tomcat/webapps/ROOT
Postgres:
  image: postgres:latest
  host: host1
  mem_min: 400m
  environment:
    - POSTGRES_USER=root
    - POSTGRES_PASSWORD={{alphanumeric | 8}}
    - POSTGRES_DB=names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – Jetty – PostgreSQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jetty:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=org.postgresql.Driver
    - database_url=jdbc:postgresql://{{Postgres|container_ip}}:5432/{{Postgres|POSTGRES_DB}}
    - database_username={{Postgres|POSTGRES_USER}}
    - database_password={{Postgres|POSTGRES_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/var/lib/jetty/webapps/ROOT.war
        - delete_dir=/var/lib/jetty/webapps/ROOT
Postgres:
  image: postgres:latest
  host: host1
  mem_min: 400m
  environment:
    - POSTGRES_USER=root
    - POSTGRES_PASSWORD={{alphanumeric | 8}}
    - POSTGRES_DB=names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – JBoss – PostgreSQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jboss/wildfly:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=org.postgresql.Driver
    - database_url=jdbc:postgresql://{{Postgres|container_ip}}:5432/{{Postgres|POSTGRES_DB}}
    - database_username={{Postgres|POSTGRES_USER}}
    - database_password={{Postgres|POSTGRES_PASSWORD}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/jboss/wildfly/standalone/deployments/ROOT.war
        - delete_dir=/opt/jboss/wildfly/standalone/deployments/ROOT
Postgres:
  image: postgres:latest
  host: host1
  mem_min: 400m
  environment:
    - POSTGRES_USER=root
    - POSTGRES_PASSWORD={{alphanumeric | 8}}
    - POSTGRES_DB=names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 2-Tier Java (WebSphere – PostgreSQL)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
AppServer:
  image: websphere-liberty:webProfile6
  publish_all: true
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=org.postgresql.Driver
    - database_url=jdbc:postgresql://{{Postgres|container_ip}}:5432/{{Postgres|POSTGRES_DB}}
    - database_username={{Postgres|POSTGRES_USER}}
    - database_password={{Postgres|POSTGRES_PASSWORD}}
    - LICENSE=accept
  plugins:
    - !plugin
      id: rPuVb
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect.war
        - delete_dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect
Postgres:
  image: postgres:latest
  host: host1
  mem_min: 400m
  environment:
    - POSTGRES_USER=root
    - POSTGRES_PASSWORD={{alphanumeric | 8}}
    - POSTGRES_DB=names
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – Tomcat – Oracle-XE)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: tomcat:8.0.21-jre8
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=oracle.jdbc.OracleDriver
    - database_url=jdbc:oracle:thin:@//{{Oracle|container_ip}}:1521/{{Oracle|sid}}
    - database_username={{Oracle|username}}
    - database_password={{Oracle|password}}
    - TZ=UTC
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/usr/local/tomcat/webapps/ROOT.war
        - delete_dir=/usr/local/tomcat/webapps/ROOT
Oracle:
  image: wnameless/oracle-xe-11g:latest
  host: host1
  mem_min: 400m
  environment:
    - username=system
    - password=oracle
    - sid=xe
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – Jetty – Oracle-XE)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jetty:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=oracle.jdbc.OracleDriver
    - database_url=jdbc:oracle:thin:@//{{Oracle|container_ip}}:1521/{{Oracle|sid}}
    - database_username={{Oracle|username}}
    - database_password={{Oracle|password}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/var/lib/jetty/webapps/ROOT.war
        - delete_dir=/var/lib/jetty/webapps/ROOT
Oracle:
  image: wnameless/oracle-xe-11g:latest
  host: host1
  mem_min: 400m
  environment:
    - username=system
    - password=oracle
    - sid=xe
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 3-Tier Java (Nginx – JBoss – Oracle-XE)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
LB:
  image: nginx:latest
  publish_all: true
  mem_min: 50m
  host: host1
  plugins:
    - !plugin
      id: 0H1Nk
      restart: true
      arguments:
        - servers=server {{AppServer | container_ip}}:8080;
AppServer:
  image: jboss/wildfly:latest
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=oracle.jdbc.OracleDriver
    - database_url=jdbc:oracle:thin:@//{{Oracle|container_ip}}:1521/{{Oracle|sid}}
    - database_username={{Oracle|username}}
    - database_password={{Oracle|password}}
  plugins:
    - !plugin
      id: oncXN
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/jboss/wildfly/standalone/deployments/ROOT.war
        - delete_dir=/opt/jboss/wildfly/standalone/deployments/ROOT
Oracle:
  image: wnameless/oracle-xe-11g:latest
  host: host1
  mem_min: 400m
  environment:
    - username=system
    - password=oracle
    - sid=xe
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### 2-Tier Java (WebSphere – Oracle-XE)

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
AppServer:
  image: websphere-liberty:webProfile6
  publish_all: true
  mem_min: 600m
  host: host1
  cluster_size: 1
  environment:
    - database_driverClassName=oracle.jdbc.OracleDriver
    - database_url=jdbc:oracle:thin:@//{{Oracle|container_ip}}:1521/{{Oracle|sid}}
    - database_username={{Oracle|username}}
    - database_password={{Oracle|password}}
    - LICENSE=accept
  plugins:
    - !plugin
      id: rPuVb
      restart: true
      arguments:
        - file_url=https://github.com/dchqinc/dchq-docker-java-example/raw/master/dbconnect.war
        - dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect.war
        - delete_dir=/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect
Oracle:
  image: wnameless/oracle-xe-11g:latest
  host: host1
  mem_min: 400m
  environment:
    - username=system
    - password=oracle
    - sid=xe
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 

### Invoking a plug-in to initialize the database separately on a 3-Tier Java (Nginx – Tomcat – MySQL)

We recommend initializing the database schema as part of the Java WAR file deployment itself. However if you still prefer executing the SQL files on the database separately – then DCHQ can help you automate this process through its plug-in framework.

In this example, MySQL in this 3-Tier application is invoking a BASH script plug-in to execute the upgrade.sql file. The BASH script plug-in was created by navigating to **Manage > Plug-ins** and looks something like this:

~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#!/bin/bash

apt-get update
apt-get -y install wget
cd /
wget $file_url
/usr/bin/mysql -u $MYSQL_USER -p$MYSQL_ROOT_PASSWORD -h127.0.0.1 $MYSQL_DATABASE < /upgrade.sql
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this BASH script plug-in, **$MYSQL_USER**, **$MYSQL_ROOT_PASSWORD**, and **$MYSQL_DATABASE** are environment variables that are passed at request time.

**$file_url** is an overrideable argument that you can define when creating the plug-in or when requesting the application. For example, this could be the upgrade.sql file from GitHub:

<https://github.com/dchqinc/dchq-docker-java-example/raw/master/src/main/webapp/WEB-INF/upgrade/upgrade.sql>

 

### Using your script or deployment plan

If you’re deploying your application on Oracle WebLogic Application Server and you would like to use your own deployment plan or custom Python script, then you can easily create a BASH script plug-in by navigating to **Manage** > **Plug-ins**. The plug-in for WebLogic may look something like this:

#!/bin/bash

cd /oracle/fmwhome/wlst_custom/

wget $deploy-python-url

wget $deploy_app-sh-url

wget $war-file-url

chmod +x deploy_app.sh

./deploy_app.sh

**$deploy-python-url**, **$deploy_app-sh-url**, and **$war-file-url** are overrideable arguments that you can define when creating the plug-in or when requesting the application.

-   **$deploy-python-url**=(URL location for deploy.py script)

-   **$deploy_app-sh-url**=(URL location for deploy_app.sh file), and

-   **$war-file-url**=(URL location for the Java WAR file)

 

Provisioning & Auto-Scaling the Underlying Infrastructure on Any Cloud
----------------------------------------------------------------------

Once an application is saved, a user can register a Cloud Provider to automate the provisioning and auto-scaling of clusters on 12 different cloud end-points including VMware vSphere, OpenStack, CloudStack, Amazon Web Services, Rackspace, Microsoft Azure, DigitalOcean, IBM SoftLayer, Google Compute Engine, and many others.

First, a user can register a Cloud Provider for Rackspace (for example) by navigating to **Manage** > **Repo & Cloud Provider** and then clicking on the **+** button to select **Rackspace**. The Rackspace API Key needs to be provided – which can be retrieved from the Account Settings section of the Rackspace Cloud Control Panel.

A user can then create a cluster with an auto-scale policy to automatically spin up new Cloud Servers. This can be done by navigating to **Manage** > **Clusters** page and then clicking on the **+** button. You can select a capacity-based placement policy and then **Weave** as the networking layer in order to facilitate secure, password-protected cross-container communication across multiple hosts within a cluster. The **Auto-Scale Policy** in this example sets the maximum number of VM’s (or Cloud Servers) to 10.

A user can now provision a number of Cloud Servers on the newly created cluster either through the UI-based workflow or by defining a simple YAML-based Machine Compose template that can be requested from the Self-Service Library.

**UI-based Workflow** – A user can request Rackspace Cloud Servers by navigating to **Manage** > **Hosts** and then clicking on the **+** button to select **Rackspace**. Once the Cloud Provider is selected, a user can select the region, size and image needed. Ports are opened by default on Rackspace Cloud Servers to accommodate some of the port requirements (e.g. 32000-59000 for Docker, 6783 for Weave, and 5672 for RabbitMQ). A Cluster is then selected and the number of Cloud Servers can be specified.

 

**YAML-based Machine Compose Template** – A user can first create a Machine Compose template for Rackspace by navigating to **Manage** > **Templates** and then selecting **Machine Compose**.

Here’s the template for requesting a 4GB Cloud Server.

Medium:

region: IAD

description: Rackspace small instance

instanceType: general1-4

image: IAD/5ed162cc-b4eb-4371-b24a-a0ae73376c73

count: 1

The supported parameters for the Machine Compose template are summarized below:

-   **description**: Description of the blueprint/template

-   **instanceType**: Cloud provider specific value (e.g. general1-4)

-   **region**: Cloud provider specific value (e.g. IAD)

-   **image**: Mandatory - fully qualified image ID/name (e.g. IAD/5ed162cc-b4eb-4371-b24a-a0ae73376c73 or vSphere VM Template name)

-   **username**: Optional - only for vSphere VM Template username

-   **password**: Optional - only for vSphere VM Template encrypted password. You can encrypt the password using the endpoint <https://www.dchq.io/#/encrypt>

-   **network**: Optional – Cloud provider specific value (e.g. default)

-   **securityGroup**: Cloud provider specific value (e.g. dchq-security-group)

-   **keyPair**: Cloud provider specific value (e.g. private key)

-   **openPorts**: Optional - comma separated port values

-   **count**: Total no of VM's, defaults to 1.

Once the Machine Compose template is saved, a user can request this machine from the Self-Service **Library**. A user can click **Customize** and then select the **Cloud Provider** and **Cluster** to use for provisioning these Rackspace Cloud Servers.

 

Deploying the Multi-Tier Java Application on the Rackspace Cluster
------------------------------------------------------------------

Once the Cloud Servers are provisioned, a user can deploy a multi-tier, Docker-based Java applications on the new Cloud Servers. This can be done by navigating to the Self-Service Library and then clicking on Customize to request a multi-tier application.

A user can select an Environment Tag (like DEV or QE) and the Rackspace Cluster created before clicking on Run.

 

Accessing The In-Browser Terminal For The Running Containers
------------------------------------------------------------

A command prompt icon should be available next to the containers’ names on the Live Apps page. This allows users to enter the container using a secure communication protocol through the agent message queue. A white list of commands can be defined by the Tenant Admin to ensure that users do not make any harmful changes on the running containers.

For the Tomcat deployment for example, we used the command prompt to make sure that the Java WAR file was deployed under the /usr/local/tomcat/webapps/ directory.

 

Monitoring the CPU, Memory & I/O Utilization of the Running Containers
----------------------------------------------------------------------

Once the application is up and running, our developers monitor the CPU, Memory, & I/O of the running containers to get alerts when these metrics exceed a pre-defined threshold. This is especially useful when our developers are performing functional & load testing.

A user can perform historical monitoring analysis and correlate issues to container updates or build deployments. This can be done by clicking on the **Actions** menu of the running application and then on **Monitoring**. A custom date range can be selected to view CPU, Memory and I/O historically.

 

Enabling the Continuous Delivery Workflow with Jenkins to Update the WAR File of the Running Application when a Build is Triggered
----------------------------------------------------------------------------------------------------------------------------------

For developers wishing to follow the “immutable” containers model by rebuilding Docker images containing the application code and spinning up new containers with every application update, DCHQ provides an automated build feature that allows developers to automatically create Docker images from Dockerfiles or private GitHub projects containing Dockerfiles.

However, many developers may wish to *update the running application server containers with the latest Java WAR file* instead. For that, DCHQ allows developers to enable a continuous delivery workflow with Jenkins. This can be done by clicking on the **Actions** menu of the running application and then selecting **Continuous Delivery**. A user can select a Jenkins instance that has already been registered with DCHQ, the actual Job on Jenkins that will produce the latest WAR file, and then a BASH script plug-in to grab this build and deploy it on a running application server. Once this policy is saved, DCHQ will grab the latest WAR file from Jenkins any time a build is triggered and deploy it on the running application server.

Developers, as a result will always have the latest Java WAR file deployed on their running containers in DEV/TEST environments.

 

Scaling out the Tomcat Application Server Cluster
-------------------------------------------------

If the running application becomes resource constrained, a user can to scale out the application to meet the increasing load. Moreover, a user can schedule the scale out during business hours and the scale in during weekends for example.

To scale out the cluster of Tomcat servers from 2 to 4, a user can click on the **Actions** menu of the running application and then select **Scale Out**. A user can then specify the new size for the cluster and then click on **Run Now**.

We then used the BASH plug-in to update Nginx’s default.conf file so that it’s aware of the new application server added. The BASH script plug-ins can also be scheduled to accommodate use cases like cleaning up logs or updating configurations at defined frequencies. An application time-line is available to track every change made to the application for auditing and diagnostics.

To execute a plug-in on a running container, a user can click on the **Actions** menu of the running application and then select **Plug-ins**. A user can then select the load balancer (Nginx) container, search for the plug-in that needs to be executed, enable container restart using the toggle button. The default argument for this plug-in will dynamically resolve all the container IP’s of the running Tomcat servers and add them as part of the default.conf file.

An application time-line is available to track every change made to the application for auditing and diagnostics. This can be accessed from the expandable menu at the bottom of the page of a running application.

Alerts and notifications are available for when containers or hosts are down or when the CPU & Memory Utilization of either hosts or containers exceed a defined threshold.

 

Conclusion
----------

Containerizing enterprise Java applications is still a challenge mostly because existing application composition frameworks do not address complex dependencies, external integrations or auto-scaling workflows post-provision. Moreover, the ephemeral design of containers meant that developers had to spin up new containers and re-create the complex dependencies & external integrations with every version update.

DCHQ, available in hosted and on-premise versions, addresses all of these challenges and simplifies the containerization of enterprise Java applications through an advance application composition framework that facilitates cross-image environment variable bindings, extensible BASH script plug-ins that can be invoked at request time or post-provision, and application clustering for high availability across multiple hosts or regions with support for auto scaling.

Sign Up for FREE on <http://DCHQ.io> or download [DCHQ On-Premise](<http://dchq.co/dchq-on-premise.html>) to get access to out-of-box multi-tier Java application templates along with application lifecycle management functionality like monitoring, container updates, scale in/out and continuous delivery.
