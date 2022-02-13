package analysis.ci;

public class Pointer {
    //这个类其实只起到一个父接口的作用，承载一个PointToSet，存放当前节点指针所指向的对象。

    private PointsToSet pts;

    protected Pointer() {
        pts = new PointsToSet();
    }

    /**
     * @return 指针的指向集合; 每个指针在创建时都关联了一个空集，所以不会返回null
     */
    public PointsToSet getPointsToSet() {
        return pts;
    }
}
