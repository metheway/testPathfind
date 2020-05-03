import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Statistics {


    private static String mapName = "";

    public static void setMapName(String name) {
        mapName = name;
    }

    static class StatusRecord {
        public int expandNodes = 0;
        public double pathLength = 0;
        public int maxNodesPerIteration = 0;
        public int maps = 0;
        public int highExpandingTimes = 0;
    }

    public static int count = 0;
    public static StatusRecord[] statusRecords = new StatusRecord[8];
    public static StatusRecord[] tmpStatisticsRecord = new StatusRecord[8];

    static {
        for (int i = 0; i < statusRecords.length; i++) {
            statusRecords[i] = new StatusRecord();
        }
        clearRecord();
        for (int i = 0; i < tmpStatisticsRecord.length; i++) {
            tmpStatisticsRecord[i] = new StatusRecord();
        }
    }

    public static void clearRecord() {
        for (int i = 0; i < statusRecords.length; i++) {
            statusRecords[i].pathLength = 0;
            statusRecords[i].maxNodesPerIteration = 0;
            statusRecords[i].expandNodes = 0;
        }
    }


    static boolean isSeperate = true;
    static int index = -1;
    static String recordString = "";

    public static void recordPathLenAndExpand(String algoType, double pathLen, int nodesExpand, int maxExpandPerIteration, Algorithm.AData aData) throws IOException {
        boolean shouldRecord = false;
        // 临时记录的数据
        if (isSeperate) {
            if (algoType.equals("RTAA")) {
                count++;
                index++;
                // 0 - 1 - 2 - 3
            } else if (algoType.equals("RAA_XDP")) {
                count++;
                index = 4;
            } else if (algoType.equals("RAA")) {
                count++;
                index = 5;
            } else if (algoType.equals("RAA_XUP")) {
                // 这个是最后一个的，记录必须四个都有数据，才可以记录
                count++;
                index = 6;
                if (count == 7) {
                    count = 0;
                    // 记录清算
                    shouldRecord = true;

                    if (aData.pathLen > 0.1) {
                        tmpStatisticsRecord[7].expandNodes = aData.nodesExtend;
                        tmpStatisticsRecord[7].pathLength = aData.pathLen;
                        tmpStatisticsRecord[7].maps++;
                    }
                } else {
                    // 如果没有的话，那么数据清空，重新计算
                    count = 0;
                    for (int i = 0; i < tmpStatisticsRecord.length; i++) {
                        tmpStatisticsRecord[i] = new StatusRecord();
                    }
                    recordString = "";
                    index = -1;
                    // 通知A*算法有问题
                    return;
                }
            } else {
                return;
            }

            tmpStatisticsRecord[index].expandNodes = nodesExpand;
            tmpStatisticsRecord[index].pathLength = pathLen;
            tmpStatisticsRecord[index].maxNodesPerIteration = maxExpandPerIteration;
            tmpStatisticsRecord[index].maps++;

            if (shouldRecord && tmpStatisticsRecord.length == 8) {
                recordString = mapName + "\t";
                for (int i = 0; i < tmpStatisticsRecord.length; i++) {
                    System.out.println("-================================" + tmpStatisticsRecord.length);
                    recordString += tmpStatisticsRecord[i].pathLength + "\t";
                    recordString += tmpStatisticsRecord[i].expandNodes + "\t";
                }
                recordString += "\n";

                File file = new File("output_seprate.txt");
                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(recordString);
                bufferedWriter.flush();

                for (int i = 0; i < tmpStatisticsRecord.length; i++) {
                    tmpStatisticsRecord[i] = new StatusRecord();
                }
                index = -1;
            }
        } else {
            if (algoType.equals("RTAA")) {
                count++;
                index++;
                // 0 - 1 - 2 - 3
            } else if (algoType.equals("RAA_XDP")) {
                count++;
                index = 4;
            } else if (algoType.equals("RAA")) {
                count++;
                index = 5;
            } else if (algoType.equals("RAA_XUP")) {
                // 这个是最后一个的，记录必须四个都有数据，才可以记录
                count++;
                index = 6;
                if (count == 7) {
                    count = 0;
                    // 记录清算
                    shouldRecord = true;

                    if (aData.pathLen > 0.1) {
                        tmpStatisticsRecord[7].expandNodes += aData.nodesExtend;
                        tmpStatisticsRecord[7].pathLength += aData.pathLen;
                        tmpStatisticsRecord[7].maps++;
                    }
                } else {
                    // 如果没有的话，那么数据清空，重新计算
                    count = 0;
                    for (int i = 0; i < tmpStatisticsRecord.length; i++) {
                        tmpStatisticsRecord[i] = new StatusRecord();
                    }
                    index = -1;
                    // 通知A*算法有问题
                    return;
                }
            } else {
                System.out.println(count);
                return;
            }

            tmpStatisticsRecord[index].expandNodes += nodesExpand;
            tmpStatisticsRecord[index].pathLength += pathLen;
            tmpStatisticsRecord[index].maxNodesPerIteration += maxExpandPerIteration;
            tmpStatisticsRecord[index].maps++;

            if (shouldRecord) {
                for (int i = 0; i < statusRecords.length; i++) {
                    System.out.println("-================================");
                    statusRecords[i].expandNodes += tmpStatisticsRecord[i].expandNodes;
                    statusRecords[i].maxNodesPerIteration += tmpStatisticsRecord[i].maxNodesPerIteration;
                    statusRecords[i].pathLength += tmpStatisticsRecord[i].pathLength;
                    statusRecords[i].maps += 1;
                    if (statusRecords[i].maxNodesPerIteration > 500) {
                        statusRecords[i].highExpandingTimes++;
                    }
                }
                for (int i = 0; i < tmpStatisticsRecord.length; i++) {
                    tmpStatisticsRecord[i] = new StatusRecord();
                }
                index = -1;
            }
        }
    }

    // reopen和不reopen的区别
    // reopen和fastReopen的区别
    static class Recorder {
        public double currentReopen = 0;
        public double currentFValue = 0;
        public double currentReopenRepeat = 0;
        public double currentReopenNonRepeat = 0;
        public int currentExpand = 0;

        public double sumReopen = 0;
        public double sumFValue = 0;
        public double sumReopenRepeat = 0;
        public double sumReopenNoneRepeat = 0;
        public int sumExpand = 0;
        public int mapCount = 0;
        public String type;

        public void printSumReopen() {
            System.out.println(type + ":" + sumReopen / mapCount + ":" + sumReopenRepeat / mapCount + ":" +
                    sumReopenNoneRepeat / mapCount + ":" + sumFValue / mapCount);
        }

        public void printCurrent() {
            System.out.println(type + ":" + currentReopen + ":" + currentReopenRepeat + ":" +
                    currentReopenNonRepeat + ":" + currentExpand + ":" + currentFValue);
        }

    }

    public static Recorder astartRecorder = new Recorder();
    public static Recorder fastARecorder = new Recorder();

    public static void recordAstarReopen(Algorithm.ReopenNodesRecord reopenNodes, double fValueEnd, String algoType, int nodesExpand) {
        // 需要去掉找不到的Double.max
        if (fValueEnd == Double.MAX_VALUE) {
            return;
        }
//        System.out.println(reopenNodes + "-" + fValueEnd + "-" + algoType);
        astartRecorder.type = algoType;
        astartRecorder.mapCount++;
        astartRecorder.currentExpand = reopenNodes.nodesExpand;
        astartRecorder.currentReopen = reopenNodes.reopenNodesSum;
        astartRecorder.currentFValue = fValueEnd;
        astartRecorder.currentReopenNonRepeat = reopenNodes.reopenNodesNonRepeat;
        astartRecorder.currentReopenRepeat = reopenNodes.reopenNodesRepeat;

        astartRecorder.sumReopen += reopenNodes.reopenNodesSum;
        astartRecorder.sumFValue += fValueEnd;
        astartRecorder.sumReopenRepeat += reopenNodes.reopenNodesRepeat;
        astartRecorder.sumReopenNoneRepeat += reopenNodes.reopenNodesNonRepeat;
        astartRecorder.sumExpand += reopenNodes.nodesExpand;
        astartRecorder.printSumReopen();
//        astartRecorder.printCurrent();
    }

    public static void recordFastAstarReopen(Algorithm.ReopenNodesRecord reopenNodes, double fValueEnd, String algoType) {
        if (fValueEnd == Double.MAX_VALUE || fValueEnd < 0.1) {
            return;
        }

        fastARecorder.type = algoType;
        fastARecorder.mapCount++;
        fastARecorder.currentReopen = reopenNodes.reopenNodesSum;
        fastARecorder.currentReopenRepeat = reopenNodes.reopenNodesRepeat;
        fastARecorder.currentReopenNonRepeat = reopenNodes.reopenNodesNonRepeat;
        fastARecorder.currentFValue = fValueEnd;
        fastARecorder.currentExpand = reopenNodes.nodesExpand;

        fastARecorder.sumReopen += reopenNodes.reopenNodesSum;
        fastARecorder.sumFValue += fValueEnd;
        fastARecorder.sumReopenRepeat += reopenNodes.reopenNodesRepeat;
        fastARecorder.sumReopenNoneRepeat += reopenNodes.reopenNodesNonRepeat;
        fastARecorder.sumExpand += reopenNodes.nodesExpand;
//        fastARecorder.printReopen();
        fastARecorder.printCurrent();
    }
}
