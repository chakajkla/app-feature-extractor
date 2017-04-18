package server;

import org.springframework.web.bind.annotation.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;
import server.objects.AppFeatureDescriptor;
import server.objects.AppFeatureDataPoint;

@RestController
public class RequestController {

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public
    @ResponseBody
    String handleFileUpload(
            @RequestParam("file") MultipartFile file) {
        String name = "test11";
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream =
                        new BufferedOutputStream(new FileOutputStream(new File("/home/vmadmin/"+name + "-uploaded")));
                stream.write(bytes);
                stream.close();
                return "You successfully uploaded " + name + " into " + name + "-uploaded !";
            } catch (Exception e) {
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
}
