package elem;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.tagkit.LineNumberTag;
import soot.tagkit.Tag;
import stmt.*;

import java.util.*;

public class Method {
    //对于对象方法的封装，里面包含了这个方法在soot里面的表示，以及被这个方法影响的stmt的集合，这个集合只包括下面注释中的五种。
    //每次将soot中的method传入封装的时候都会进行初始化操作，把这类语句提取出来，同时也会将这个方法中定义的本地变量封装成一个Variable变量，方便后面使用。
    //之后能从这个对象里面获取所有的本地变量对应的variable以及五种语句。

    private final SootMethod delegate;

    private Set<Statement> pointerAffectingStmt;

    private Map<Local, Variable> localMap;

    public Method(SootMethod sootMethod) {
        this.delegate = sootMethod;
        initialize();
    }

    /**
     * Translate SootMethod to ours 'pointer-affecting' IR
     *
     * 'pointer-affecting' statements:
     * 1. New      x = new T()
     * 2. Assign   x = y
     * 3. Store    x.f = y
     * 4. Load     y = x.f
     * 5. Call     r = x.k(arg, ...)
     *
     * Java语言中的指针有：
     * 1. Local Variable: x          <- 考虑
     * 2. Static field: C.f           <- 在本作业中我们不考虑(一般实现的话处理成全局变量)
     * 3. instance field: x.f         <- 考虑
     * 4. Array element: array[i]    <- 在本作业中我们不考虑(一般实现的话处理成array[i]抽象成array.index_field)
     */
    private void initialize() {
        pointerAffectingStmt = new LinkedHashSet<>();

        // 避免重复生成对象
        localMap = new HashMap<>();

        for (Unit unit : delegate.retrieveActiveBody().getUnits()) {
            Stmt stmt = (Stmt) unit;
            if (stmt instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) stmt;
                Value l = assignStmt.getLeftOp();
                Value r = assignStmt.getRightOp();
                // x = new T
                if (l instanceof Local && r instanceof NewExpr) {
                    Variable x = getVariable((Local) l);
                    Allocation alloc = new Allocation(x, assignStmt);
                    addPointerAffectingStmt(stmt, alloc);
                }
                // x = y
                if (l instanceof Local && r instanceof Local) {
                    Variable x = getVariable((Local) l);
                    Variable y = getVariable((Local) r);
                    Assign assign = new Assign(y, x);
                    addPointerAffectingStmt(stmt, assign);
                }
                // y = x.f
                if (l instanceof Local && r instanceof InstanceFieldRef) {
                    Variable y = getVariable((Local) l);
                    Variable x = getVariable((Local) ((InstanceFieldRef) r).getBase());
                    Field f = new Field((InstanceFieldRef) r);
                    InstanceLoad load = new InstanceLoad(y, x, f);
                    x.getLoads().add(load);
                    addPointerAffectingStmt(stmt, load);
                }
                // x.f = y
                if (l instanceof InstanceFieldRef && r instanceof Local) {
                    Variable x = getVariable((Local) ((InstanceFieldRef) l).getBase());
                    Field f = new Field((InstanceFieldRef) l);
                    Variable y = getVariable((Local) r);
                    InstanceStore store = new InstanceStore(x, f, y);
                    x.getStores().add(store);
                    addPointerAffectingStmt(stmt, store);
                }
                //x.f=y.f呢？
                //没有这种语句
                //处理静态变量



                //处理数组


            }
            if (stmt.containsInvokeExpr()) {
                CallSite callSite = null;
                Variable x = null;

                InvokeExpr invokeExpr = stmt.getInvokeExpr();
                if (invokeExpr instanceof InstanceInvokeExpr) {
                    x = getVariable((Local) ((InstanceInvokeExpr) invokeExpr).getBase());
                    if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getLeftOp() instanceof Local) {
                        // r = x.k(arg, ...)
                        Variable r = getVariable((Local) ((AssignStmt) stmt).
                                getLeftOp());
                        callSite = new CallSite(stmt, x, r);
                    } else {
                        // x.k(arg, ...)
                        callSite = new CallSite(stmt, x);
                    }
                } else if (invokeExpr instanceof StaticInvokeExpr) {
                    // ClassName.k(arg, ...)   : static call
                    callSite = new CallSite(stmt);
                }

                Call call = new Call(callSite);
                if (x != null) {
                    x.getCalls().add(call);
                }
                addPointerAffectingStmt(stmt, call);
            }
        }
    }

    //这一步是从soot的stmt里面获取行号，然后把他一起封装进我们自己的Statement对象里面
    private void addPointerAffectingStmt(Stmt stmt, Statement ir) {

        ir.setEnclosingMethod(this);
        Tag lineTag = stmt.getTag(LineNumberTag.IDENTIFIER);
        if (lineTag != null) {
            int line = Integer.parseInt(lineTag.toString());
            ir.setLine(line);
        }
        pointerAffectingStmt.add(ir);
    }

    public Variable getVariable(Local local) {
        return localMap.computeIfAbsent(local, k -> new Variable(local, this));
    }

    //遍历所有语句找出接收返回值的变量
    public List<Variable> getRetVariable() {
        List<Variable> variableList = new LinkedList<>();
        for (Unit unit : delegate.getActiveBody().getUnits()) {
            if (unit instanceof ReturnStmt) {
                ReturnStmt returnStmt = (ReturnStmt) unit;
                Value v = returnStmt.getOp();
                if (v instanceof Local) {
                    variableList.add(getVariable((Local) v));
                }
            }
        }
        return variableList;
    }

    //找出所有参数变量
    public List<Variable> getParams() {
        List<Variable> variableList = new LinkedList<>();
        List<Local> locals = delegate.getActiveBody().getParameterLocals();
        for (Local local : locals) {
            Variable variable = getVariable(local);
            variableList.add(variable);
        }
        return variableList;
    }

    //返回this
    public Variable getThisVariable() {
        Local thisLocal = delegate.getActiveBody().getThisLocal();
        return getVariable(thisLocal);
    }


    public Set<Statement> getStatements() {
        return pointerAffectingStmt;
    }

    public SootMethod getSootMethod() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Method method = (Method) o;
        return Objects.equals(delegate, method.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(delegate.getSignature()).append(" {\n");
        for (Statement stmt : pointerAffectingStmt) {
            buff.append("\t").append(stmt).append("\n");
        }
        buff.append("}");
        return buff.toString();
    }
}

