package reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    private static String fileInDeg = "graphStats/inDegreeData.txt";
    private static String fileCluster = "graphStats/clusteringCoefficientData.txt";
    private static String fileShortestPath = "graphStats/shortestPathData.txt";

    private static int clusterCoeffCounter = 0;
    private static int shortestPathCounter = 0;


    static BufferedWriter writerInDeg;
    static BufferedWriter writerClusterCoeff;
    static BufferedWriter writerShortestPath;

//    static {
//        try {
//            writerInDeg = new BufferedWriter(new FileWriter(fileInDeg, true));
//            writerClusterCoeff = new BufferedWriter(new FileWriter(new File(fileCluster), true));
//            writerShortestPath = new BufferedWriter(new FileWriter(new File(fileShortestPath), true));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void writeInDegree(String s) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileInDeg), true));
            bw.write(s);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void closeInDegreeWriter() {
//        try {
//            writerInDeg.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void writeClusterCoefficient(double coeff) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileCluster), true));
            bw.write(++clusterCoeffCounter + " " + coeff + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void closeClusterCoefficientWriter() {
//        try {
//            writerClusterCoeff.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static void writeShortestPath(double length) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileShortestPath), true));
            bw.write(++shortestPathCounter + " " + length + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void closeShortestPathWriter() {
//        try {
//            writerShortestPath.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
