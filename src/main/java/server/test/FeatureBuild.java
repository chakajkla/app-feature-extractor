package server.test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import server.FeatureProcessor;
import server.nlp.FeatureParser;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;

public class FeatureBuild {

//    public static void main(String[] args) {
//
//
//       //AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.google.android.gm", "Gmail is an easy to use email app that saves you time and keeps your messages safe. Get your messages instantly via push notifications, read and respond online & offline, and find any message quickly. With the Gmail app you get: • An organized inbox - Social and promotional messages are sorted into categories so you can read messages from friends and family first. • Less spam - Gmail blocks spam before it hits your inbox to keep your account safe and clutter free. • 15GB of free storage - You won’t need to delete messages to save space. • Multiple account support - Use both Gmail and non-Gmail addresses (Outlook.com, Yahoo Mail, or any other IMAP/POP email) right from the app.\n");
//
//        AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.android.chrome", "Browse fast on your Android phone and tablet with the Google Chrome browser you love on desktop. Pick up where you left off on your other devices with tab sync, search by voice, and save up to 50% of data usage while browsing. • Sync Across Devices - seamlessly access and open tabs and bookmarks from your laptop, phone or tablet • Save Data - reduce mobile data usage by up to 50% while browsing • Faster Browsing - choose from search results that instantly appear as you type and quickly access previously visited pages • Voice Search - use the magic of Google voice search to find answers on-the-go without typing • Translate - easily read webpages in any language • Intuitive Gestures - open as many tabs as your heart desires and quickly flip through them by swiping from side to side on the toolbar • Privacy - use Incognito mode to browse without saving your history");
//
//
//        ap = FeatureParser.applyScoreFilter(ap);
//
//        for (AppFeatureDataPoint fe : ap.getFunctionList()) {
//            System.out.println(fe.toString() + " " + fe.getScore());
//        }
//
//    }


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
