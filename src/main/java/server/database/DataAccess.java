package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import server.nlp.NLPUtil;

import server.objects.AndroidApp;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;
import server.objects.PathStorage;
import server.objects.User;

public class DataAccess {

    public static boolean updateData(AndroidApp app) {

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String desc = NLPUtil.removeNonCharacters(app.getDescription());

            String sql = "INSERT INTO app_data (app_name, description) "
                    + "VALUES (\"" + app.getPackageName() + "\", \"" + desc
                    + "\" );";

            // System.out.println(sql);
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
        System.out.println("Insert Operation done successfully");

        return true;

    }
    
    public static boolean insertNewUser(String userId, int numberOfApps, String randomID) {
        Connection c = null;
        Statement stmt = null;
        String endOfStudyId = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            
            List<User> currentUsers = getAllUsers();
            boolean userExists = false;
            for (User user : currentUsers) {
                if (StringUtils.equals(user.getDeviceId(), userId)) {
                    userExists = true;
                    break;
                }
            }
            String sql;
            if (userExists) {
                sql = "UPDATE user_data"
                        + " SET number_apps = " + numberOfApps
                        +", random_id = " + randomID
                        + " WHERE device_id = \"" + userId + "\";";
            } else {
                sql = "INSERT INTO user_data (device_id, number_apps, random_id, end_of_study_id) "
                        + "VALUES (\"" + userId + "\", " + numberOfApps + ", \"" + randomID 
                        + "\", \"" + endOfStudyId + "\");";
                
            }
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.rollback();
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return false;
        }
        System.out.println("Insert Operation done successfully");

