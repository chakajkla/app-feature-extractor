/**
 * 
 */
package server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import server.database.DataAccess;
import server.objects.User;

/**
 * @author niessen
 *
 */
@Component
public class ScheduledTasks
{
    // Every day at 1am
    @Scheduled(cron="0 0 1 * * *")
    public void checkUsedApps4AllUsers() {
        DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("yyyy_MM_dd");
        DateTime lastDay = new DateTime().minusDays(1);
        List<User> userList = DataAccess.getAllUsers();
        for (User user : userList) {
            String assignedApps = DataAccess.getAppsFromAppGroupsWithId(user.getAppGroup());
            List<String> assignedAppPackages = new ArrayList<String>();
            if (!StringUtils.isEmpty(assignedApps)) {
                assignedAppPackages = Arrays.asList(StringUtils.split(assignedApps, ';'));
            } else {
                continue;
            }
            File[] files = new File("/home/vmadmin/data_storage/labeled_data/").listFiles();
            for (File file : files) {
                if (assignedAppPackages.isEmpty()) {
                    break;
                }
                String fileName = file.getName();
                if (StringUtils.startsWith(fileName, "labeled_data_" + user.getDeviceId() + "_" + dateFormatter.print(lastDay))) {
                    CSVParser csvFileParser;
                    try
                    {
                        csvFileParser = CSVFormat.DEFAULT.withDelimiter(';').withHeader(DataQualityProcessor.FILE_HEADER_MAPPING).withRecordSeparator('\n').parse(new FileReader(file));
                        //Get a list of CSV file records
                        List<CSVRecord> csvRecords = csvFileParser.getRecords();
                        
                        for (CSVRecord record : csvRecords) {
                            try {
                                if ((StringUtils.equals(record.get("context_event_type"), DataQualityProcessor.SENSOR_TYPE_LABELLING)
                                        && StringUtils.equals(record.get("property_key"), "packageName"))
                                    || (StringUtils.equals(record.get("context_event_type"), DataQualityProcessor.SENSOR_TYPE_INTERACTION)
                                                && StringUtils.equals(record.get("property_key"), "package_name"))) {
                                    String packageName = record.get("property_value");
                                    if (assignedAppPackages.contains(packageName)) {
                                        assignedAppPackages.remove(assignedAppPackages.indexOf(packageName));
                                    }
                                }
                            } catch(IllegalArgumentException e) {
                                // Ignore
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    
                }
            }
            if (!assignedAppPackages.isEmpty()) {
                String notUsedApps = StringUtils.join(assignedAppPackages, ';');
                DataAccess.updateNotUsedApps(user.getDeviceId(), notUsedApps);
            } else {
                DataAccess.updateNotUsedApps(user.getDeviceId(), "");
            }
        }
    }

}
