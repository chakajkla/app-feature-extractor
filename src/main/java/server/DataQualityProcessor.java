/**
 * 
 */
package server;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;

import server.database.DataAccess;

/**
 * @author niessen
 *
 */
public class DataQualityProcessor
{
    public static final String [] FILE_HEADER_MAPPING = {"session_id","context_event_id","context_event_type","timestamp","timezone","property_key","property_value"};
    private static final String SENSOR_TYPE_APP = "CONTEXT_SENSOR_APP";
    private static final String SENSOR_TYPE_CONNECTIVITY = "CONTEXT_SENSOR_CONNECTIVITY";
    private static final String SENSOR_TYPE_SETTINGS = "CONTEXT_SENSOR_SETTINGS";
    private static final String SENSOR_TYPE_PACKAGE = "CONTEXT_SENSOR_PACKAGE";
    private static final String SENSOR_TYPE_DEVICE_PROTECTION = "CONTEXT_SENSOR_DEVICE_PROTECTION";
    private static final String SENSOR_TYPE_LOCATION = "CONTEXT_SENSOR_LOCATION";
    private static final String SENSOR_TYPE_GENERIC = "CONTEXT_SENSOR_GENERIC";
    private static final String SENSOR_TYPE_AWARENESS = "CONTEXT_SENSOR_AWARENESS";
    private static final String SENSOR_TYPE_NOTIFICATION = "CONTEXT_SENSOR_NOTIFICATION";
    public static final String SENSOR_TYPE_INTERACTION = "CONTEXT_SENSOR_INTERACTION";
    public static final String SENSOR_TYPE_LABELLING = "CONTEXT_SENSOR_LABELLING";
    
    private String filePath;
    private String fileName;
    
    private boolean appSensorRecorded = false;
    private boolean interactionSensorRecorded = false;
    private boolean connectivitySensorRecorded = false;
    private boolean deviceProtectionSensorRecorded = false;
    private boolean awarenessSensorRecorded = false;
    private boolean settingsSensorRecorded = false;
    private boolean labellingSensorRecorded = false;
    private boolean locationSensorRecorded = false;
    
    private boolean awarenessSensorValuesCorrect = false;
    private boolean connectivitySensorValuesCorrect = false;
    private boolean deviceProtectionSensorValuesCorrect = false;
    private boolean settingsSensorValuesCorrect = false;
    private boolean labellingSensorValuesCorrect = false;
    private boolean locationSensorValuesCorrect = false;
    
