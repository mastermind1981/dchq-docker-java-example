MySQL
===


PostgreSQL
===

-Ddatabase_driverClassName=org.postgresql.Driver
-Ddatabase_url=jdbc:postgresql://host:5432/db
-Ddatabase_username=username
-Ddatabase_password=password

Oracle
===

-Ddatabase_driverClassName=oracle.jdbc.OracleDriver
-Ddatabase_url=jdbc:oracle:thin:host:1521:XE
-Ddatabase_username=username
-Ddatabase_password=password

docker run -d -p 1521:1521 -p 8081:8080 sath89/oracle-xe-11g

Download Oracle JDBC driver from
/u01/app/oracle/product/11.2.0/xe/jdbc/lib/ojdbc6.jar

mvn install:install-file -DgroupId=com.oracle -DartifactId=ojdbc7 -Dversion=12.1.0.1 -Dpackaging=jar -Dfile=ojdbc7.jar -DgeneratePom=true
