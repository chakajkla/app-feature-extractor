package server.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import server.log.LogUtil;
import server.objects.AndroidApp;

import java.io.IOException;

/**
 * Created by christophstanik on 6/23/14.
 */
public class PlayStoreAppPageCrawler {
	private static final String URL_PREFIX = "https://play.google.com/store/apps/details?id=";
	private static final String URL_SUFFIX_LANG_EN = "&hl=en";

	public static AndroidApp crawlAppPages(String packageName) {
		Document doc = null;

		AndroidApp app = null;
		try {
			doc = Jsoup.connect(URL_PREFIX + packageName + URL_SUFFIX_LANG_EN)
					.get();

			app = new AndroidApp();

			String description = doc.getElementsByAttributeValueContaining(
					"itemprop", "description").text();
			app.setDescription(description);

			String appName = doc.getElementsByClass("id-app-title").text();
			app.setName(appName);

			app.setPackageName(packageName);

			String starsCount = doc.select("div.stars-count").text()
					.replace('(', ' ').replace(')', ' ').trim();
			app.setStarsCount(starsCount);

			String rating = doc.getElementsByClass("score").text();
			app.setRating(rating);

			String dlNumber = doc.getElementsByAttributeValueContaining(
					"itemprop", "numDownloads").text();
			app.setDownloadNumber(dlNumber);

			String category = doc.getElementsByAttributeValueContaining(
					"itemprop", "genre").text();
			app.setCategory(category);

			String categoryLink = doc.getElementsByClass("category").attr(
					"href");
			app.setCategoryLink("https://play.google.com" + categoryLink);

			String lastUpdateDate = doc.getElementsByAttributeValueContaining(
					"itemprop", "datePublished").text();
			app.setLastUpdateDate(lastUpdateDate);

			String developer = doc
					.getElementsByAttributeValueContaining("itemprop", "author")
					.text().split(" ")[0];
			app.setDeveloper(developer);

			String hasInAppPurchase = doc.select("div.inapp-msg").text();
			app.setHasInAppPurchase(String.valueOf(hasInAppPurchase.length() > 0));

			String price = doc
					.getElementsByAttributeValueContaining("itemprop", "price")
					.select("meta").attr("content");
			if (price.equalsIgnoreCase("0") || price.equalsIgnoreCase("")) {
				price = "free";
			}
			app.setPrice(price);

			if (app.getSearchQuery() == null || app.getSearchQuery().equals("")) {
				app.setSearchQuery("toplist");
			}

		} catch (IOException e) {
			// e.printStackTrace();
		}

		return app;
	}

	public static void debug(AndroidApp app) {
		LogUtil.log("------");
		LogUtil.log("name:" + app.getName());
		LogUtil.log("packageName:" + app.getPackageName());
		LogUtil.log("category:" + app.getCategory());
		LogUtil.log("price:" + app.getPrice());
		LogUtil.log("description:" + app.getDescription());
		LogUtil.log("starsCount:" + app.getStarsCount());
		LogUtil.log("numDownloads:" + app.getDownloadNumber());
		LogUtil.log("lastUpdatedDate:" + app.getLastUpdateDate());
		LogUtil.log("developer:" + app.getDeveloper());
		LogUtil.log("hasInAppPurchase:" + app.getHasInAppPurchase());
		LogUtil.log("categoryLink:" + app.getCategoryLink());
		LogUtil.log("------");
	}
}