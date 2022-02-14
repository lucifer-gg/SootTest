package elem;

import soot.jimple.Stmt;

import java.util.Objects;

public class CallSite {
    //对调用点的封装，分别是具体的调用语句，涉及的接收者以及返回值的接收者

    private final Stmt callSite;
    private final Variable receiver;
    private final Variable ret;

    public CallSite(){
        this(null);
    }

    public CallSite(Stmt callSite) {
        this(callSite, null);
    }

    public CallSite(Stmt callSite, Variable receiver) {
        this(callSite, receiver, null);
    }

    /**
     * @param callSite
     * @param receiver receiver object
     * @param ret return object
     */
    public CallSite(Stmt callSite, Variable receiver, Variable ret) {
        this.callSite = callSite;
        this.receiver = receiver;
        this.ret = ret;
    }

    public Stmt getCallSite() {
        return callSite;
    }

    public Variable getReceiver() {
        return receiver;
    }

    public Variable getRet() {
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CallSite callSite1 = (CallSite) o;
        return Objects.equals(callSite, callSite1.callSite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callSite);
    }

    @Override
    public String toString() {
        return callSite.toString();
    }
}
