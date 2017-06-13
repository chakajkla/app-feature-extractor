package server;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

import org.springframework.web.multipart.MultipartFile;

import server.DataQualityProcessor;
import server.database.DataAccess;
import server.objects.AppFeatureDescriptor;
import server.objects.AppFeatureDataPoint;
import server.objects.request.App;
import server.objects.response.Feature;
import server.objects.response.Response;

@RestController
public class RequestController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public
    @ResponseBody
    String handleFileUpload(
            @RequestParam("file") MultipartFile file) {
        String name = file.getOriginalFilename();
        String filePath = "/home/vmadmin/data_storage/"; //default for labelled data
        if (name.contains("installed_apps")) {
            filePath = "/home/vmadmin/data_storage/packages/";
        } else if (name.contains("manual_features")) {
            filePath = "/home/vmadmin/data_storage/manual_features/";
        } else if (name.contains("error")) {
            filePath = "/home/vmadmin/data_storage/errors/";
        }

        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File(filePath + name)));
                stream.write(bytes);
                stream.close();
                System.out.println("You successfully uploaded " + name + " into " + name + "-uploaded !");
                
                // Inserting new user into db
                if (name.contains("installed_apps")) {
                    insertNewUserIntoDb(file, name);
                }
                // Data quality check for labeled data
                else if (name.contains("labeled_data")) {
                    DataAccess.insertNewLabelledFile(name, DataQualityProcessor.getDeviceIdFromName(name, 13), "not yet checked");
                    new DataQualityProcessor(filePath, name).checkLabeledDataFile();
                }
                
                return "You successfully uploaded " + name + " into " + name + "-uploaded !";
            } catch (Exception e) {
                e.printStackTrace();
                return "You failed to upload " + name + " => " + e.getMessage();
            }
        } else {
            return "You failed to upload " + name + " because the file was empty.";
        }
    }

    @RequestMapping("/feature")
    public Response greeting(@RequestParam(value = "packageid") String packageID) {

        packageID = packageID.trim();

        System.out.println("Searching features for..." + packageID);

        AppFeatureDescriptor featurelist = FeatureProcessor.getAppFeatures(packageID);

        if (featurelist == null) {
            return new Response(packageID);
        }

        if (featurelist.getFunctionList().isEmpty()) {
            return new Response(packageID);
        }

        Response res = new Response(packageID);

        Set<Feature> ft = new HashSet<Feature>();
        for (int i = 0; i < featurelist.getFunctionList().size(); i++) {
            AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
            Feature f = new Feature(dp.getFeature(), dp.getScore());
            ft.add(f);
        }
        res.setFeatures(ft);

        return res;
    }
    
    @RequestMapping("/notUsedApps")
    public String notUsedApps(@RequestParam(value = "deviceid") String deviceId) {

        deviceId = deviceId.trim();
           
        String notUsedApps = DataAccess.getNotUsedAppsWithDeviceId(deviceId);

        return notUsedApps;
    }
    
    @RequestMapping("/appStage")
    public String appStage(@RequestParam(value = "deviceid") String deviceId) {

        deviceId = deviceId.trim();
           
        boolean secondStage = DataAccess.getStageWithDeviceId(deviceId);
        
        String secondStageAsString = Boolean.toString(secondStage);

        return secondStageAsString;
    }
    
    @RequestMapping("/assignedApps")
    public String assignedApps(@RequestParam(value = "deviceid") String deviceId) {

        deviceId = deviceId.trim();
           
        String assignedApps = DataAccess.getAssignedAppsWithDeviceId(deviceId);

        return assignedApps;
    }
    
    @RequestMapping("/assignedAppNames")
    public String assignedAppNames(@RequestParam(value = "deviceid") String deviceId) {

        deviceId = deviceId.trim();
           
        String assignedApps = DataAccess.getAssignedAppNamesWithDeviceId(deviceId);

        return assignedApps;
    }

    @RequestMapping(value = "/ranking", method = RequestMethod.POST)
    public ResponseEntity<Response> rankFeature(@RequestBody App app) {

        if(app == null){
            return new ResponseEntity<>(new Response(null), HttpStatus.BAD_REQUEST);
        }

        String packageID = app.getId().trim();

        System.out.println("Ranking features for..." + packageID + " " + app.getFeatureVector().toString());

//        Map<String, Float> rankedMap = TensorflowUtil.getFeaturesScore(packageID,  RequestControllerUtil.convertToArray(app.getFeatureVector()));
//
//        AppFeatureDescriptor featurelist = FeatureProcessor.getAppFeatures(packageID);
//
//        if (featurelist == null) {
//            return new ResponseEntity<>(new Response(packageID), HttpStatus.OK);
//        }
//
//        if (featurelist.getFunctionList().isEmpty()) {
//            return new ResponseEntity<>(new Response(packageID), HttpStatus.OK);
//        }

        Response res = new Response(packageID);

//        Set<Feature> ft = new HashSet<>();
//        for (int i = 0; i < featurelist.getFunctionList().size(); i++) {
//            AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
//            Feature f = new Feature(dp.getFeature(), rankedMap.get(dp.getFeature()));
//            ft.add(f);
//        }
//        res.setFeatures(ft);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

//    @RequestMapping(value = "/tmp")
//    public ResponseEntity<App> get() {
//
//        App car = new App("testID");
//
//        List<Float> data = new ArrayList<>();
//        data.add(1.45f);
//        data.add(2.34f);
//        data.add(3.45f);
//
//        car.setFeatureVector(data);
//
//        return new ResponseEntity<App>(car, HttpStatus.OK);
//    }
    
    private void insertNewUserIntoDb(MultipartFile file, String fileName) {
        // Extract number of apps
        String installedAppsString = extractFileString(file);
        String[] installedApps = StringUtils.split(installedAppsString, ';');
        int numberOfApps = installedApps.length;
        
        // Extract deviceId
        String deviceId = DataQualityProcessor.getDeviceIdFromName(fileName, 15);
        
        DataAccess.insertNewUser(deviceId, numberOfApps);
    }
    
    private String extractFileString(MultipartFile file) {
        try
        {
            InputStream fileInputStream = file.getInputStream();
            StringWriter writer = new StringWriter();
            IOUtils.copy(fileInputStream, writer, "UTF-8");
            String fileString = writer.toString();
            writer.close();
            fileInputStream.close();
            
            return fileString;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           return null;
    }

}
