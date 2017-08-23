package server.test;

import server.FeatureProcessor;
import server.log.LogUtil;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static server.nlp.FeatureParser.applyScoreNormalization;

public class FeatureBuild {

    public static void main2(String[] args) {


       AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.google.android.gm", "You won’t need to delete messages to save space. Gmail is an easy to use email app that saves you time and keeps your messages safe. Get your messages instantly via push notifications, read and respond online & offline, and find any message quickly. With the Gmail app you get: • An organized inbox - Social and promotional messages are sorted into categories so you can read messages from friends and family first. • Less spam - Gmail blocks spam before it hits your inbox to keep your account safe and clutter free. • 15GB of free storage - You won’t need to delete messages to save space. • Multiple account support - Use both Gmail and non-Gmail addresses (Outlook.com, Yahoo Mail, or any other IMAP/POP email) right from the app.\n");

        //AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.android.chrome", "Browse fast on your Android phone and tablet with the Google Chrome browser you love on desktop. Pick up where you left off on your other devices with tab sync, search by voice, and save up to 50% of data usage while browsing. • Sync Across Devices - seamlessly access and open tabs and bookmarks from your laptop, phone or tablet • Save Data - reduce mobile data usage by up to 50% while browsing • Faster Browsing - choose from search results that instantly appear as you type and quickly access previously visited pages • Voice Search - use the magic of Google voice search to find answers on-the-go without typing • Translate - easily read webpages in any language • Intuitive Gestures - open as many tabs as your heart desires and quickly flip through them by swiping from side to side on the toolbar • Privacy - use Incognito mode to browse without saving your history");


        //AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.facebook.katana", "Keeping up with friends is faster than ever. • See what friends are up to • Share updates, photos and videos • Get notified when friends like and comment on your posts • Play games and use your favorite apps Now you can get early access to the next version of Facebook for Android by becoming a beta tester. Learn how to sign up, give feedback and leave the program in our Help Center: http://on.fb.me/133NwuP Sign up directly here: http://play.google.com/apps/testing/com.facebook.katana Problems downloading or installing the app? See http://bit.ly/GPDownload1 Still need help? Please tell us more about the issue. http://bit.ly/invalidpackage Facebook is only available for users age 13 and over. Terms of Service: http://m.facebook.com/terms.php.");


        //AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.whatsapp", "WhatsApp Messenger is a FREE messaging app available for Android and other smartphones. WhatsApp uses your phone's Internet connection (4G/3G/2G/EDGE or Wi-Fi, as available) to let you message and call friends and family. Switch from SMS to WhatsApp to send and receive messages, calls, photos, videos, documents, and Voice Messages. WHY USE WHATSAPP: • NO FEES: WhatsApp uses your phone's Internet connection (4G/3G/2G/EDGE or Wi-Fi, as available) to let you message and call friends and family, so you don't have to pay for every message or call.* There are no subscription fees to use WhatsApp. • MULTIMEDIA: Send and receive photos, videos, documents, and Voice Messages. • FREE CALLS: Call your friends and family for free with WhatsApp Calling, even if they're in another country.* WhatsApp calls use your phone's Internet connection rather than your cellular plan's voice minutes. (Note: Data charges may apply. Contact your provider for details. Also, you can't access 911 and other emergency service numbers through WhatsApp). • GROUP CHAT: Enjoy group chats with your contacts so you can easily stay in touch with your friends or family. • WHATSAPP WEB: You can also send and receive WhatsApp messages right from your computer's browser. • NO INTERNATIONAL CHARGES: There's no extra charge to send WhatsApp messages internationally. Chat with your friends around the world and avoid international SMS charges.* • SAY NO TO USERNAMES AND PINS: Why bother having to remember yet another username or PIN? WhatsApp works with your phone number, just like SMS, and integrates seamlessly with your phone's existing address book. • ALWAYS LOGGED IN: With WhatsApp, you're always logged in so you don't miss messages. No more confusion about whether you're logged in or logged out. • QUICKLY CONNECT WITH YOUR CONTACTS: Your address book is used to quickly and easily connect you with your contacts who have WhatsApp so there's no need to add hard-to-remember usernames. • OFFLINE MESSAGES: Even if you miss your notifications or turn off your phone, WhatsApp will save your recent messages until the next time you use the app. • AND MUCH MORE: Share your location, exchange contacts, set custom wallpapers and notification sounds, email chat history, broadcast messages to multiple contacts at once, and more! *Data charges may apply. Contact your provider for details. --------------------------------------------------------- We're always excited to hear from you! If you have any feedback, questions, or concerns, please email us at: android-support@whatsapp.com or follow us on twitter: http://twitter.com/WhatsApp @WhatsApp ---------------------------------------------------------");
        //AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.whatsapp", "WhatsApp uses your phone's Internet connection (4G/3G/2G/EDGE or Wi-Fi, as available) to let you message and call friends and family, so you don't have to pay for every message or call.");

       // AppFeatureDescriptor ap = FeatureProcessor.extractFeatures("com.netflix.mediaclient", "Netflix is the world’s leading subscription service for watching TV episodes and movies on your phone. This Netflix mobile application delivers the best experience anywhere");

        //need a way to reduce the features


        Comparator<AppFeatureDataPoint> comp = new Comparator<AppFeatureDataPoint>() {
            @Override
            public int compare(AppFeatureDataPoint o1, AppFeatureDataPoint o2) {
                if(o1.getScore() - o2.getScore() > 0) return -1;
                if(o1.getScore() - o2.getScore() < 0) return 1;

                return 0;
            }
        };

        //apply score filter
        ap = applyScoreNormalization(ap);

        ArrayList<AppFeatureDataPoint> felist = ap.getFunctionList();
        Collections.sort(felist,comp);

        for (AppFeatureDataPoint fe : felist) {
            LogUtil.log(fe.toString() + " " + fe.getScore());
        }

    }

    public static void buildTopAppFeatures() {

        java.nio.file.Path pa = Paths
                .get("D:\\data\\phd_thesis\\data\\appdata\\android_links\\ids.txt");

        try {
            List<String> ids = Files.readAllLines(pa, Charset.defaultCharset());

            int ct = 1;
            for (String id : ids) {
                LogUtil.log("building features for " + id + " " + ct
                        + "/" + ids.size());
                FeatureProcessor.getAppFeatures(id);
                ct++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
