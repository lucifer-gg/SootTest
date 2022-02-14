package elem;

import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;

import java.util.Objects;

public class StaticCallSite extends CallSite{

    private final Stmt callSite;
    private final Variable ret;
    private final StaticInvokeExpr staticInvokeExpr;
    public StaticCallSite(Stmt callSite,Variable ret,StaticInvokeExpr staticInvokeExpr){
        super();
        this.callSite=callSite;
        this.ret=ret;
        this.staticInvokeExpr=staticInvokeExpr;
    }

    public StaticCallSite(Stmt callSite,StaticInvokeExpr staticInvokeExpr){
        this(callSite,null,staticInvokeExpr);
    }

    public Stmt getCallSite() {
        return callSite;
    }

    public Variable getRet() {
        return ret;
    }

    public StaticInvokeExpr getStaticInvokeExpr() {
        return staticInvokeExpr;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaticCallSite staticCallSite=(StaticCallSite) o;
        return Objects.equals(callSite, staticCallSite.getCallSite());
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
