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
    private static final String [] FILE_HEADER_MAPPING = {"session_id","context_event_id","context_event_type","timestamp","property_key","property_value"};
    private static final String SENSOR_TYPE_APP = "CONTEXT_SENSOR_APP";
    private static final String SENSOR_TYPE_CONNECTIVITY = "CONTEXT_SENSOR_CONNECTIVITY";
    private static final String SENSOR_TYPE_SETTINGS = "CONTEXT_SENSOR_SETTINGS";
    private static final String SENSOR_TYPE_PACKAGE = "CONTEXT_SENSOR_PACKAGE";
    private static final String SENSOR_TYPE_DEVICE_PROTECTION = "CONTEXT_SENSOR_DEVICE_PROTECTION";
    private static final String SENSOR_TYPE_LOCATION = "CONTEXT_SENSOR_LOCATION";
    private static final String SENSOR_TYPE_GENERIC = "CONTEXT_SENSOR_GENERIC";
    private static final String SENSOR_TYPE_AWARENESS = "CONTEXT_SENSOR_AWARENESS";
    private static final String SENSOR_TYPE_NOTIFICATION = "CONTEXT_SENSOR_NOTIFICATION";
    private static final String SENSOR_TYPE_INTERACTION = "CONTEXT_SENSOR_INTERACTION";
    private static final String SENSOR_TYPE_LABELLING = "CONTEXT_SENSOR_LABELLING";
    
    private String filePath;
    private String fileName;
    
    public DataQualityProcessor(String filePath, String fileName) {
        this.filePath = filePath;
        this.fileName = fileName;
    }

    public boolean checkLabeledDataFile() {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        boolean appSensorRecorded = false;
        boolean interactionSensorRecorded = false;
        boolean connectivitySensorRecorded = false;
        boolean deviceProtectionSensorRecorded = false;
        boolean awarenessSensorRecorded = false;
        
        boolean allDefaultSensorsRecorded = false;
        
        
        try {
            String userId = getDeviceIdFromName(fileName, 13);
            //DataAccess.updateLabelledFile(fileName, 1, "Currently checked");
            
            fileReader = new FileReader(filePath + fileName);
            
            csvFileParser = CSVFormat.DEFAULT.withDelimiter(';').withHeader(FILE_HEADER_MAPPING).withRecordSeparator('\n').parse(fileReader);
            
            //Get a list of CSV file records
            List<CSVRecord> csvRecords = csvFileParser.getRecords();
            
            // Check all default sensors are recorded
            for (CSVRecord record : csvRecords) {
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
                }
            }
        } catch(Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();
            //DataAccess.updateLabelledFile(fileName, 3, "Error while reading file");
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
        
        if (appSensorRecorded && interactionSensorRecorded && connectivitySensorRecorded && deviceProtectionSensorRecorded && awarenessSensorRecorded) {
            allDefaultSensorsRecorded = true;
        } else {
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
            
            String errorString = StringUtils.removeEnd(builder.toString(), ",") + "not recorded";
            //DataAccess.updateLabelledFile(fileName, 3, errorString);
            
            return false;
        }
        
        if (allDefaultSensorsRecorded) {
            //DataAccess.updateLabelledFile(fileName, 2, "Check passed successfully");
            return true;
        }
        
        return false;
    }
    
    public static String getDeviceIdFromName(String fileName, int startIndex) {
        return StringUtils.substring(fileName, startIndex, startIndex+17);
    }
    
}
