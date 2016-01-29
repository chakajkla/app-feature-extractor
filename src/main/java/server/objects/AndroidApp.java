package server.objects;

/**
 * Created by christophstanik on 6/23/14. Extended by chakajklajes on 21/012016
 */
@SuppressWarnings("serial")
public class AndroidApp implements java.io.Serializable {

	String category;
	String price;
	String name;
	String packageName;
	String description;
	String starsCount;
	String downloadNumber;
	String lastUpdateDate;
	String developer;
	String hasInAppPurchase;
	String searchQuery;
	String rating;

	// links
	String categoryLink;
	String appLink;

	public String getSearchQuery() {
		return searchQuery;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public String getHasInAppPurchase() {
		return hasInAppPurchase;
	}

	public void setHasInAppPurchase(String hasInAppPurchase) {
		this.hasInAppPurchase = hasInAppPurchase;
	}

	public String getDeveloper() {
		return developer;
	}

	public void setDeveloper(String developer) {
		this.developer = developer;
	}

	public String getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(String lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getDownloadNumber() {
		return downloadNumber;
	}

	public void setDownloadNumber(String downloadNumber) {
		this.downloadNumber = downloadNumber;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStarsCount() {
		return starsCount;
	}

	public void setStarsCount(String starsCount) {
		this.starsCount = starsCount;
	}

	public String getCategoryLink() {
		return categoryLink;
	}

	public void setCategoryLink(String categoryLink) {
		this.categoryLink = categoryLink;
	}

	public String getAppLink() {
		return appLink;
	}

	public void setAppLink(String appLink) {
		this.appLink = appLink;
	}
}