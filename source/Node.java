import java.util.Objects;

class Node {
    // 0 = start, 1 = finish, 2 = wall, 3 = empty, 4 = checked, 5 = finalpath
    private NodeType nodeType = NodeType.EMPTY;
    private int hops;
    private int row;
    private int col;
    private int lastRow;
    private int lastCol;
    private Node parrent;
    private double dToEnd = 0;
    private boolean isRepeated = false;

    public double f = Double.MAX_VALUE;
    public double h = Double.MAX_VALUE;
    public double g;

    public Node(NodeType type, int x, int y) {    //CONSTRUCTOR
        nodeType = type;
        this.row = x;
        this.col = y;
        hops = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return row == node.row && col == node.col;
    }

    @Override
    public int hashCode() {
        return row * 7 + col * 13;
//        return Objects.hash(row, col);
    }

    public int getRow() {
        return row;
    }        //GET METHODS

    public int getCol() {
        return col;
    }

    public int getLastRow() {
        return lastRow;
    }

    public int getLastCol() {
        return lastCol;
    }

    public NodeType getType() {
        return nodeType;
    }

    public int getHops() {
        return hops;
    }

    public boolean isRepeated() {
        return isRepeated;
    }

    public Node getParrent() {
        return parrent;
    }

    public void setType(NodeType type) {
        nodeType = type;
    }
    //SET METHODS

    public void setLastNode(int x, int y) {
        lastRow = x;
        lastCol = y;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public void setParrent(Node parrent) {
        this.parrent = parrent;
    }

    public void setRepeated(boolean repeated) {
        this.isRepeated = repeated;
    }

    @Override
    public String toString() {
        return "Node{" +
                "nodeType=" + nodeType +
                ", x=" + row +
                ", y=" + col +
                '}';
    }

    public double getOctileDist(Node end) {
        return getOctileDist(end.row, end.col);
    }

    public double getOctileDist(int finishx, int finishy) {        //CALCULATES THE EUCLIDIAN DISTANCE TO THE FINISH NODE
        int xdif = Math.abs(row - finishx);
        int ydif = Math.abs(col - finishy);
        dToEnd = Math.max(xdif, ydif) + (Math.sqrt(2) - 1) * Math.min(xdif, ydif);
        return dToEnd;
    }

    public double getManHattanDist(Node end) {
        return getManHattanDist(end.row, end.col);
    }

    public double getManHattanDist(int finishx, int finishy) {
        int xdif = Math.abs(row - finishx);
        int ydif = Math.abs(col - finishy);
        dToEnd = Math.sqrt((xdif * xdif) + (ydif * ydif));
        return dToEnd;
    }

}