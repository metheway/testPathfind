
import java.io.IOException;
import java.util.*;

public class Algorithm {
    private static final boolean DEBUG = true;
    public static boolean solving = true;
    ArrayList<Node> openList = new ArrayList<>();
    Set<Node> closedSet = new HashSet<Node>();
    Node lastNodeReopened;
    public PathFindingPainter pathFinding;
    private String algoType;
    public boolean running = false;

    public void AStar(PathFindingPainter pathFinding) {
        // 载入地图，起点，终点
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findAPath(pathFinding.map, start, end);
    }

    public void AStarFast(PathFindingPainter pathFinding) {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
    }

    public void WA(PathFindingPainter pathFinding) {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findWAPath(pathFinding.map, start, end);
    }

    public void RTAA(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRTAAPath(pathFinding.map, start, end, 200, 2, new AData(1, 1));
    }

    public void RTA_XDP(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRTA_XDPPath(pathFinding.map, start, end, 100, 2);
    }

    public void RTA_XUP(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRTA_XUPPath(pathFinding.map, start, end, 10, 2);
    }

    public void RAA(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRAAPath(pathFinding.map, start, end, 200, 2, new AData(1, 1));
    }

    public void RAA_XDP(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRAA_XDPPath(pathFinding.map, start, end, 200, 2, new AData(1, 1));
    }

    public void RAA_XUP(PathFindingPainter pathFinding) throws IOException {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findRAA_XUPPath(pathFinding.map, start, end, 200, 2, new AData(1, 1));
    }

    public void WAFast(PathFindingPainter pathFinding) {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
    }

    public void XDP(PathFindingPainter pathFinding) {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
        findXDPPath(pathFinding.map, start, end);
    }

    public void XDPFast(PathFindingPainter pathFinding) {
        this.pathFinding = pathFinding;
        Node start = pathFinding.map[pathFinding.startx][pathFinding.starty];
        Node end = pathFinding.map[pathFinding.finishx][pathFinding.finishy];
    }


    static class ReopenNodesRecord {
        public int reopenNodesRepeat = 0;
        public int reopenNodesNonRepeat = 0;
        public int reopenNodesSum = 0;
        public int nodesExpand = 0;
    }

    class Strategy {
        public boolean isReopen = false;
        public boolean isFastReopen = false;
    }

    // 启发值乘的系数
    private double hCoefficient = 1;
    // 是否reopen和fastreopen的策略
    private Strategy strategy = new Strategy();
    // reopen记录
    private ReopenNodesRecord reopenNodesRecord;
    private int nodesExpand = 0;

