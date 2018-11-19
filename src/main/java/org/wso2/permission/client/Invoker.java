package org.wso2.permission.client;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.permission.client.util.AuthenticationServiceClient;
import org.wso2.permission.client.util.PropertyReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class Invoker {
    private static final Logger log = Logger.getLogger(Invoker.class);
    private static Properties configs;
    private static String tenantDomain;

    public static void main( String[] args ) throws IOException, LoginAuthenticationExceptionException {
        // Initialize
        try {
            initialize();
        } catch (IOException e) {
            log.error("Error while initializing the client. Aborting process.", e);
            throw e;
        }

        // Authenticate tenant
        String cookie;
        try {
            cookie = login();
        } catch (RemoteException | LoginAuthenticationExceptionException | MalformedURLException e) {
            log.error("Error while authentication. Aborting the process", e);
            throw e;
        }


    }

    /**
     * This method reads client configurations from configurations.properties file.
     */
    public static void initialize() throws IOException {
        log.info("Initialization started.");
        // Set log4j configs
        PropertyConfigurator.configure(Paths.get(".", Constants.LOG4J_PROPERTIES).toString());
        // Read client configurations
        configs = PropertyReader.loadProperties(Paths.get(".", Constants.CONFIGURATION_PROPERTIES).toString());

        // Set trust-store configurations to the JVM
        log.info("Setting trust store configurations to JVM.");
        if (configs.getProperty(Constants.TRUST_STORE_PASSWORD) != null && configs.getProperty(Constants.TRUST_STORE_TYPE) != null
                && configs.getProperty(Constants.TRUST_STORE) != null) {
            System.setProperty("javax.net.ssl.trustStore", Paths.get(".", configs.getProperty(Constants.TRUST_STORE)).toString());
            System.setProperty("javax.net.ssl.trustStorePassword", configs.getProperty(Constants.TRUST_STORE_PASSWORD));
            System.setProperty("javax.net.ssl.trustStoreType", configs.getProperty(Constants.TRUST_STORE_TYPE));
        } else {
            log.error("Trust store configurations missing in the configurations.properties file. Aborting process.");
            System.exit(1);
        }
        log.info("Initialization finished.");
    }

    public static String login() throws RemoteException, LoginAuthenticationExceptionException, MalformedURLException {
        log.info("Authentication started");
        URL baseUrl = new URL (configs.getProperty(Constants.BACK_END_URL));
        String serviceUrl = new URL(baseUrl, Constants.AUTHENTICATOR_SERVICE_PATH).toString();

        log.info("Creating authentication service client on URL : " + serviceUrl);
        AuthenticationServiceClient authenticator = new AuthenticationServiceClient(serviceUrl);

        // Construct username with tenant domain and password from properties
        String userName = configs.getProperty(Constants.ADMIN_USERNAME);
        String password = configs.getProperty(Constants.ADMIN_PASSWORD);
        tenantDomain = userName.substring(userName.lastIndexOf('@') + 1);

        // Return session cookie
        log.info("Authenticating user : " + userName + " password : " + password + " tenant domain : " + tenantDomain);
        return authenticator.authenticate(userName, password, tenantDomain);
    }
}
