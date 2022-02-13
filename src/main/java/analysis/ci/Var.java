package analysis.ci;

import elem.Variable;

import java.util.Objects;

public class Var extends Pointer {
    //这个代表的就是PFG图上的x这种变量的节点
    //由于也继承了Pointer，所以里面也有这个指针所指向的对象的集合。

    private Variable var;

    public Var(Variable var) {
        this.var = var;
    }

    /**
     * @return 返回PFG上该指针节点对应的变量
     */
    public Variable getVariable() {
        return var;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Var var1 = (Var) o;
        return Objects.equals(var, var1.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var);
    }

    @Override
    public String toString() {
        return var.toUniqueString();
    }
}
