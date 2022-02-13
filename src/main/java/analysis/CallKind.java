package analysis;

import soot.Unit;
import soot.jimple.*;

public enum CallKind {
    //存放调用关系的枚举类

    //枚举类，代表调用的种类
    INTERFACE("invokeinterface"),
    VIRTUAL("invokevirtual"),
    SPECIAL("invokespecial"),
    STATIC("invokestatic");

    private String inst;

    CallKind(String inst) {
        this.inst = inst;
    }

    /**
     * @param unit
     * @return 返回给定Unit的调用类型
     * @throws IllegalArgumentException 如果Unit不存在函数调用，那么抛出异常
     */
    public static CallKind getCallKind(Unit unit) throws IllegalArgumentException {
        InvokeExpr invoke = ((Stmt) unit).getInvokeExpr();
        if (invoke instanceof InterfaceInvokeExpr) {
            return INTERFACE;
        }
        if (invoke instanceof VirtualInvokeExpr) {
            return VIRTUAL;
        }
        if (invoke instanceof SpecialInvokeExpr) {
            return SPECIAL;
        }
        if (invoke instanceof StaticInvokeExpr) {
            return STATIC;
        }
        throw new IllegalArgumentException(invoke.toString());
    }

    @Override
    public String toString() {
        return inst;
    }
}
