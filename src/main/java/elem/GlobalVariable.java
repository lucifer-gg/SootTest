package elem;

import soot.Local;
import soot.SootField;

import java.util.Objects;

public class GlobalVariable extends Variable{
    //这个其实用来代表全局变量，为了兼容之前的代码继承Variable，不过variable的属性（local，method）对于这个变量而言本身是没有意义的
    //反而是这个东西本身的field才是有意义的
    private SootField field;
    public GlobalVariable(Local local, Method method) {
        super(local, method);
    }
    public GlobalVariable(SootField sootField){
        super(null,null);
        this.field=sootField;
    }

    //重写equals方法，为了方便比较
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GlobalVariable that = (GlobalVariable) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public String toString() {
        return "GlobalVariable{" +
                "field=" + field +
                '}';
    }
}
