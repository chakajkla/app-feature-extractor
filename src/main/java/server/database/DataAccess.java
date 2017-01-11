package server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import server.nlp.NLPUtil;

import server.objects.AndroidApp;
import server.objects.AppFeatureDataPoint;
import server.objects.AppFeatureDescriptor;
import server.objects.PathStorage;

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
