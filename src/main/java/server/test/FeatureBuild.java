package server.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import server.FeatureProcessor;

public class FeatureBuild {

	public static void buildTopAppFeatures() {

		java.nio.file.Path pa = Paths
				.get("D:\\data\\phd_thesis\\data\\appdata\\android_links\\ids.txt");

		try {
			List<String> ids = Files.readAllLines(pa, Charset.defaultCharset());

			int ct = 1;
			for (String id : ids) {
				System.out.println("building features for " + id + " " + ct
						+ "/" + ids.size());
				FeatureProcessor.getAppFeatures(id);
				ct++;

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