        return true;
    }
    
    public static boolean updateNotUsedApps(String userId, String notUsedApps) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            
            String sql = "UPDATE user_data"
                        + " SET lastAppCheck = CURRENT_TIMESTAMP"
                        + ", notUsedApps = \"" + notUsedApps + "\""
                        + " WHERE device_id = \"" + userId + "\";";
            
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.rollback();
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return false;
        }
        System.out.println("Update Operation done successfully");

        return true;
    }
    
    public static List<User> getAllUsers() {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        List<User> userList = new ArrayList<User>();
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select * from user_data;";
            
            result = stmt.executeQuery(sql);
            
            while (result.next()) {
                User user = new User();
                user.setDeviceId(result.getString(1));
                user.setNumberOfApps(result.getInt(2));
                user.setEmail(result.getString(3));
                user.setAppGroup(result.getInt(4));
                user.setSecondStage(result.getBoolean(7));
                user.setRandomID(result.getString(8));
                user.setEndOfStudy(result.getString(9));
                
                userList.add(user);
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        System.out.println("Select Operation done successfully");
        return userList;
    }
    
    public static boolean getStageWithDeviceId(String deviceId) {
        List<User> userList = getAllUsers();
        boolean secondStage = false;
        
        for (User user : userList) {
            if (StringUtils.equals(user.getDeviceId(), deviceId)) {
                secondStage = user.getSecondStage();
                break;
            }
        }
        
        return secondStage;
    }
    
    public static String getEndOfStudyWithDeviceId(String deviceId) {
        List<User> userList = getAllUsers();
        String endOfStudy = null;
        
        for (User user : userList) {
            if (StringUtils.equals(user.getDeviceId(), deviceId)) {
                endOfStudy = user.getEndOfStudy();
                break;
            }
        }
        
        return endOfStudy;
    }
    
    public static String getEndOfStudyIdWithDeviceId(String deviceId) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        String endOfStudyId = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select end_of_study_id from user_data where device_id = \"" + deviceId + "\";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                endOfStudyId = result.getString(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        System.out.println("Select Operation done successfully");
        return endOfStudyId;
    }
    
    public static String getNotUsedAppsWithDeviceId(String deviceId) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        String apps = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select notUsedApps from user_data where device_id = \"" + deviceId + "\";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                apps = result.getString(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        System.out.println("Select Operation done successfully");
        return apps;
    }
    
    public static String getAssignedAppsWithDeviceId(String id) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        Integer appGroup = null;
        String assignedApps = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select app_group from user_data where device_id = \"" + id + "\";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                appGroup = result.getInt(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        if (appGroup != null) {
            assignedApps = getAppsFromAppGroupsWithId(appGroup);
            System.out.println("Select Operation done successfully");
        }
        
        return assignedApps;
    }
    
    public static String getAssignedAppNamesWithDeviceId(String id) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        Integer appGroup = null;
        String assignedApps = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select app_group from user_data where device_id = \"" + id + "\";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                appGroup = result.getInt(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        if (appGroup != null) {
            assignedApps = getAppNamesFromAppGroupsWithId(appGroup);
            System.out.println("Select Operation done successfully");
        }
        
        return assignedApps;
    }
    
    public static String getAppsFromAppGroupsWithId(int id) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        String apps = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select apps from app_groups where id = " + id + ";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                apps = result.getString(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        System.out.println("Select Operation done successfully");
        return apps;
    }
    
    public static String getAppNamesFromAppGroupsWithId(int id) {
        Connection c = null;
        Statement stmt = null;
        ResultSet result = null;
        String apps = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "select app_names from app_groups where id = " + id + ";";
            
            result = stmt.executeQuery(sql);
            
            
            while (result.next()) {
                apps = result.getString(1);
                break;
            }
            result.close();
            stmt.close();
            c.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }
            return null;
        }
        
        
        System.out.println("Select Operation done successfully");
        return apps;
    }
    
    public static boolean insertNewLabelledFile(String fileName, String userId, String description) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "INSERT INTO labelled_files (file_name, file_State, user_id, state_desc) "
                    + "VALUES (\"" + fileName + "\", 0, \"" + userId + "\", \"" + description + "\");";
            
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.rollback();
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }

            return false;
        }
        System.out.println("Insert Operation done successfully");

        return true;
    }
    
    public static boolean updateLabelledFile(String fileName, int fileState, String description) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            String sql = "UPDATE labelled_files "
                    + "SET file_state = " + fileState + ", state_desc = \"" + description + "\", updated = CURRENT_TIMESTAMP "
                    + "WHERE file_name = \"" + fileName + "\";";
            
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            try
            {
                c.rollback();
                c.close();
            }
            catch (SQLException e1)
            {
                e1.printStackTrace();
            }

            return false;
        }
        System.out.println("Update Operation done successfully");

        return true;
    }

    public static AndroidApp getAppFromDatabase(String packageID) {
        Connection c = null;
        Statement stmt = null;

        AndroidApp ap = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM app_data WHERE app_name = '"
                            + packageID + "';");
            while (rs.next()) {
                String name = rs.getString("app_name");
                String description = rs.getString("description");
                ap = new AndroidApp();
                ap.setPackageName(name);
                ap.setDescription(description);

            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Select Operation done successfully");
        return ap;

    }

    public static boolean checkPackageID(String packageID) {
        Connection c = null;
        Statement stmt = null;

        int contain = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT count(*) as ct FROM app_data WHERE app_name = '"
                            + packageID + "';");
            while (rs.next()) {
                contain = rs.getInt("ct");
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Check Operation done successfully");

        if (contain == 1) {
            return true;
        }

        return false;
    }

    public static void storeFeatures(AppFeatureDescriptor featurelist) {

        if (featurelist == null || featurelist.getFunctionList() == null) {
            return;
        }

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();

            for (AppFeatureDataPoint dp : featurelist.getFunctionList()) {
                String sql = "INSERT INTO features (app_name, verb, noun, preposition, particle, col_score, tfidf_score) "
                        + "VALUES ("
                        + "\""
                        + featurelist.getName()
                        + "\""
                        + ", \""
                        + dp.getVerb()
                        + "\" "
                        + ", \""
                        + dp.getNoun()
                        + "\" "
                        + ", \""
                        + dp.getPreposition() != null ? dp.getPreposition() : " "
                        + "\" "
                        + ", \""
                        + dp.getParticle() != null ? dp.getParticle() : " "
                        + "\" "
                        + ", "
                        + dp.getNgramScore()
                        + " "
                        + ", "
                        + dp.getTfScore() + " " + ");";

                // System.out.println(sql);
                stmt.executeUpdate(sql);
                c.commit();

            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        System.out.println("Insert Operation done successfully");

    }

    public static AppFeatureDescriptor getFeatures(String packageID) {

        AppFeatureDescriptor ap = new AppFeatureDescriptor();
        ap.setName(packageID);

        Connection c = null;
        Statement stmt = null;

        int featureCount = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            //System.out.println("Opened database successfully");

            stmt = c.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT * FROM features WHERE app_name = '"
                            + packageID + "';");
            while (rs.next()) {

                AppFeatureDataPoint app_feature = new AppFeatureDataPoint();
                app_feature.setName(packageID);
                app_feature.setVerb(rs.getString("verb"));
                app_feature.setNoun(rs.getString("noun"));
                app_feature.setPreposition(rs.getString("preposition"));
                app_feature.setParticle(rs.getString("particle"));
                app_feature.setNgramScore(rs.getDouble("col_score"));
                app_feature.setTfScore(rs.getDouble("tfidf_score"));

                ap.addFunctionList(app_feature);
                featureCount++;

            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Read features successfully__Total : " + featureCount);

        return ap;
    }

    public static double getColocScore(String key) {
        Connection c = null;
        Statement stmt = null;

        double score = 0;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            //System.out.println("Searching coloc score...");

            stmt = c.createStatement();
            ResultSet rs = stmt
                    .executeQuery("SELECT score FROM coloc_map WHERE key = '"
                            + key + "';");
            while (rs.next()) {
                score = rs.getInt("score");
            }
            rs.close();
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }


        return score;

    }
}
