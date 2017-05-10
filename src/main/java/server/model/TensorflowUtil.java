package server.model;

import org.tensorflow.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/***
 * jar must be executed with -Djava.library.path=FULL_PATH_TO_JNI jvm option
 */
public class TensorflowUtil {

    private static final String userListsDir = "/home/vmadmin/data_storage/users";
    private static final String modelDir = "/home/vmadmin/models";
    private static HashMap<String, Graph> graphMap = new HashMap<>();
    private static HashMap<String, List<String>> labelMap = new HashMap<>();

    static {
        //System.setProperty("java.library.path", "/home/tomcat/test/jni");

        //read list of users
        Path FILE_PATH = Paths.get(userListsDir, "users.txt");
        List<String> userIDs = new ArrayList<>();
        try (Stream<String> stream = Files.lines(FILE_PATH)) {
            userIDs = stream.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //read user models+labels
        for (String id : userIDs) {
            byte[] graphDef = readAllBytesOrExit(Paths.get(modelDir, id + "_model.pb"));
            Graph g = new Graph();
            g.importGraphDef(graphDef);
            graphMap.put(id, g);

            List<String> labels =
                    readAllLinesOrExit(Paths.get(modelDir, id + "_label_strings.txt"));
            labelMap.put(id, labels);

        }

    }

    private static List<String> readAllLinesOrExit(Path path) {
        try {
            return Files.readAllLines(path, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(0);
        }
        return null;
    }

    private static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static Map<String, Float> getFeaturesScore(String userID, Float[] data) {

        if (labelMap.containsKey(labelMap.get(userID))) {
            return null;
        }

        float[] featureProbabilities = executeGraph(getTensor(getBytes(unbox(data))), userID);

        Map<String, Float> featureMap = new HashMap<>();
        for (int i = 0; i < featureProbabilities.length; i++) {
            featureMap.put(labelMap.get(userID).get(i), featureProbabilities[i]);
        }

        return featureMap;
    }

    private static float[] unbox(Float[] data){
        float[] f = new float[data.length];
        for(int i = 0; i < data.length ; i++){
            f[i] = data[i].floatValue();
        }
        return f;
    }

    private static byte[] getBytes(float[] data) {

        byte[] dataBytes = new byte[data.length * 4]; //4 bytes for a float

        for (int i = 0, j = 0; i < data.length; i++, j += 4) {
            float val = data[i];
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] dByte = buffer.putFloat(val).array();
            System.arraycopy(dByte, 0, dataBytes, j, 4);
        }

        return dataBytes;
    }


    private static Tensor getTensor(byte[] data) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);

            final Output input = b.constant("input", data);
            final Output output = b.cast(input, DataType.FLOAT);
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0);
            }
        }
    }


    private static float[] executeGraph(Tensor dataPoint, String userID) {

        if (graphMap.containsKey(graphMap.get(userID))) {
            return null;
        }

        try (Session s = new Session(graphMap.get(userID));
             Tensor result = s.runner().feed("input", dataPoint).fetch("output").run().get(0)) {
            final long[] rshape = result.shape();
            if (result.numDimensions() != 2 || rshape[0] != 1) {
                throw new RuntimeException(
                        String.format(
                                "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                Arrays.toString(rshape)));
            }
            int nlabels = (int) rshape[1];
            return result.copyTo(new float[1][nlabels])[0];
        }

    }

    static class GraphBuilder {
        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output cast(Output value, DataType dtype) {
            return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
        }

        Output constant(String name, Object value) {
            try (Tensor t = Tensor.create(value)) {
                return g.opBuilder("Const", name)
                        .setAttr("dtype", t.dataType())
                        .setAttr("value", t)
                        .build()
                        .output(0);
            }
        }

        private Graph g;
    }
}