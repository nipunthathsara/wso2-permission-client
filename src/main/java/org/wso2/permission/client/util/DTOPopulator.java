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

package org.wso2.permission.client.util;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.NumberUtils;
import org.apache.log4j.Logger;
import org.wso2.permission.client.Constants;
import org.wso2.permission.client.DTO;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This class provides functionality to populate the permissions DTOs to invoke the service.
 * Override this class to use this client against a different data set.
 */
public class DTOPopulator {
    private static Logger log = Logger.getLogger(DTOPopulator.class);
    private Properties configs;
    private ArrayList<DTO> permissions = new ArrayList();
    private List<CSVRecord> csvRecords;

    /**
     * Read and parse permissions CSV file.
     *
     * @param configs
     * @throws IOException
     */
    public DTOPopulator(Properties configs) throws IOException {
        this.configs = configs;
        // Read CSV file.
        FileReader fileReader = null;
        log.info("reading permissions csv file.");
        if (configs.getProperty(Constants.PERMISSSIONS_CSV_FILE) != null) {
            fileReader = new FileReader(Paths.get(".", configs.getProperty(Constants.PERMISSSIONS_CSV_FILE)).toString());
        } else {
            log.error("Permissions CSV file name not defined in the" + Constants.CONFIGURATION_PROPERTIES + " file. Aborting process.");
            System.exit(1);
        }

        CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT);
        // Retrieving records from CSV file.
        csvRecords = csvParser.getRecords();
    }

    /**
     * Populates and returns the permission DTOs.
     *
     * @return
     */
    public ArrayList<DTO> getPermissions() {
        log.info("Started populating permissions DTOs.");
        DTO permission;
        // Iterate through each record of the CSV file and populate DTOs.
        if (csvRecords != null) {
            for (int i = 0; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);
                // In order to be a valid record, should have 2 columns, a non empty property value. Numerical collection name.
                if (record.size() != 2 || record.get(0).isEmpty() || !NumberUtils.isNumber(record.get(1).trim())) {
                    log.info("Skipping record due to invalid format : " + record.toString());
                    continue;
                }

                permission = new DTO();
                // Set collection attributes.
                permission.setCollectionName(record.get(1));
                permission.setCollectionDescription(null);
                permission.setCollectionMediaType("");
                permission.setCollectionParentPath(configs.getProperty(Constants.REGISTRY_PARENT_PATH));

                // Set property attributes.
                permission.setPropertyName(Constants.PROPERTY_NAME);
                permission.setPropertyPath(Paths.get(configs.getProperty(Constants.REGISTRY_PARENT_PATH), permission.getCollectionName()).toString());
                permission.setPropertyValue(record.get(0)); // Display name of permission

                permissions.add(permission);
            }
        } else {
            log.error("No CSV records found. Aborting process.");
            System.exit(1);
        }
        return permissions;
    }
}