    public DataQualityProcessor(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public boolean checkLabeledDataFile() {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        
        
        try {
            DataAccess.updateLabelledFile(fileName, 1, "Currently checked");
            
            fileReader = new FileReader(filePath + fileName);
            csvFileParser = CSVFormat.DEFAULT.withDelimiter(';').withHeader(FILE_HEADER_MAPPING).withRecordSeparator('\n').parse(fileReader);
            
            //Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            
            // Check all default sensors are recorded
            check4SensorsRecorded(csvRecords);
            if (awarenessSensorRecorded) {
                awarenessSensorValuesCorrect = checkAwarenessSensorValues(csvRecords);
            }
            if (connectivitySensorRecorded) {
                connectivitySensorValuesCorrect = checkConnectivitySensorValues(csvRecords);
            }
            if (deviceProtectionSensorRecorded) {
                deviceProtectionSensorValuesCorrect = checkDeviceProtectionSensorValues(csvRecords);
            }
            if (settingsSensorRecorded) {
                settingsSensorValuesCorrect = checkSettingsSensorValues(csvRecords);
            }
            if (labellingSensorRecorded) {
                labellingSensorValuesCorrect = checkLabellingSensorValues(csvRecords);
            }
            if (locationSensorRecorded) {
                locationSensorValuesCorrect = checkLocationSensorValues(csvRecords);
            }
        } catch(Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
            DataAccess.updateLabelledFile(fileName, 3, "Error while reading file");
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
                if (csvFileParser != null) {
                    csvFileParser.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }
        
        return checkResults();
    }
    
    private boolean checkAwarenessSensorValues(List<CSVRecord> csvRecords) {
        int contextEventId = 0;
        try {
            for (CSVRecord record : csvRecords) {
                if (record.size() < 7) {
                    continue;
                }
                if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_AWARENESS)
                        && StringUtils.equals(record.get("property_key"), "type")
                        && StringUtils.equals(record.get("property_value"), "places")) {
                    contextEventId = Integer.parseInt(record.get("context_event_id"));
                    continue;
                }
                if (Integer.parseInt(record.get("context_event_id")) == contextEventId
                        && StringUtils.equals(record.get("property_key"), "typeString")) {
                    String propertyValue = record.get("property_value");
                    String[] properties = propertyValue.split(",");
                    for (String property : properties) {
                        String[] intValues = property.split("#");
                        for (String intValue : intValues) {
                            Integer.parseInt(intValue);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
    
    private boolean checkConnectivitySensorValues(List<CSVRecord> csvRecords) {
        boolean connectivitySensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (record.size() < 7) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_CONNECTIVITY)) {
                switch(record.get("property_key")) {
                    case "airplanemode":
                    case "wificonnected":
                    case "mobileconnected":
                    case "wifienabled":
                    case "bluetoothconnected":
                    case "hiddenssid":
                        String value = StringUtils.trimToEmpty(record.get("property_value"));
                        if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "false")) {
                            connectivitySensorValuesCorrect = true;
                        } else {
                            connectivitySensorValuesCorrect = false;
                        }
                        break;
                }
            }
        }
        return connectivitySensorValuesCorrect;
    }
    
    private boolean checkDeviceProtectionSensorValues(List<CSVRecord> csvRecords) {
        boolean deviceProtectionSensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (record.size() < 7) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_DEVICE_PROTECTION)) {
                switch(record.get("property_key")) {
                    case "ispasswordprotected":
                    case "istrustedantivirusinstalled":
                    case "accessibilityenabled":
                    case "musesdatabaseexists":
                    case "isrooted":
                        String value = StringUtils.trimToEmpty(record.get("property_value"));
                        if (StringUtils.equalsIgnoreCase(value, "true") || StringUtils.equalsIgnoreCase(value, "false")) {
                            deviceProtectionSensorValuesCorrect = true;
                        } else {
                            deviceProtectionSensorValuesCorrect = false;
                        }
                        break;
                }
            }
        }
        return deviceProtectionSensorValuesCorrect;
    }
    
    private boolean checkSettingsSensorValues(List<CSVRecord> csvRecords) {
        try {
            for (CSVRecord record : csvRecords) {
                if (record.size() < 7) {
                    continue;
                }
                if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_SETTINGS)
                        && StringUtils.equals(record.get("property_key"), "osversion")) {
                    Double.parseDouble(record.get("property_value"));
                }
            }
        } catch (NumberFormatException|NullPointerException e) {
            return false;
        }
        return true;
    }
    
    private boolean checkLabellingSensorValues(List<CSVRecord> csvRecords) {
        boolean labellingSensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (record.size() < 7) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_LABELLING)) {
                switch(record.get("property_key")) {
                    case "referencedId":
                    case "usage":
                    case "appName":
                        String value = StringUtils.trimToEmpty(record.get("property_value"));
                        if (!StringUtils.isBlank(value)) {
                            labellingSensorValuesCorrect = true;
                        } else {
                            labellingSensorValuesCorrect = false;
                        }
                        break;
                }
            }
        }
        return labellingSensorValuesCorrect;
    }
    
    private boolean checkLocationSensorValues(List<CSVRecord> csvRecords) {
        try {
            for (CSVRecord record : csvRecords) {
                if (record.size() < 7) {
                    continue;
                }
                if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_LABELLING)) {
                    switch(record.get("property_key")) {
                        case "longitude":
                        case "latitude":
                            Double.parseDouble(record.get("property_value"));
                            break;
                        case "hashid":
                            Integer.parseInt(record.get("property_value"));
                            
                            break;
                    }
                }
            }
        } catch (NumberFormatException|NullPointerException e) {
            return false;
        }
        return true;
    }
    
    private boolean checkResults() {
        String errorString = "";
        if (!appSensorRecorded || !interactionSensorRecorded || !connectivitySensorRecorded || !deviceProtectionSensorRecorded || !awarenessSensorRecorded) {
            StringBuilder builder = new StringBuilder();
            
            if (!appSensorRecorded)
                builder.append("appSensor, ");
            if (!interactionSensorRecorded)
                builder.append("interactionSensor, ");
            if (!connectivitySensorRecorded)
                builder.append("connectivitySensor, ");
            if (!deviceProtectionSensorRecorded)
                builder.append("deviceProtectionSensor, ");
            if (!awarenessSensorRecorded)
                builder.append("awarenessSensor, ");
            
            errorString += StringUtils.removeEnd(builder.toString(), ",") + "not recorded. ";
        }
        
        if (!awarenessSensorValuesCorrect) {
            errorString += "AwarenessSensor: values are invalid. ";
        }
        
        if (!connectivitySensorValuesCorrect) {
            errorString += "ConnectivitySensor: values are invalid. ";
        }
        
        if (!deviceProtectionSensorValuesCorrect) {
            errorString += "DeviceProtectionSensor: values are invalid. ";
        }
        
        if (settingsSensorRecorded && !settingsSensorValuesCorrect) {
            errorString += "SettingsSensor: values are invalid. ";
        }
        
        if (labellingSensorRecorded && !labellingSensorValuesCorrect) {
            errorString += "LabellingSensor: values are invalid. ";
        }
        
        if (locationSensorRecorded && !locationSensorValuesCorrect) {
            errorString += "LocationSensor: values are invalid. ";
        }
        
        if (StringUtils.isEmpty(errorString)) {
            DataAccess.updateLabelledFile(fileName, 3, errorString);
            return false;
        } else {
            DataAccess.updateLabelledFile(fileName, 2, "Check passed successfully");
            return true;
        }
    }
    
    private void check4SensorsRecorded(List<CSVRecord> csvRecords) {
        for (CSVRecord record : csvRecords) {
            if (record.size() < 7) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_APP)) {
                appSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_INTERACTION)) {
                interactionSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_CONNECTIVITY)) {
                connectivitySensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_DEVICE_PROTECTION)) {
                deviceProtectionSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_AWARENESS)) {
                awarenessSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_SETTINGS)) {
                settingsSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_LABELLING)) {
                labellingSensorRecorded = true;
            } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_LOCATION)) {
                locationSensorRecorded = true;
            }
        } 
    }
    
    public static String getDeviceIdFromName(String fileName, int startIndex) {
        return StringUtils.substring(fileName, startIndex, startIndex+17);
    }
    
}
