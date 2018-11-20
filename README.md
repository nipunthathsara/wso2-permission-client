# wso2-permission-client
Client to automate external permission creation in WSO2 Identity Server.

* Input data

    This client requires a two columned CSV formatted input data file.
    
     1.First value - String with display name of the permission. (Property value)
     
    2.Second value - Unique and Numeric value for the permission id. (Collection name) 
    
##Instrunctions
1. Clone the repo and build parent pom using `mvn clean install`
2. Copy and extract the built artifact `org.wso2.permission.client-1.0-SNAPSHOT-bundle.zip` from the $target directory.
3. Do the `configurations.properties` file changes appropriately.
    1. admin.username - Tenant admin user to authenticate the service stubs. Use the fully qualified user name with tenant domain. (Eg. admin@carbon.super)
    2. admin.password - password of tenant admin.
    3. backend.url - Host and port of the Identity Server.
    4. truststore - name of the client truststore file.
    5. truststore.password - client trust store password.
    6. truststore.type - Client trust store type.
    7. registry.parent.path - Registry path where the new permissions should be created
    8. permisssions.csv.file - File name of the permissions csv file described above.
4. Place the `permissions.csv` file in the extracted folder.
5. Run the client using `java -jar ./org.wso2.permission.client-1.0-SNAPSHOT-jar-with-dependencies.jar` command.
6. Observe the logs in created log.out file.