Configure
===

Open the `src/main/webapp/WEB-INF/database.properties` file.

Set the following environmental values:
`database_driverClassName=com.mysql.jdbc.Driver`
`database_url=jdbc:mysql://10.1.1.3:3306/information_schema`
`database_username=your_user`
`database_password=your_password`


There is one Servlet what connects to MySQL and prints version on index page.
You can find it there `src/main/java/dchq/dbconnect/IndexServlet.java`


Packaging
===
You need to have Java to build this sample.
You need to have Maven to create a WAR file.

Run
`mvn package`

Now you can use `target/dbconnect.war`.
