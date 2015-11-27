Deploying WebSphere
===
Copy `dbconnect.war` and `init-server-env.sh` to the `/tmp/` on a host with Docker.

Run default WebSphere instance.

`sudo docker run -e LICENSE=accept -d -p 9080:9080 -p 9443:9443
-e database_driverClassName=com.mysql.jdbc.Driver
-e database_url=jdbc:mysql://host:3306/information_schema
-e database_username=username
-e database_password=password
 websphere-liberty:webProfile6
`

Deploy war file to the host.

`chmod a+x /tmp/init-server-env.sh`
`sudo docker cp /tmp/init-server-env.sh <container_id>:/opt/ibm/wlp/usr/servers/defaultServer/init-server-env.sh`
`sudo docker exec <container_id> /opt/ibm/wlp/usr/servers/defaultServer/init-server-env.sh`
`sudo docker cp /tmp/dbconnect.war <container_id>:/opt/ibm/wlp/usr/servers/defaultServer/dropins/dbconnect.war`

`sudo docker restart <container_id>`

Change **<container_id>** to valid value (eg *fe425a5b114f*).


Deploying Tomcat
===
Copy `dbconnect.war` to the `/tmp/` on a host with Docker.

`sudo docker run -d -p 9080:8080 -P --link mysql
-e database_driverClassName=com.mysql.jdbc.Driver
-e database_url=jdbc:mysql://mysql-ip:3306/names
-e database_username=root
-e database_password=password tomcat:8.0.21-jre8`

`sudo docker cp /tmp/dbconnect.war <container_id>:/usr/local/tomcat/webapps/dbconnect.war`


Deploying Jetty
===
Copy `dbconnect.war` to the `/tmp/` on a host with Docker.

`sudo docker run -d -p 9080:8080 -P --link mysql
-e database_driverClassName=com.mysql.jdbc.Driver
-e database_url=jdbc:mysql://mysql-ip:3306/names
-e database_username=root
-e database_password=password jetty:latest`

`sudo docker cp /tmp/dbconnect.war <container_id>:/var/lib/jetty/webapps/dbconnect.war`


Deploying JBoss
===
Copy `dbconnect.war` to the `/tmp/` on a host with Docker.

`sudo docker run -d -p 9080:8080 -P --link mysql
-e database_driverClassName=com.mysql.jdbc.Driver
-e database_url=jdbc:mysql://mysql-ip:3306/names
-e database_username=root
-e database_password=password jboss/wildfly:latest`

`sudo docker cp /tmp/dbconnect.war <container_id>:/opt/jboss/wildfly/standalone/deployments/dbconnect.war`

