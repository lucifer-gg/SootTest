package stmt;

import elem.CallSite;

import java.util.Objects;

public class Call extends Statement {
    //调用语句，属性就是一个调用点

    private CallSite callSite;

    public Call(CallSite callSite) {
        this.callSite = callSite;
    }

    public CallSite getCallSite() {
        return callSite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Call call = (Call) o;
        return Objects.equals(callSite, call.callSite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(callSite);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + callSite.toString();
    }
}

