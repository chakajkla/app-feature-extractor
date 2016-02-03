package server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

import server.objects.AppFeatureDescriptor;
import server.objects.AppFeatureDataPoint;

@RestController
public class RequestController {



    @RequestMapping("/feature")
    public Response greeting(@RequestParam(value="packageid") String packageID) {

	    packageID = packageID.trim();
	
        System.out.println("Searching features for..." + packageID);
	
		AppFeatureDescriptor featurelist = FeatureProcessor.getAppFeatures(packageID);
		
		if(featurelist == null){
		    return new Response(packageID);
		}
		
		if(featurelist.getFunctionList().isEmpty()){
		     return new Response(packageID);
		}

		Response res = new Response(packageID);
		
		Set<Feature> ft = new HashSet<Feature>();
		for (int i = 0; i < featurelist.getFunctionList().size(); i++) {
			AppFeatureDataPoint dp = featurelist.getFunctionList().get(i);
			Feature f = new Feature(dp.getFeature(),dp.getScore());		
			ft.add(f);
		}
		res.setFeatures(ft);
		
        return res;
    }
}
