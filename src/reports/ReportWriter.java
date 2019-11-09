package reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ReportWriter {
    private static String fileInDeg = "graphStats/inDegreeData-shuffle-star-50cache.txt";
    private static String fileCluster = "graphStats/clusteringCoefficientData-shuffle-star-50cache.txt";
    private static String fileShortestPath = "graphStats/shortestPathData-shuffle-star-50cache.txt";

    private static int clusterCoeffCounter = 0;
    private static int shortestPathCounter = 0;


    public static void writeInDegree(String s) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileInDeg), true));
            bw.write(s);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeClusterCoefficient(double coeff) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileCluster), true));
            bw.write(++clusterCoeffCounter + " " + coeff + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeShortestPath(double length) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileShortestPath), true));
            bw.write(++shortestPathCounter + " " + length + "\n");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
