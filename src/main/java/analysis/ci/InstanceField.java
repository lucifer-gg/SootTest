package analysis.ci;

import elem.Field;
import elem.Obj;

import java.util.Objects;

public class InstanceField extends Pointer {
    //这个类代表的是pfg里面的oi.f这一类节点，所以需要有一个obj，一个field
    //然后它集成了Pointer，所以他里面其实也有他所指向的对象的集合。

    private Obj o;

    private Field f;

    public InstanceField(Obj o, Field f) {
        this.o = o;
        this.f = f;
    }

    /**
     * @return 返回PFG上该指针节点对应的字段的基对象(Base Object)
     */
    public Obj getBase() {
        return o;
    }

    /**
     * @return 返回PFG上该指针节点对应的字段
     */
    public Field getField() {
        return f;
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        InstanceField that = (InstanceField) o1;
        return Objects.equals(o, that.o) && Objects.equals(f, that.f);
    }

    @Override
    public int hashCode() {
        return Objects.hash(o, f);
    }

    @Override
    public String toString() {
        return "(" + o + ")" + "." + f;
    }
}
