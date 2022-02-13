package stmt;

import elem.Variable;

import java.util.Objects;

public class Assign extends Statement {
    //赋值语句，x=y

    private Variable from;

    private Variable to;

    public Assign(Variable from, Variable to) {
        this.from = from;
        this.to = to;
    }

    /**
     * @return 赋值右侧
     */
    public Variable getFrom() {
        return from;
    }

    /**
     * @return 赋值左侧
     */
    public Variable getTo() {
        return to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assign assign = (Assign) o;
        return Objects.equals(from, assign.from) && Objects.equals(to, assign.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + from + " = " + to;
    }
}
