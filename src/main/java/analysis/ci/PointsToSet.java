package analysis.ci;

import com.google.common.collect.Iterators;
import elem.Obj;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;


public class PointsToSet implements Iterable<Obj> {
    //本质上代表一个pointer所指向的对象。

    private Set<Obj> pts;

    public PointsToSet() {
        this(new HashSet<>());
    }

    public PointsToSet(Set<Obj> pts) {
        this.pts = pts;
    }

    public static PointsToSet singleton(Obj o) {
        PointsToSet pointsToSet = new PointsToSet();
        pointsToSet.addObject(o);
        return pointsToSet;
    }

    /**
     * 往pts中添加指向的对象
     * @param obj
     * @return 返回false，如果给定的obj早已存在；否则返回true
     */
    public boolean addObject(Obj obj) {
        return pts.add(obj);
    }

    /**
     * @return pts是否为空
     */
    public boolean isEmpty() {
        return pts.isEmpty();
    }

    /**
     * @return pts集合的迭代器
     */
    @Override
    public Iterator<Obj> iterator() {
        return Iterators.unmodifiableIterator(pts.iterator());
    }

    /**
     * @param pts1
     * @param pts2
     * @return pts1 U pts2
     */
    public static PointsToSet union(PointsToSet pts1, PointsToSet pts2) {
        PointsToSet result = new PointsToSet();
        result.pts.addAll(pts1.pts);
        result.pts.addAll(pts2.pts);
        return result;
    }

    /**
     * 合并另一个pts到当前
     * @param pts
     */
    public void union(PointsToSet pts) {
        for (Obj o : pts) {
            addObject(o);
        }
    }

    /**
     * @param pts1
     * @param pts2
     * @return pts1 - pts2
     */
    public static PointsToSet difference(PointsToSet pts1, PointsToSet pts2) {
        PointsToSet result = new PointsToSet();
        result.pts.addAll(pts1.pts);
        result.difference(pts2);
        return result;
    }

    /**
     * self = self - pts
     * @param pts
     */
    public void difference(PointsToSet pts) {
        for (Obj o : pts) {
            if (this.pts.contains(o)) {
                this.pts.remove(o);
            }
        }
    }

    @Override
    public String toString() {
        return pts.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointsToSet that = (PointsToSet) o;
        return Objects.equals(pts, that.pts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pts);
    }
}
