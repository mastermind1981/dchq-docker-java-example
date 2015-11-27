package dchq.dbconnect.service;

/**
 * @since 11/25/2015
 */
public class NameDirectoryServiceFactory {
    private static NameDirectoryServiceFactory service = new NameDirectoryServiceFactory();

    private String driverClassName;

    public NameDirectoryService createService() {
        if (driverClassName.contains("mysql")) {
            return new NameDirectoryServiceMysql();
        } else if (driverClassName.contains("postgresql")) {
            return new NameDirectoryServicePostgresql();
        } else if (driverClassName.contains("oracle")) {
            return new NameDirectoryServiceOracle();
        }
        throw new UnsupportedOperationException("Unknown Database Type");
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public static NameDirectoryServiceFactory createFactory() {
        return service;
    }
}
