package stmt;

import elem.Method;

public class Statement {
    //所有类型语句的父接口，其实就是记录了哪个method的哪一行


    private Method enclosingMethod;

    private int line = -1;

    public void setEnclosingMethod(Method method) {
        this.enclosingMethod = method;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        if (enclosingMethod != null) {
            buff.append(enclosingMethod.getSootMethod().getSignature());
        }
        if (line >= 0) {
            buff.append("@").append(line);
        }
        return buff.toString();
    }
}