    private double findPath(Node[][] currentMap, Node start, Node end) {
        // 必要的初始化
        necessaryInit(start, end);
        Node currentNode = start;
        nodesExpand = 0;

        while (solving) {
            if ((openList.size() <= 0)) {
                // 如果是起点，那么
                break;
            }

            nodesExpand++;

            // openList里面的点，应该早就算过了
            currentNode = openList.remove(0);
            closedSet.add(currentNode);

            if (end.equals(currentNode)) {
                System.out.println("done=============");
                solving = false;
                break;
            }

            if (currentNode != start) {
                currentNode.setType(NodeType.OPEN);
            }

            // 注意边界
            ArrayList<Node> neighbours = getNeighbors(currentMap, currentNode);
            // 如果有更好的路径，移出closedSet加入openList并且reopen
            for (int i = 0; i < neighbours.size(); i++) {
                Node neighbor = neighbours.get(i);
                // 如果已经走过，进了closedset，或者是墙，这个点就不必走了
                if (neighbor.getType() == NodeType.WALL) {
                    continue;
                }

                // 如果在closedSet里面，则计算过了的，进行reopen
                if (strategy.isReopen && closedSet.contains(neighbor)) {
                    if (neighbor.g > currentNode.g + getDistance(neighbor, currentNode)) {
                        // 如果没有采用fastreopen, reopen
                        reopen(end, currentNode, neighbor);
                        openList.remove(neighbor);
                        neighbor.setRepeated(true);
                        reopenNodesRecord.reopenNodesSum++;
                    }
                }

                if (openList.contains(neighbor)) {
                    if (neighbor.g > currentNode.g + getDistance(neighbor, currentNode)) {
                        reopen(end, currentNode, neighbor);
                    }
                    continue;
                } else if (closedSet.contains(neighbor)) {
                    // 如果在closedSet里面，不用open了，因为已经open过
                    continue;
                } else {
                    // 如果没有在扩展节点中，那么加入扩展节点集
                    reopen(end, currentNode, neighbor);
                    if (!neighbor.equals(end)) {
                        neighbor.setType(NodeType.CHECKED);
                    }
                    openList.add(neighbor);
                }

                // 这里可以更新节点的情况，回调更新节点
                reopenNodesRecord.nodesExpand++;
                if (pathFinding != null) {
                    pathFinding.onUpdate();
                }
            }
            mergeSort(openList, 0, openList.size() - 1);
        }

        // 记录这个算法的类型，reopen的数量
        for (int i = 0; i < currentMap.length; i++) {
            for (int j = 0; j < currentMap[0].length; j++) {
                if (currentMap[i][j].isRepeated()) {
                    // 不重复的open的点
                    reopenNodesRecord.reopenNodesNonRepeat++;
                }
            }
        }
        reopenNodesRecord.reopenNodesRepeat = reopenNodesRecord.reopenNodesSum - reopenNodesRecord.reopenNodesNonRepeat;

        // 另外更新f值为pathlength
        double pathLen = 0;

        if (end.getParrent() == null) {
            return pathLen;
        }

        while (!end.equals(start) || !end.equals(null)) {
            Node parrent = end.getParrent();
            if (parrent == null) {
                break;
            }
            if (!parrent.equals(start) && !parrent.equals(end)) {
                parrent.setType(NodeType.FINALPATH);
            }
            pathLen += getDistance(end, parrent);
            end = parrent;
        }
        end.f = pathLen;
        return pathLen;
    }

    private void updateFValue(Node node, double hCoefficient, String algoType) {
        if (algoType.contains("XDP")) {
            node.f = 1 / (2 * hCoefficient) * (node.g + node.h + Math.sqrt((node.g - node.h) * (
                    node.g - node.h) + 4 * hCoefficient * node.h * node.h));
        } else if (algoType.contains("XUP")) {
            node.f = 1 / (2 * hCoefficient) * (node.g + node.h + Math.sqrt((node.g + node.h) * (
                    node.g + node.h) + 4 * hCoefficient * (hCoefficient - 1) * node.h * node.h));
        } else {
            node.f = node.g + node.h;
        }
    }

    private void reopen(Node end, Node currentNode, Node neighbor) {
        // 计算neighbour节点的启发值等，将neighbour节点的父节点设置为当前节点
        neighbor.g = currentNode.g + getDistance(neighbor, currentNode);
        neighbor.h = getHeuristic(end, neighbor);
        updateFValue(neighbor, hCoefficient, algoType);
        neighbor.setParrent(currentNode);
    }

    private boolean isDiagonal(Node lastNodeReopened, Node neighbor) {
        return Math.abs(lastNodeReopened.getRow() - neighbor.getRow()) == Math.abs(lastNodeReopened.getCol() - neighbor.getCol());
    }

    private void necessaryInit(Node start, Node end) {
        solving = true;
        reopenNodesRecord = new ReopenNodesRecord();

        openList.clear();
        closedSet.clear();

        openList.add(start);
        Node currentNode = start;
        currentNode.g = 0;
        currentNode.h = getHeuristic(end, currentNode);
        updateFValue(currentNode, hCoefficient, algoType);
        lastNodeReopened = start;
    }

    private double getHeuristic(Node end, Node current) {
        return hCoefficient * current.getOctileDist(end);
//        return hCoefficient * current.getManHattanDist(end);
    }

    public AData findAPath(Node[][] currentMap, Node start, Node end) {
        algoType = "A";
        hCoefficient = 1;
        strategy.isFastReopen = false;
        strategy.isReopen = true;
        double pathLen = findPath(currentMap, start, end);
        // 如果找到了
        if (!solving) {
            Statistics.recordAstarReopen(reopenNodesRecord, pathLen, algoType, nodesExpand);
        }
        if (pathLen >= 0.1) {
            System.out.println("B:" + nodesExpand);
        }
        AData aData = new AData();
        aData.nodesExtend = nodesExpand;
        aData.pathLen = pathLen;
        return aData;
    }

