/**
 * 
 */
package server;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import server.database.DataAccess;
import server.log.LogUtil;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
    public static final String SENSOR_TYPE_DEVICE = "CONTEXT_SENSOR_DEVICE";
    
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
    private boolean genericSensorRecorded = false;
    private boolean packageSensorRecorded = false;
    private boolean notificationSensorRecorded = false;
    private boolean deviceSensorRecorded = false;
    
    private boolean awarenessSensorValuesCorrect = false;
    private boolean connectivitySensorValuesCorrect = false;
    private boolean deviceProtectionSensorValuesCorrect = false;
    private boolean settingsSensorValuesCorrect = false;
    private boolean labellingSensorValuesCorrect = false;
    private boolean locationSensorValuesCorrect = false;
    private boolean appSensorValuesCorrect = false;
    private boolean interactionSensorValuesCorrect = false;
    
    public DataQualityProcessor(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public boolean checkLabeledDataFile() {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        boolean errorOccured = false;
        
        
        try {
            DataAccess.updateLabelledFile(fileName, 1, "Currently checked", false, "");
            
            fileReader = new FileReader(filePath + fileName);
            csvFileParser = CSVFormat.DEFAULT.withDelimiter(';').withHeader(FILE_HEADER_MAPPING).withSkipHeaderRecord().withRecordSeparator('\n').withQuote(null).parse(fileReader);
            
            //Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            LogUtil.log("First record:" + csvRecords.get(0).toString());
            
            // Check all default sensors are recorded
            check4SensorsRecorded(csvRecords);
            if(interactionSensorRecorded){
                interactionSensorValuesCorrect = checkInteractionSensorValues(csvRecords);
            }
            if(appSensorRecorded){
                appSensorValuesCorrect = checkAppSensorValues(csvRecords);
            }
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
            errorOccured = true;
            LogUtil.log("Error in CsvFileReader !!!");
            e.printStackTrace();
            DataAccess.updateLabelledFile(fileName, 3, "Error while reading file", false, "");
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
                if (csvFileParser != null) {
                    csvFileParser.close();
                }
            } catch (IOException e) {
                LogUtil.log("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }
        
        if (errorOccured) {
            return false;
        }
        return checkResults();
    }
    
    private boolean checkAwarenessSensorValues(List<CSVRecord> csvRecords) {
        int contextEventId = 0;
        try {
            for (CSVRecord record : csvRecords) {
                if (!record.isConsistent()) {
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

    private boolean checkInteractionSensorValues(List<CSVRecord> csvRecords) {
        boolean interactionSensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (!record.isConsistent()) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_INTERACTION)) {
                switch(record.get("property_key")) {
                    case "package_name":
                        String packageName = StringUtils.trimToEmpty(record.get("property_value"));
                        if (!StringUtils.equalsIgnoreCase(packageName, "null") && StringUtils.isNotEmpty(packageName)) {
                            interactionSensorValuesCorrect = true;
                        }
                        else{
                            interactionSensorValuesCorrect = false;
                        }
                        break;
                    case "app_name":
                        String appName = StringUtils.trimToEmpty(record.get("property_value"));
                        if (!StringUtils.equalsIgnoreCase(appName, "null") && StringUtils.isNotEmpty(appName)) {
                            interactionSensorValuesCorrect = true;
                        }
                        else{
                            interactionSensorValuesCorrect = false;
                        }
                        break;
                }
            }
        }
        return interactionSensorValuesCorrect;
    }

    private boolean checkAppSensorValues(List<CSVRecord> csvRecords) {
        boolean appSensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (!record.isConsistent()) {
                continue;
            }
            if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_APP)) {
                switch(record.get("property_key")) {
                    case "packagename":
                        String packageName = StringUtils.trimToEmpty(record.get("property_value"));
                        if (!StringUtils.equalsIgnoreCase(packageName, "null") && StringUtils.isNotEmpty(packageName)) {
                            appSensorValuesCorrect = true;
                        }
                        else{
                            appSensorValuesCorrect = false;
                        }
                        break;
                    case "appname":
                        String appName = StringUtils.trimToEmpty(record.get("property_value"));
                        if (!StringUtils.equalsIgnoreCase(appName, "null") && StringUtils.isNotEmpty(appName)) {
                            appSensorValuesCorrect = true;
                        }
                        else{
                            appSensorValuesCorrect = false;
                        }
                        break;
                }
            }
        }
        return appSensorValuesCorrect;
    }
    
    private boolean checkConnectivitySensorValues(List<CSVRecord> csvRecords) {
        boolean connectivitySensorValuesCorrect = false;
        for (CSVRecord record : csvRecords) {
            if (!record.isConsistent()) {
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
            if (!record.isConsistent()) {
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
                if (!record.isConsistent()) {
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
            if (!record.isConsistent()) {
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
                if (!record.isConsistent()) {
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
        String errorString1 = "";
        String errorString2 = "";
        
        StringBuilder builder = new StringBuilder();
        
        if (!appSensorRecorded) {
            builder.append("AppSensor, ");
        }
        if (!interactionSensorRecorded) {
            builder.append("InteractionSensor, ");
        }
        if (!connectivitySensorRecorded) {
            builder.append("ConnectivitySensor, ");
        }
        if (!deviceProtectionSensorRecorded) {
            builder.append("DeviceProtectionSensor, ");
        }
        if (!awarenessSensorRecorded) {
            builder.append("AwarenessSensor, ");
        }
        if (!settingsSensorRecorded) {
            builder.append("SettingsSensor, ");
        }
        if (!genericSensorRecorded) {
            builder.append("GenericSensor, ");
        }
        if (!deviceSensorRecorded) {
            builder.append("DeviceSensor, ");
        }
        if (!locationSensorRecorded) {
            builder.append("LocationSensor, ");
        }
        if (!notificationSensorRecorded) {
            builder.append("NotificationSensor, ");
        }
        if (!packageSensorRecorded) {
            builder.append("PackageSensor, ");
        }
        
        String missingSensors = builder.toString();
        errorString2 += StringUtils.removeEnd(missingSensors, ",") + "not recorded. ";
        
        
        if (!awarenessSensorValuesCorrect) {
            errorString1 += "AwarenessSensor: values are invalid. ";
        }
        
        if (!connectivitySensorValuesCorrect) {
            errorString1 += "ConnectivitySensor: values are invalid. ";
        }
        
        if (!deviceProtectionSensorValuesCorrect) {
            errorString1 += "DeviceProtectionSensor: values are invalid. ";
        }
        
        if (settingsSensorRecorded && !settingsSensorValuesCorrect) {
            errorString1 += "SettingsSensor: values are invalid. ";
        }
        
        if (labellingSensorRecorded && !labellingSensorValuesCorrect) {
            errorString1 += "LabellingSensor: values are invalid. ";
        } else if (labellingSensorRecorded && labellingSensorValuesCorrect && (!interactionSensorRecorded || !appSensorRecorded)) {
            errorString1 += "LabellingSensor: There is no interaction or app sensor data. ";
        }
        
        if (locationSensorRecorded && !locationSensorValuesCorrect) {
            errorString1 += "LocationSensor: values are invalid. ";
        }

        if (appSensorRecorded && !appSensorValuesCorrect) {
            errorString1 += "AppSensor: values are invalid. ";
        }

        if (interactionSensorRecorded && !interactionSensorValuesCorrect) {
            errorString1 += "InteractionSensor: values are invalid. ";
        }
        
        if (StringUtils.isEmpty(errorString1)) {
            DataAccess.updateLabelledFile(fileName, 3, errorString1, StringUtils.isEmpty(missingSensors), !StringUtils.isEmpty(missingSensors) ? errorString2 : "");
            return false;
        } else {
            DataAccess.updateLabelledFile(fileName, 2, "Check passed successfully", StringUtils.isEmpty(missingSensors), !StringUtils.isEmpty(missingSensors) ? errorString2 : "");
            return true;
        }
    }
    
    private void check4SensorsRecorded(List<CSVRecord> csvRecords) {
        for (CSVRecord record : csvRecords) {
//            if (!record.isConsistent()) {
//                continue;
//            }
            try {
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
                } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_DEVICE)) {
                    deviceSensorRecorded = true;
                } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_GENERIC)) {
                    genericSensorRecorded = true;
                } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_NOTIFICATION)) {
                    notificationSensorRecorded = true;
                } else if (StringUtils.equals(record.get("context_event_type"), SENSOR_TYPE_PACKAGE)) {
                    packageSensorRecorded = true;
                }
            } catch(IllegalArgumentException e) {
                continue;
            }
        } 
    }
    
    public static String getDeviceIdFromName(String fileName) {

        //error_e2ca25038246da18
        //labeled_data_eacf84949b4dcaf2
        //installed_apps_eacf84949b4dcaf2

        String[] sp = fileName.split("_");
        if(sp.length >= 3) {

            if(fileName.startsWith("error") || fileName.startsWith("invalid")){
                return sp[1];
            }

            return sp[2];
        }

        return null;
    }
    
}
