package server.database;

import org.apache.commons.lang3.StringUtils;
import server.log.LogUtil;
import server.nlp.NLPUtil;
import server.objects.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataAccess {

    public static boolean updateData(AndroidApp app) {

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            LogUtil.log("Opened database successfully");

            stmt = c.createStatement();

            String desc = NLPUtil.removeNonCharacters(app.getDescription());

            String sql = "INSERT INTO app_data (app_name, description) "
                    + "VALUES (\"" + app.getPackageName() + "\", \"" + desc
                    + "\" );";

            // LogUtil.log(sql);
            stmt.executeUpdate(sql);
            c.commit();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
        LogUtil.log("Insert Operation done successfully");

        return true;

    }
    
    public static boolean insertNewUser(String userId, int numberOfApps, String osVersion, String sdkVersion, String phoneModel, String realDeviceId) {
        Connection c = null;
        Statement stmt = null;
        String endOfStudyId = StringUtils.substring(UUID.randomUUID().toString(), 0, 6);
        
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            LogUtil.log("Opened database successfully");

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
                        +", os_version = '" + osVersion
                        +"', sdk_version = '" + sdkVersion
                        +"', phone_model = '" + phoneModel
                        + "' WHERE device_id = \"" + userId + "\";";
            } else {
                sql = "INSERT INTO user_data (device_id, number_apps, end_of_study_id, os_version, sdk_version, phone_model, real_device_id) "
                        + "VALUES (\"" + userId + "\", " + numberOfApps + ", \"" + endOfStudyId + "\", \"" + osVersion + "\", \"" + sdkVersion + "\", \"" + phoneModel + "\", \"" +realDeviceId+ "\")";
                
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
        LogUtil.log("Insert Operation done successfully");

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
            LogUtil.log("Opened database successfully");

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
        LogUtil.log("Update Operation done successfully");

        return true;
    }


    public static boolean updateLabellingCount(String userId, int labellingCount) {

        int previousCount = getLabellingCountWithDeviceId(userId);

        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            LogUtil.log("Opened database successfully");

            stmt = c.createStatement();

            String sql = "UPDATE user_data"
                    + " SET labelling_count = " + (previousCount + labellingCount)
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
        LogUtil.log("Update Operation done successfully");

        return true;
    }


    public static boolean updateInvalidUser(String userId) {

        Connection c = null;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            LogUtil.log("Opened database successfully");

            stmt = c.createStatement();

            String sql = "UPDATE user_data"
                    + " SET invalid = 1"
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
        LogUtil.log("Update Operation done successfully");

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
            LogUtil.log("Opened database successfully");

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
                user.setEndOfStudy(result.getString(9));
                user.setLabellingCount(result.getInt(12));
                
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
        
        
        LogUtil.log("Select Operation done successfully");
        return userList;
    }

    public static int getLabellingCountWithDeviceId(String deviceId) {
        List<User> userList = getAllUsers();

        for (User user : userList) {
            if (StringUtils.equals(user.getDeviceId(), deviceId)) {
                return user.getLabellingCount();
            }
        }

        return 0;
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
            LogUtil.log("Opened database successfully");

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
        
        
        LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
        
        
        LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
            LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
            LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
        
        
        LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
        
        
        LogUtil.log("Select Operation done successfully");
        return apps;
    }
    
    public static boolean insertNewLabelledFile(String fileName, String userId, String description, boolean secondStage) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:"
                    + PathStorage.databasePath);
            c.setAutoCommit(false);
            LogUtil.log("Opened database successfully");

            stmt = c.createStatement();

            String sql = "INSERT INTO labelled_files (file_name, file_State, user_id, state_desc, first_stage) "
                    + "VALUES (\"" + fileName + "\", 0, \"" + userId + "\", \"" + description + "\", " + (secondStage ? 0 : 1) + ");";
            
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
        LogUtil.log("Insert Operation done successfully");

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
            LogUtil.log("Opened database successfully");

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
        LogUtil.log("Update Operation done successfully");

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
            LogUtil.log("Opened database successfully");

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
        LogUtil.log("Select Operation done successfully");
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
            LogUtil.log("Opened database successfully");

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
        LogUtil.log("Check Operation done successfully");

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
            LogUtil.log("Opened database successfully");

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

                // LogUtil.log(sql);
                stmt.executeUpdate(sql);
                c.commit();

            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
        LogUtil.log("Insert Operation done successfully");

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
            //LogUtil.log("Opened database successfully");

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
        LogUtil.log("Read features successfully__Total : " + featureCount);

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
            //LogUtil.log("Searching coloc score...");

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
