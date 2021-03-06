package server;

import org.springframework.web.multipart.MultipartFile;
import server.database.DataAccess;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Chakajkla on 5/11/17.
 */
public class RequestControllerUtil {


    public static <T> T[] convertToArray(List<T> list) {

        T[] array = (T[]) Array.newInstance(list.getClass(), list.size());

        for (int i = 0; i < list.size(); i++) {
            array[i] = (T) list.get(i);
        }

        return array;

    }

    public static void updateMissingPackages(String filePath, String fileName) {

        Set<String> packageIds = getPackageIds(filePath, fileName);
        if (packageIds != null && !packageIds.isEmpty()) {
            for (String packageId : packageIds) {
                if (!FeatureProcessor.containPackageIDOffline(packageId)) {
                    FeatureProcessor.appendPackageID(packageId);
                }
            }
        }
    }

    private static Set<String> getPackageIds(String filePath, String fileName) {

        File file = new File(filePath + fileName);

        if (!file.exists()) {
            return null;
        }

        HashSet<String> packageIds = new HashSet<>();

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {

            stream.forEach(elem -> {
                if (elem.contains("CONTEXT_SENSOR_INTERACTION") && elem.contains("package_name")) {
                    String[] sp = elem.split(";");
                    packageIds.add(sp[6]);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return packageIds;
    }

    public static void updateUserLabellingCount(String filePath, String fileName) {

        int numberOfLabellings = countLabels(filePath, fileName);
        if (numberOfLabellings > 0) {
            // Extract deviceId
            String deviceId = DataQualityProcessor.getDeviceIdFromName(fileName);
            DataAccess.updateLabellingCount(deviceId, numberOfLabellings);
        }

    }

    public static void updateInvalidUser(String fileName) {

            // Extract deviceId
            String deviceId = DataQualityProcessor.getDeviceIdFromName(fileName);
            DataAccess.updateInvalidUser(deviceId);

    }

    private static int countLabels(String filePath, String fileName) {

        File file = new File(filePath + fileName);

        if (!file.exists()) {
            return 0;
        }

        ArrayList<String> labelledLines = new ArrayList<>();

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {

            stream.forEach(elem -> {
                if (elem.contains("CONTEXT_SENSOR_LABELLING") && elem.contains(";usage;") && !elem.contains(";UNKNOWN")) {
                    labelledLines.add(elem);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return labelledLines.size();

    }

    private static String extractFileString(MultipartFile file) {
        try {
            InputStream fileInputStream = file.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(fileInputStream, writer, "UTF-8");
            String fileString = writer.toString();
            writer.close();
            fileInputStream.close();

            return fileString;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void insertNewUserIntoDb(MultipartFile file, String fileName, String osVersion, String sdkVersion, String phoneModel, String realDeviceId) {
        // Extract number of apps
        String installedAppsString = extractFileString(file);
        String[] installedApps = StringUtils.split(installedAppsString, ';');
        int numberOfApps = installedApps.length;

        // Extract deviceId
        String deviceId = DataQualityProcessor.getDeviceIdFromName(fileName);

        DataAccess.insertNewUser(deviceId, numberOfApps, osVersion, sdkVersion, phoneModel, realDeviceId);
    }

//    public static void main(String[] args){
//
//        List<Float> list = new ArrayList<>();
//
//        list.add(1.45f);
//        list.add(2.34f);
//        list.add(3.45f);
//
//        LogUtil.log(Arrays.toString(convertToArray(list)));
//
//    }

    public static String getDirectory(String filePath, String deviceId) {

        File dir = new File(filePath + deviceId);
        if (!dir.exists()) {
            dir.mkdir();
        }

        if (dir.exists() && dir.isDirectory()) {
            return filePath + deviceId + "/";
        }

        return filePath;

    }

}
