import org.junit.Test;

import java.io.*;
import java.util.*;

public class MapReader {
    Set<String> readMapSet = new HashSet<>();

    Node start;
    Node end;

    String[] currentMapInfo;
    Node[][] currentMap;
    private static File map;

    public void init() {
        initMap();
        // 找个起点，找个终点
        start = findAFreeNode(currentMap);
        start.setType(NodeType.START);
        // 必须要能连通的，这个怎么保证，直接用并查集试试

        end = findAFreeNode(currentMap);
        end.setType(NodeType.FINISH);
    }


    public static void main(String[] args) throws IOException {
        MapReader experiment = new MapReader();
        while (true) {
            // 读取下一幅地图
            if (experiment.readMaps()) {
                System.out.println("all finished");
                String stringToWrite = "";
                // 将四个数据都写入文件吧,
                for (int i = 0; i < Statistics.statusRecords.length; i++) {
                    // 输出什么
                    stringToWrite += Statistics.statusRecords[i].pathLength + "\r\n";
                }
                for (int i = 0; i < Statistics.statusRecords.length; i++) {
                    stringToWrite += Statistics.statusRecords[i].expandNodes + "\r\n";
                }
                for (int i = 0; i < Statistics.statusRecords.length; i++) {
                    stringToWrite += Statistics.statusRecords[i].maxNodesPerIteration + "\r\n";
                }
                for (int i = 0; i < Statistics.statusRecords.length; i++) {
                    stringToWrite += Statistics.statusRecords[i].maps + "\r\n";
                    System.out.println(Statistics.statusRecords[i].highExpandingTimes);
                }

                String fileName = "output.txt";
                File file = new File(fileName);

                if (!file.exists()) {
                    file.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(stringToWrite);

                bufferedWriter.close();
                fileWriter.close();
                break;
            }

            Statistics.setMapName(map.getName());
            System.out.println("================================");

            // 初始化地图，起点和终点
            experiment.init();
            Algorithm algorithm = new Algorithm();

            // A* 算法
            Algorithm.AData aData = algorithm.findAPath(experiment.currentMap, experiment.start, experiment.end);
            if (aData.pathLen <= 0.1) {
                experiment.start = null;
                experiment.end = null;
                continue;
            } else {

            }

            // RTAA* range = 50
            experiment.initMap();
            double epsilon = 2;
            int originalExpandingNodes = 50;
            for (originalExpandingNodes = 50; originalExpandingNodes <= 200; originalExpandingNodes += 50) {
                algorithm.findRTAAPath(experiment.currentMap, experiment.start, experiment.end, originalExpandingNodes, epsilon, aData);
            }
            originalExpandingNodes = 50;

            // RRA*
            experiment.initMap();
            algorithm.findRAAPath(experiment.currentMap, experiment.start, experiment.end, originalExpandingNodes, epsilon, aData);

            // RRA*_XDP
            experiment.initMap();
            algorithm.findRAA_XDPPath(experiment.currentMap, experiment.start, experiment.end, originalExpandingNodes, epsilon, aData);

            // RRA*_XUP
            experiment.initMap();
            algorithm.findRAA_XUPPath(experiment.currentMap, experiment.start, experiment.end, originalExpandingNodes, epsilon, aData);

//            experiment.initMap();
//            algorithm.findWAPath(experiment.currentMap, experiment.start, experiment.end);
//
//            experiment.initMap();
//            algorithm.findXDPPath(experiment.currentMap, experiment.start, experiment.end);

            // 防止start和end沿用
            experiment.start = null;
            experiment.end = null;

        }
    }

    public void initMap() {
        currentMap = null;
        currentMap = new Node[currentMapInfo.length][currentMapInfo[0].length()];
        for (int row = 0; row < currentMapInfo.length; row++) {
            for (int col = 0; col < currentMapInfo[0].length(); col++) {
                if (currentMapInfo[row].charAt(col) == '@' || currentMapInfo[row].charAt(col) == 'T') {
                    currentMap[row][col] = new Node(NodeType.WALL, row, col);
                } else if (currentMapInfo[row].charAt(col) == '.' ||
                        currentMapInfo[row].charAt(col) == 'C') {
                    currentMap[row][col] = new Node(NodeType.EMPTY, row, col);
                } else {
                    currentMap[row][col] = new Node(NodeType.WALL, row, col);
                }
            }
        }

        if (start != null) {
            start = currentMap[start.getRow()][start.getCol()];
            start.setType(NodeType.START);
        }
        if (end != null) {
            end = currentMap[end.getRow()][end.getCol()];
            end.setType(NodeType.FINISH);
        }
    }

    private Node findAFreeNode(Node[][] currentMap) {
        int width = currentMap[0].length;
        int height = currentMap.length;

        Random random = new Random();
        Node node = null;
        int row;
        int col;
        while (node == null || node.getType() == NodeType.WALL) {
            row = random.nextInt(height);
            col = random.nextInt(width);
            if (node == null) {
                node = currentMap[row][col];
            }

            if (node == end && NodeType.START == node.getType()) {
                continue;
            }
            if (node == start && NodeType.FINISH == node.getType()) {
                continue;
            }
            node = currentMap[row][col];
        }
        return node;
    }

    public boolean readMaps() throws IOException {
        File mapDirectory = new File("maps");
        int width = 0;
        int height = 0;
        boolean mapAllRead = true;
        map = null;
        for (File m : mapDirectory.listFiles()) {
            map = m;
            // 如果已经读取过了，那么跳过
            if (readMapSet.contains(map.getName()) || map.getName().contains("512") ||map.getName().contains("1024")) {
                continue;
            }
            System.out.println(map.getName());
            // 逐行读取，碰到type, width,height, map之后开始@为障碍物
            BufferedReader bufferedReader = new BufferedReader(new FileReader(map));
            String line = bufferedReader.readLine();
            int index = 0;
            while (line != null) {
                if (line.contains("type") || line.contains("map")) {
                    line = bufferedReader.readLine();
                    continue;
                }
                if (line.contains("width")) {
                    width = Integer.parseInt(line.split(" ")[1]);
                    line = bufferedReader.readLine();
                    continue;
                }
                if (line.contains("height")) {
                    height = Integer.parseInt(line.split(" ")[1]);
                    currentMapInfo = new String[height];
                    line = bufferedReader.readLine();
                    continue;
                }

                currentMapInfo[index++] = line;
                line = bufferedReader.readLine();
            }
            // 每一幅地图都要读取一次
            mapAllRead = false;
            break;
        }
        readMapSet.add(map.getName());
        return mapAllRead;
    }

    @Test
    public void Test() throws IOException {
        String fileName = "abc";
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write("abc");

        bufferedWriter.close();
        fileWriter.close();
    }
}