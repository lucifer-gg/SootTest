package stmt;

import elem.Field;
import elem.Variable;

import java.util.Objects;

public class InstanceStore extends Statement {
    //代笔x.f=y

    private Variable from;

    private Variable base;

    private Field field;

    public InstanceStore(Variable base, Field field, Variable from) {
        this.from = from;
        this.base = base;
        this.field = field;
    }

    /**
     * @return 赋值左侧字段的基变量(Base Variable)
     */
    public Variable getVariable() {
        return base;
    }

    /**
     * @return 赋值左侧的字段
     */
    public Field getField() {
        return field;
    }

    /**
     * @return 赋值右侧的变量
     */
    public Variable getFrom() {
        return from;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceStore store = (InstanceStore) o;
        return Objects.equals(from, store.from) && Objects.equals(base, store.base) && Objects.equals(field, store.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, base, field);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + base + "." + field + " = " + from;
    }
}
