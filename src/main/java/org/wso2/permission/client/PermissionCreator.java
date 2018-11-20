/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.permission.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.log4j.Logger;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;

public class PermissionCreator {
    private static Logger log = Logger.getLogger(PermissionCreator.class);
    private ResourceAdminServiceStub resourceStub;
    private PropertiesAdminServiceStub propertiesStub;
    private Properties configs;

    /**
     * Create and configure registry service resourceStub.
     * @param cookie
     * @param configs
     * @throws MalformedURLException
     * @throws AxisFault
     */
    public PermissionCreator (String cookie, Properties configs) throws MalformedURLException, AxisFault {
        this.configs = configs;
        // Create and configure stubs.
        if (configs.getProperty(Constants.BACK_END_URL) != null) {
            // Create resource stub.
            URL baseUrl = new URL(configs.getProperty(Constants.BACK_END_URL));
            String serviceUrl = new URL(baseUrl, Constants.RESOURCE_SERVICE_PATH).toString();
            log.info("Creating resource admin service stub for the service URL : " + serviceUrl);
            resourceStub = new ResourceAdminServiceStub(serviceUrl);
            // Create properties stub.
            serviceUrl = new URL(baseUrl, configs.getProperty(Constants.PROPERTIES_SERVICE_PATH)).toString();
            log.info("Creating registry properties admin service stub for the service URL : " + serviceUrl);
            propertiesStub = new PropertiesAdminServiceStub(serviceUrl);

            // Configure resourceStub
            ServiceClient serviceClient = resourceStub._getServiceClient();
            Options options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            // Configure property stub.
            serviceClient = propertiesStub._getServiceClient();
            options = serviceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } else {
            log.error("Backend server URL missing in the " + Constants.CONFIGURATION_PROPERTIES + " file. Aborting process.");
            System.exit(1);
        }
    }

    /**
     * Create registry property.
     * @param path
     * @param name
     * @param value
     * @throws PropertiesAdminServiceRegistryExceptionException
     * @throws RemoteException
     */
    private void createProperty(String path, String name, String value) throws PropertiesAdminServiceRegistryExceptionException, RemoteException {
        log.info("Creating registry property, name : " + name + " path : " + path + " value : " + value);
        propertiesStub.setProperty(path, name, value);

    }

    /**
     * Create registry collection.
     * @param collectionName
     * @param parentPath
     * @param description
     * @throws RemoteException
     * @throws ResourceAdminServiceExceptionException
     */
    private void createCollection(String collectionName, String parentPath, String description)
            throws RemoteException, ResourceAdminServiceExceptionException {
        log.info("Creating registry collection, name : " + collectionName + " parentPath : " + parentPath + " description : " + description);
        resourceStub.addCollection(parentPath, collectionName, null, description);
    }

    /**
     * This method reads the DTOs array and creates permissions in the Identity Server.
     * @param permissions
     */
    public void createPermissions(ArrayList<DTO> permissions) {
        log.info("Started creating permissions in the Identity Server.");
        for (DTO permission : permissions) {
            try {
                createCollection(permission.getCollectionName(), permission.getCollectionParentPath(), permission.getCollectionDescription());
                createProperty(permission.getPropertyPath(), permission.getPropertyName(), permission.getPropertyValue());
            }catch (RemoteException | ResourceAdminServiceExceptionException | PropertiesAdminServiceRegistryExceptionException e){
                log.error("Error while creating permission. Collection name : " + permission.getCollectionName() + " Property value : " + permission.getPropertyValue());
            }
        }
        log.info("Finished creating permission in the identity Server.");
    }
}
