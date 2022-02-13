package analysis;

import soot.*;
import soot.jimple.Stmt;

import java.util.*;

public class JimpleCallGraph {
    //这个里面存所有参与分析的方法
    //所有可达方法
    //所有方法在调用图中伸出去的边（相当于出度）
    //所有到当前方法的边（相当于入度）
    //最后一个存的是一个method里面的语句到这个method的映射，也就是说通过这个map可以找出所有的语句它属于哪个method

    private Collection<SootMethod> entries;

    private Set<SootMethod> reachableMethods = new HashSet<>();

    private Map<SootMethod, Set<CallEdge>> callee2caller = new HashMap<>();

    private Map<SootMethod, Set<CallEdge>> caller2callee = new HashMap<>();

    private Map<Unit, SootMethod> unit2Owner = new HashMap<>();

    public JimpleCallGraph() {
        //解析代码构造自定义的jimplegraph
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            for (SootMethod method : clazz.getMethods()) {
                if (!method.isConcrete()) {
                    continue;
                }
                Body body = method.retrieveActiveBody();
                if (body != null) {
                    for (Unit unit : body.getUnits()) {
                        unit2Owner.put(unit, method);
                    }
                }
            }
        }
    }

    /**
     * @return 返回被分析程序的entry方法，作业中只需返回main方法即可
     */
    public Collection<SootMethod> getEntryMethods() {
        if (entries != null) {
            return entries;
        }
        entries = new LinkedList<>();
        for (SootClass clazz : Scene.v().getApplicationClasses()) {
            for (SootMethod method : clazz.getMethods()) {
                if ("main".equals(method.getName())) {
                    entries.add(method);
                }
            }
        }
        // 初始情况下，entry methods总是可达的
        reachableMethods.addAll(entries);
        return entries;
    }

    /**
     * @param method
     * @return 返回给定方法内的的所有调用点
     */
    public Collection<Unit> getCallSiteIn(SootMethod method) {
        List<Unit> callSites = new LinkedList<>();
        if (method.hasActiveBody()) {
            Body body = method.getActiveBody();
            for (Unit unit : body.getUnits()) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    callSites.add(stmt);
                }
            }
        }
        return callSites;
    }

    /**
     * @param method
     * @return 返回是否给定method在调用图上是可达的；entry Method总是可达的
     */
    public boolean contains(SootMethod method) {
        return reachableMethods.contains(method);
    }

    /**
     * 是否调用边在CallGraph中存在
     * @param callSite 调用点
     * @param callee 被调用函数
     * @return true, 如果存在
     */
    public boolean contains(Unit callSite, SootMethod callee) {
        Set<CallEdge> callEdges = getCallInOf(callee);
        for (CallEdge callEdge : callEdges) {
            if (callEdge.getCallSite() == callSite) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加一条调用遍到调用图中
     * @param callSite 调用点
     * @param callee 被调用method
     * @param callKind 调用类型
     * @return
     */
    //边本来就是调用点到方法的边
    public boolean addEdge(Unit callSite, SootMethod callee, CallKind callKind) {
        // 维护 Reachable Methods
        reachableMethods.add(callee);

        CallEdge callEdge = new CallEdge(callKind, callSite, callee);

        // 维护两个表

        Set<CallEdge> callers = callee2caller.computeIfAbsent(callee, k -> new HashSet<>());
        boolean ret = callers.add(callEdge);

        SootMethod caller = unit2Owner.get(callSite);
        Set<CallEdge> callees = caller2callee.computeIfAbsent(caller, k -> new HashSet<>());
        callees.add(callEdge);

        return ret;
    }

    /**
     * @param method
     * @return 返回method调用出去的边
     */
    public Set<CallEdge> getCallOutOf(SootMethod method) {
        Set<CallEdge> result = caller2callee.computeIfAbsent(method, k -> new HashSet<>());
        return Collections.unmodifiableSet(result);
    }

    /**
     * @param method
     * @return 返回method调用进来的边
     */
    public Set<CallEdge> getCallInOf(SootMethod method) {
        Set<CallEdge> result = callee2caller.computeIfAbsent(method, k -> new HashSet<>());
        return Collections.unmodifiableSet(result);
    }
}