    class AData {
        public double pathLen = 0;
        public int nodesExtend = 0;

        public AData() {
        }

        public AData(double pathLen, int nodesExtend) {
            this.pathLen = pathLen;
            this.nodesExtend = nodesExtend;
        }
    }


    public void findWAPath(Node[][] currentMap, Node start, Node end) {
        algoType = "WA";
        hCoefficient = 2;
        strategy.isReopen = false;
        strategy.isFastReopen = false;
        double pathLen = findPath(currentMap, start, end);
        if (!solving) {
            Statistics.recordFastAstarReopen(reopenNodesRecord, pathLen, algoType);
        }
    }

    public void findXDPPath(Node[][] currentMap, Node start, Node end) {
        // 排序有所不同，更新f不一样
        algoType = "XDP";
        hCoefficient = 2;
        strategy.isReopen = false;
        strategy.isFastReopen = false;
        double pathLen = findPath(currentMap, start, end);
        if (!solving) {
            Statistics.recordFastAstarReopen(reopenNodesRecord, pathLen, algoType);
        }
    }

    public void findRTA_XDPPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon) throws IOException {
        algoType = "RTAA_XDP";
        hCoefficient = epsilon;
        int expandingNodes = originalExpandingNodes;
        double pathLen = findPath(currentMap, start, end);

        Node currentNode = start;
        int MAX_EXPAND = 999999;
        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, new AData(1, 1));
    }

    public void findRTA_XUPPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon) throws IOException {
        algoType = "RTAA_XUP";
        hCoefficient = epsilon;
        int expandingNodes = originalExpandingNodes;
        double pathLen = findPath(currentMap, start, end);

        Node currentNode = start;
        int MAX_EXPAND = 999999;
        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, new AData(1, 1));
    }

    public void findRTAAPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon, AData aData) throws IOException {
        algoType = "RTAA";
        hCoefficient = epsilon;
        int expandingNodes = originalExpandingNodes;
        Node currentNode = start;
        // 防止死循环
        int MAX_EXPAND = 999999;

        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, aData);
    }

    public void findRAAPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon, AData aData) throws IOException {
        algoType = "RAA";
        hCoefficient = epsilon;
        // 表示无限制扩展
        int expandingNodes = originalExpandingNodes;
        originalExpandingNodes = -1;

        Node currentNode = start;
        int MAX_EXPAND = 999999;

        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, aData);
    }

    public void findRAA_XDPPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon, AData aData) throws IOException {
        algoType = "RAA_XDP";
        hCoefficient = epsilon;
        int expandingNodes = originalExpandingNodes;
        originalExpandingNodes = -1;

        Node currentNode = start;
        int MAX_EXPAND = 999999;

        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, aData);
    }

    public void findRAA_XUPPath(Node[][] currentMap, Node start, Node end, int originalExpandingNodes, double epsilon, AData aData) throws IOException {
        algoType = "RAA_XUP";
        hCoefficient = epsilon;
        int expandingNodes = originalExpandingNodes;
        originalExpandingNodes = -1;

        Node currentNode = start;
        int MAX_EXPAND = 999999;

        realTimePathFinding(algoType, currentMap, start, end, originalExpandingNodes, expandingNodes, currentNode, MAX_EXPAND, aData);
    }

    private void realTimePathFinding(String algoType, Node[][] currentMap, Node start, Node end, int originalExpandingNodes,
                                     int expandingNodes, Node currentNode, int MAX_EXPAND, AData aData) throws IOException {
        necessaryInit(start, end);
        double pathLen = 0;
        Node originalStart = start;
        Node lastEnd = start;
        int nodesExpand = 0;
        int maxExpandPerIteration = originalExpandingNodes;
        int nodesExpandPerIteration = 0;

        running = true;

        boolean rangeAdaptive = false;
        boolean useRangeAdaptive = false;

//         这个是为了RRA准备的
        if (originalExpandingNodes == -1) {
            rangeAdaptive = true;
            useRangeAdaptive = true;
        }
        if (algoType.contains("RRA")) {
            originalExpandingNodes = 2;
        }
//        else {
//            originalExpandingNodes = 50;
//        }

//        originalExpandingNodes = 50;

        while (MAX_EXPAND > 0) {
            // 如果可以扩展，那么每次还原
            if (useRangeAdaptive && !rangeAdaptive) {
                rangeAdaptive = true;
            }

            if (end.equals(currentNode)) {
                // 如果到达终点，那么停止
                break;
            }
            if (false == running) {
                // pause，则停止
                break;
            }
            // 扩展这么多个节点，找到最小的f，走到那里去，再更新closedSet
            while (expandingNodes > 0 || rangeAdaptive) {
                // 扩展点数大于500就跳出
                if (nodesExpandPerIteration > 200) {
                    nodesExpandPerIteration = 0;
                    break;
                }

                nodesExpand++;
                nodesExpandPerIteration++;

                if ((openList.size() <= 0)) {
                    // 如果是起点，那么
                    break;
                }

                // openList里面的点，应该早就算过了
                currentNode = openList.remove(0);
                closedSet.add(currentNode);

                // 如果到达终点
                if (end.equals(currentNode)) {
                    solving = false;
                    break;
                }

                // 如果是range可变，那么直到没有碰到障碍才停止扩展，也就是最前沿的扩展节点周围没有环状
                if (useRangeAdaptive && stillLoopExpand(currentMap, currentNode)) {
                    // 环状的障碍，那么就继续扩展
                    rangeAdaptive = true;
                } else {
                    // 非环状障碍，则按RTAA扩展
                    rangeAdaptive = false;
                }

                if (!currentNode.equals(start)) {
                    currentNode.setType(NodeType.CHECKED);
                }

                // 注意边界
                ArrayList<Node> neighbours = getNeighbors(currentMap, currentNode);
                // 如果有更好的路径，移出closedSet加入openList并且reopen
                for (int i = 0; i < neighbours.size(); i++) {
                    Node neighbor = neighbours.get(i);

                    // 如果已经走过，进了closedset，或者是墙，这个点就不必走了
                    if (neighbor.getType() == NodeType.WALL) {
                        continue;
                    }

                    // 跳过那些已经扫描过的，如果closedSet包括，跳过
                    if (closedSet.contains(neighbor)) {
                        continue;
                    }

                    // 一定要邻居才可以，否则造成跳跃行进
                    if (openList.contains(neighbor)) {
                        if (neighbor.g > currentNode.g + getDistance(neighbor, currentNode)) {
                            // 把当前节点设置为邻居节点的双亲节点
                            computeNodeHeuristic(end, currentNode, neighbor);
                        }
                        continue;
                    } else {
                        // 如果没有在扩展节点中，那么计算后加入扩展节点集
                        computeNodeHeuristic(end, currentNode, neighbor);
                        openList.add(neighbor);
                    }

                    // 这里可以更新节点的情况，回调更新节点
                    reopenNodesRecord.nodesExpand++;
                    if (pathFinding != null) {
                        pathFinding.onUpdate();
                    }
                }
                mergeSort(openList, 0, openList.size() - 1);
                expandingNodes--;
                MAX_EXPAND--;
            }

            if (openList.isEmpty()) {
                // 没有找到出口
                break;
            }

            // 找到最小f的节点，更新所有closedSet里面的
            double minF = openList.get(0).f;
            for (Node node : closedSet
            ) {
                node.g = minF - node.h;
            }

            if (rangeAdaptive && nodesExpandPerIteration > maxExpandPerIteration) {
                maxExpandPerIteration = nodesExpandPerIteration;
                nodesExpandPerIteration = 0;
            }

            if (DEBUG) {
                // 如果是画图检查
                Node tmpCurr = currentNode;
                while (!tmpCurr.equals(lastEnd)) {
                    Node parrent = tmpCurr.getParrent();
                    if (parrent == null) {
                        break;
                    }
                    parrent.setType(NodeType.FINALPATH);
                    // 为什么会加1和根号2以外的数字,parrent有超出1以外的！！！原来tmpCurr才是当前节点
                    pathLen += getDistance(tmpCurr, parrent);

                    // 起点和终点颜色不能变
                    if (!parrent.equals(originalStart) && !parrent.equals(end)) {
                        parrent.setType(NodeType.FINALPATH);
                    }
                    tmpCurr = parrent;
                }
            }
            lastEnd = currentNode;
            // 找出路径
            expandingNodes = originalExpandingNodes;
            // 将终点找出，并且作为下一个起点
            start = currentNode;
        }
        // 结束了
        // 找到pathLen长度
        System.out.println("path:" + pathLen);
        System.out.println("nodes_expanded:" + nodesExpand);
        if (maxExpandPerIteration == -1) {
            maxExpandPerIteration = 50;
        }
        System.out.println("max_nodes_expanded_per_interation:" + maxExpandPerIteration);
        Statistics.recordPathLenAndExpand(this.algoType, pathLen, nodesExpand, maxExpandPerIteration, aData);
        // 记录扩展节点数，每轮最大扩展节点数，路径长度

        originalStart.setType(NodeType.START);
        end.setType(NodeType.FINISH);

        if (pathFinding != null) {
            pathFinding.onUpdate();
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void computeNodeHeuristic(Node end, Node currentNode, Node neighbor) {
        // 计算neighbour节点的启发值等，将neighbour节点的父节点设置为当前节点
        neighbor.g = currentNode.g + getDistance(neighbor, currentNode);
        neighbor.h = getHeuristic(end, neighbor);
        updateFValue(neighbor, hCoefficient, algoType);
        neighbor.setParrent(currentNode);
    }

    private double backTrack(Node start, Node end) {
        double len = 0;
        while (!end.equals(start)) {
            Node curr = end.getParrent();

            if (curr == null) {
                break;
            }
            len += getDistance(end, curr);
            end = curr;
        }
        return len;
    }

    private boolean stillLoopExpand(Node[][] currentMap, Node currentNode) {
        int nodesInClosedSet = 0;

        // 确认是否是环障碍
        for (Node node : getNeighbors(currentMap, currentNode)) {
            if (closedSet.contains(node)) {
                nodesInClosedSet++;
            }
        }
        if (nodesInClosedSet > 1) {
            // 有环障碍
            return true;
        } else {
            return false;
        }
    }

    private ArrayList<Node> getNeighbors(Node[][] currentMap, Node node) {
        ArrayList<Node> neighbours = new ArrayList<>();
        for (int row = -1; row <= 1; row++) {
            for (int col = -1; col <= 1; col++) {
                if (row == 0 && col == 0) {
                    continue;
                }
                int newRow = node.getRow() + row;
                int newCol = node.getCol() + col;
                if (newRow < 0 || newRow >= currentMap.length ||
                        newCol < 0 || newCol >= currentMap[0].length) {
                    continue;
                }
                Node neighbor = currentMap[newRow][newCol];
                neighbours.add(neighbor);
            }
        }
        return neighbours;
    }

    public void mergeSort(ArrayList<Node> openList, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        mergeSort(openList, left, mid);
        mergeSort(openList, mid + 1, right);
        merge(openList, left, mid, right);
    }

    private void merge(ArrayList<Node> openList, int left, int mid, int right) {
        int i, j;
        ArrayList<Node> tmpList = new ArrayList<>();
        for (int t = left; t <= right; t++) {
            tmpList.add(openList.get(t));
        }

        int k = left;
        for (i = left, j = mid + 1; i <= mid && j <= right; k++) {
            if (tmpList.get(i - left).f > tmpList.get(j - left).f) {
                openList.set(k, tmpList.get(j++ - left));
            } else {
                openList.set(k, tmpList.get(i++ - left));
            }
        }

        while (i < mid) {
            openList.set(k++, tmpList.get(i++ - left));
        }
        while (j < right) {
            openList.set(k++, tmpList.get(j++ - left));
        }
    }

    private double getDistance(Node firstNode, Node secNode) {
        int xdif = Math.abs(firstNode.getRow() - secNode.getRow());
        int ydif = Math.abs(firstNode.getCol() - secNode.getCol());
        double result = Math.sqrt(xdif * xdif + ydif * ydif);
        return result;
    }

}
