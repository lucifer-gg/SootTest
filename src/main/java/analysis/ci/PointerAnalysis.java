package analysis.ci;

import analysis.CallKind;
import analysis.HeapModel;
import analysis.JimpleCallGraph;
import analysis.TaintGenerator;
import elem.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.scalar.Pair;
import stmt.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PointerAnalysis {

    /**
     * 堆模型
     */
    protected HeapModel heapModel;

    protected SootMethod sootMethod;
    protected Method entry;

    protected WorkList WL;

    protected PointerFlowGraph PFG;

    //所有语句
    protected Set<Statement> S;

    //可达方法
    protected Set<Method> RM;

    protected JimpleCallGraph CG;

    protected Set<SootMethod> SOURCE;

    protected Set<SootMethod> SINK;

    public PointerAnalysis(SootMethod entry,Set<SootMethod> source,Set<SootMethod> sink) {
        //入口方法
        this.sootMethod = entry;
        this.heapModel = HeapModel.v();
        this.SOURCE=source;
        this.SINK=sink;
    }

    /**
     * 开始指针分析算法
     */
    public void solve() {
        initialize();
        analysis();
    }

    /**
     * 实现指针分析算法(Lecture 10, Page 115)的前两行;
     * 初始化各种数据结构，entry methods
     */
    protected void initialize() {
        WL = new WorkList();
        PFG = new PointerFlowGraph();
        S = new LinkedHashSet<>();
        RM = new LinkedHashSet<>();
        CG = new JimpleCallGraph();
        entry = new Method(sootMethod);

        //1-第一步
        addReachable(entry);
        //待处理，入口是普通方法，调用本对象内的其他普通方法还是会出错
        //主要是由于入口方法的this没有分配点造成的
        //可以考虑构造一个静态方法作为入口，避免这个问题
        //构造是自动还是其他过程再说。
        //如果这样处理下面的处理就是不必要的
//        if(!entry.getSootMethod().isStatic()){
//            //不是静态方法，要给this赋予一个初始对象
//            //否则在入口方法内的this相关的方法（私有方法，父类方法）不会被调用
//            WL.addPointerEntry(PFG.getVar(entry.getThisVariable()),PointsToSet.singleton(new Obj()));
//        }

    }

    /**
     * 实现指针分析中的 WorkList 处理主循环
     */
    private void analysis() {
        while (!WL.isEmpty()) {
            Pair<Pointer, PointsToSet> entry = WL.remove();

            // propagate
            Pointer n = entry.getO1();
            PointsToSet pts = entry.getO2();
            //2-传播
            //这里在propagate中获取delta
            PointsToSet delta = propagate(n, pts);

            //如果不是var不管他

            if (!(n instanceof Var)) {
                continue;
            }
            //而且尝试得出如果var里面的variable实际上是静态变量，那么是不会有Store和load语句的，也不会有call语句
            if(((Var) n).getVariable() instanceof GlobalVariable){
                continue;
            }

            // now, n represents a variable x
            Var x = (Var) n;

            //处理x.f = y这种情况
            processInstanceStore(x, delta);

            //处理y = x.f
            processInstanceLoad(x, delta);

            //处理y=x.foo(a,b)这种情况
            processCall(x, delta);
        }
    }


    //如果m没有访问过把他加到RM里
    //把语句全部加进S里
    //然后处理new和assign语句
    //处理方法见笔记
    protected void addReachable(Method m) {
        if (!RM.contains(m)) {
            RM.add(m);
            Set<Statement> S_m = m.getStatements();
            S.addAll(S_m);

            // 处理new语句
            processAllocations(m);

            // 处理assign
            processLocalAssign(m);

            //staticCall啥的我觉得应该是在这处理的
            //而且处理方式和processCall应该是类似的，只是不需要处理dispatch而已
            processStaticCalls(m);


        }
    }

    protected void processStaticCalls(Method methodInput){
        for (StaticCallSite staticCallSite:methodInput.getStaticCalls()) {
            //静态方法
            StaticInvokeExpr staticInvokeExpr=staticCallSite.getStaticInvokeExpr();
            SootMethod staticMethod = staticInvokeExpr.getMethod();

            Method method = null;
            for (Method m : RM) {
                if (m.getSootMethod() == staticMethod) {
                    method = m;
                    break;
                }
            }

            if (method == null) {
                method = new Method(staticMethod);
            }

            if(SOURCE.contains(method.getSootMethod())){
                Variable ret=staticCallSite.getRet();
                if(ret!=null){
                    WL.addPointerEntry(PFG.getVar(ret), PointsToSet.singleton(TaintGenerator.getTaintObj(method,staticCallSite)));
                }
            }

            if (!CG.contains(staticCallSite.getCallSite(), method.getSootMethod())) {
                // add l -> m to CG
                CG.addEdge(staticCallSite.getCallSite(), method.getSootMethod(), CallKind.getCallKind(staticCallSite.getCallSite()));

                addReachable(method);

                // foreach parameter p_i of m do
                //   AddEdge(a_i, p_i)
                //建立参数间的对照关系
                StaticInvokeExpr invoke=staticCallSite.getStaticInvokeExpr();
                for (int i = 0; i < method.getParams().size(); i++) {
                    //这里记得处理直接传string的情况
                    Value arg1 = invoke.getArg(i);
                    if(arg1 instanceof StringConstant){
                        StringConstant stringConstant=(StringConstant)arg1;
                        Local arg=new JimpleLocal(stringConstant.toString()+invoke.toString(),stringConstant.getType());
                        //pi
                        Variable argVariable = methodInput.getVariable(arg);
                        //ai
                        Variable paramVariable = method.getParams().get(i);
                        //建立边关系
                        addPFGEdge(PFG.getVar(argVariable), PFG.getVar(paramVariable));
                    }else {
                        Local arg = (Local) invoke.getArg(i);
                        //pi
                        Variable argVariable = methodInput.getVariable(arg);
                        //ai
                        Variable paramVariable = method.getParams().get(i);
                        //建立边关系
                        addPFGEdge(PFG.getVar(argVariable), PFG.getVar(paramVariable));
                    }
                }

                // AddEdge(m_ret, r)
                //就是r
                //建立返回值的对照关系
                Variable callerRetVar = staticCallSite.getRet();
                if (callerRetVar != null) {
                    List<Variable> calleeRetVariableList = method.getRetVariable();
                    for (Variable calleeRetVar : calleeRetVariableList) {
                        addPFGEdge(PFG.getVar(calleeRetVar), PFG.getVar(callerRetVar));
                    }
                }
            }
        }
        //还需要处理另外两种call的情形。
    }

    /**
     * 这个方法实现集合的差运算: delta = pts - pts(n)
     * 另外还实现了Lecture 9, Page 43的 propagate(p, pts) 函数
     *
     * 这里合并了两个步骤到一个方法里面，用于降低冗余的计算
     * @param n
     * @param pts
     * @return pts - pts(n)
     */

    //传播方法
    //就是把pts加到pt（n）中，然后遍历n所有的后继节点x，将x，pts加到wl中
    //注意这个方法把笔记中的计算delta的过程合并进了这个方法里，最后返回供后面使用
    protected PointsToSet propagate(Pointer n, PointsToSet pts) {
        PointsToSet ptsOfn = n.getPointsToSet();//pt（n）
        PointsToSet delta = PointsToSet.difference(pts, ptsOfn);//pt（n）和pts的差，就是delta
        if (!delta.isEmpty()) {
            ptsOfn.union(delta);

            // 对于n的后继，delta是新加到pt（n）中的，所以delta也要传播到n的后继中，所以构造pair放到wl中
            PointsToSet normalObj=new PointsToSet();
            PointsToSet taintObj=new PointsToSet();
            for(Obj obj:delta){
                if(obj instanceof TaintObj){
                    taintObj.addObject(obj);
                }else {
                    normalObj.addObject(obj);
                }
            }
            for (Pointer s : PFG.getSuccessorOf(n)) {
                WL.addPointerEntry(s, normalObj);
            }
            if (!taintObj.isEmpty()){
                PointsToSet cloneTaintObjs=new PointsToSet();
                for (Obj taint:taintObj){
                    TaintObj t=(TaintObj) taint;
                    TaintObj clone = t.clone();
                    clone.path.add(n);
                    cloneTaintObjs.addObject(clone);
                }

                for (Pointer s : PFG.getSuccessorOf(n)) {
                    WL.addPointerEntry(s, cloneTaintObjs);
                }

            }

        }
        return delta;
    }

    //这个其实就是现在PFG中加边，然后把t，pt(s)加入到wl中
    protected void addPFGEdge(Pointer s, Pointer t) {
        boolean add = PFG.addEdge(s, t);
        //pt（s）如果不是null，就把pt（s）全部传播到pt（t）中，即添加到待办列表
        if (add) {
            PointsToSet pointsToSetOfS = s.getPointsToSet();
            PointsToSet normalObj=new PointsToSet();
            PointsToSet taintObj=new PointsToSet();
            for(Obj obj:pointsToSetOfS){
                if(obj instanceof TaintObj){
                    taintObj.addObject(obj);
                }else {
                    normalObj.addObject(obj);
                }
            }

            WL.addPointerEntry(t, normalObj);

            if (!taintObj.isEmpty()){
                PointsToSet cloneTaintObjs=new PointsToSet();
                for (Obj taint:taintObj){
                    TaintObj tt=(TaintObj) taint;
                    TaintObj clone = tt.clone();
                    clone.path.add(s);
                    cloneTaintObjs.addObject(clone);
                }
                WL.addPointerEntry(t, cloneTaintObjs);
            }
        }
    }

    //它实现了一个heapModle用于模拟堆
    //这个处理new语句其实就是在堆上创建一个对象，然后将x，oi存进workList中
    protected void processAllocations(Method m) {
        Set<Statement> S_m = m.getStatements();
        S_m.stream()
                .filter(s -> s instanceof Allocation)
                .forEach(s -> {
                    Allocation i = (Allocation) s;
                    Obj o = heapModel.getObj(i.getAllocationSite(), i.getType(), m);
                    WL.addPointerEntry(PFG.getVar(i.getVar()), PointsToSet.singleton(o));
                });
    }

    /**
     * 处理局部变量的赋值；
     * 如: x = y
     *
     * Lecture 10, Page 118 的 AddReachable函数的第二个foreach循环
     * @param m
     */
    //处理赋值语句x=y其实就是在x，y中间加一条边
    //加边的方法在addPFGEdge中
    protected void processLocalAssign(Method m) {
        Set<Statement> S_m = m.getStatements();
        S_m.stream()
                .filter(s -> s instanceof Assign)
                .forEach(s -> {
                    // x = y
                    Assign assign = (Assign) s;
                    // y -> x
                    addPFGEdge(PFG.getVar(assign.getFrom()), PFG.getVar(assign.getTo()));
                });
    }

    /**
     * 处理字段的写语句;
     * 如: x.f = y
     *
     * Lecture 10, Page 124 Solve函数中处理store语句的foreach循环
     * @param var pts改变的指针节点, 对应的指针变量为x
     * @param pts 改变的部分delta
     */
    //按照算法其实就是把y对应的节点指向 delta中对象的f
    protected void processInstanceStore(Var var, PointsToSet pts) {
        for (Obj o_i : pts) {
            if(o_i instanceof TaintObj)continue;
            //找到所有的x.f=y的语句
            Set<InstanceStore> stores = var.getVariable().getStores();
            // x.f = y
            for (InstanceStore store : stores) {
                // y -> o_i.f
                addPFGEdge(PFG.getVar(store.getFrom()), PFG.getInstanceField(o_i, store.getField()));
            }
        }
    }

    /**
     * 处理字段读语句;
     * 如: y = x.f
     *
     * Lecture 10, Page 124 Solve函数中处理load语句的foreach循环
     * @param var pts改变的指针节点, 对应的指针变量为x
     * @param pts 改变的部分delta
     */
    //道理同上，先找到所有的y=x.f这个形式的语句的为止，根据语句获取y，oi.f，然后添加一条边
    protected void processInstanceLoad(Var var, PointsToSet pts) {
        for (Obj o_i : pts) {
            if(o_i instanceof TaintObj)continue;
            Set<InstanceLoad> loads = var.getVariable().getLoads();
            // y = x.f
            for (InstanceLoad load : loads) {
                // o_i.f -> y
                addPFGEdge(PFG.getInstanceField(o_i, load.getField()), PFG.getVar(load.getTo()));
            }
        }
    }

    /**
     * 处理函数调用，型如：l: r = var.k(a1, ..., an)
     * @param var
     * @param pts
     */
    //这个地方需要处理Source
    protected void processCall(Var var, PointsToSet pts) {
        //还需要处理另外两种call的情形。


        Method curMethod = var.getVariable().getMethod();

        for (Obj o_i : pts) {
            //taintObj仅仅作为标记存在，他不应该被处理
            if(o_i instanceof TaintObj)continue;
            //首先找到调用点
            Set<Call> calls = var.getVariable().getCalls();
            for (Call call : calls) {
                // r = var.k(a1, ..., an)
                CallSite callSite = call.getCallSite();
                // m = Dispatch(o_i, k)

                Method m=null;
                if(callSite.getCallSite().getInvokeExpr() instanceof SpecialInvokeExpr){
                    //specialInvoke不需要分派
                    m=dispatchForSpecialCall(callSite);
                }else {
                    //分派到具体方法
                    //对于每个oi和调用点进行分派，确定方法
                    m = dispatch(o_i, callSite);
                }


                // add <m_this, {o_i}> to WL
                //把m的this变量加入到wl中
                WL.addPointerEntry(PFG.getVar(m.getThisVariable()), PointsToSet.singleton(o_i));

                if(SOURCE.contains(m.getSootMethod())){
                    Variable ret = callSite.getRet();
                    if(ret!=null){
                        WL.addPointerEntry(PFG.getVar(ret), PointsToSet.singleton(TaintGenerator.getTaintObj(m,callSite,o_i)));
                    }
                }

                // if l -> m not in CG
                //建立callsite到m的边
                if (!CG.contains(callSite.getCallSite(), m.getSootMethod())) {
                    // add l -> m to CG
                    CG.addEdge(callSite.getCallSite(), m.getSootMethod(), CallKind.getCallKind(callSite.getCallSite()));

                    addReachable(m);

                    // foreach parameter p_i of m do
                    //   AddEdge(a_i, p_i)
                    //建立参数间的对照关系
                    InvokeExpr invoke = callSite.getCallSite().getInvokeExpr();
                    for (int i = 0; i < m.getParams().size(); i++) {
                        //这里记得处理直接传string的情况
                        Value arg1 = invoke.getArg(i);
                        if(arg1 instanceof StringConstant){
                            StringConstant stringConstant=(StringConstant)arg1;
                            Local arg=new JimpleLocal(stringConstant.toString()+invoke.toString(),stringConstant.getType());
                            //pi
                            Variable argVariable = curMethod.getVariable(arg);
                            //ai
                            Variable paramVariable = m.getParams().get(i);
                            //建立边关系
                            addPFGEdge(PFG.getVar(argVariable), PFG.getVar(paramVariable));
                        }else {
                            Local arg = (Local) invoke.getArg(i);
                            //pi
                            Variable argVariable = curMethod.getVariable(arg);
                            //ai
                            Variable paramVariable = m.getParams().get(i);
                            //建立边关系
                            addPFGEdge(PFG.getVar(argVariable), PFG.getVar(paramVariable));
                        }
                    }

                    // AddEdge(m_ret, r)
                    //就是r
                    //建立返回值的对照关系
                    Variable callerRetVar = callSite.getRet();
                    if (callerRetVar != null) {
                        List<Variable> calleeRetVariableList = m.getRetVariable();
                        for (Variable calleeRetVar : calleeRetVariableList) {
                            addPFGEdge(PFG.getVar(calleeRetVar), PFG.getVar(callerRetVar));
                        }
                    }
                }
            }
        }
    }

    protected Method dispatchForSpecialCall(CallSite callSite){
        SootMethod sootMethod=callSite.getCallSite().getInvokeExpr().getMethod();
        Method method = null;
        for (Method m : RM) {
            if (m.getSootMethod() == sootMethod) {
                method = m;
                break;
            }
        }

        if (method == null) {
            method = new Method(sootMethod);
        }
        return method;

    }

    //根据对象和调用点分派
    //这个就是先获取o的实际类型，然后在这个类里面找有没有方法的签名（方法名，返回值，参数类型）一样，如果有就返回这个方法，如果没有就去父类中找
    protected Method dispatch(Obj o, CallSite callSite) {
        AssignStmt assignStmt = (AssignStmt) o.getAllocSite();
        NewExpr newExpr = (NewExpr) assignStmt.getRightOp();
        RefType refType = (RefType) newExpr.getType();

        //就是获取当前对象o的创建点T o=new T（）；，然后看他创建语句右边的T到底是什么类型
        SootClass sootClass = refType.getSootClass();

        InvokeExpr invokeExpr = callSite.getCallSite().getInvokeExpr();
        //获取调用点是调用了哪个方法
        SootMethod sootMethod = invokeExpr.getMethod();
        //然后用之前的分派方式分派
        SootMethod dispatch = dispatch(sootClass, sootMethod);

        //返回值是一个sootmethod，看看这个方法之前有没有被加入到RM里面，没有加封装并返回
        Method method = null;
        for (Method m : RM) {
            if (m.getSootMethod() == dispatch) {
                method = m;
                break;
            }
        }

        if (method == null) {
            method = new Method(dispatch);
        }
        return method;
    }

    /**
     */
    //这个方法就是之前的dispatch方法，先匹配本类中的方法是否有符合的，没有去找父类
    //fix:返回值也应该参与比较
    //此方法修改为返回值也参与比较，且参数全匹配才行
    //
    private SootMethod dispatch(SootClass sootClass, SootMethod method) {
        for (SootMethod m : sootClass.getMethods()) {
            if (!m.isAbstract()) {
                if (m.getName().equals(method.getName())
                        && m.getParameterCount() == method.getParameterCount()) {
                    // 没有参数列表，那么直接匹配到了
                    if (m.getParameterCount() == 0) {
                        Type returnTypeOfM = m.getReturnType();
                        Type returnTypeOfMethod = method.getReturnType();
                        if (returnTypeOfM.toQuotedString().equals(returnTypeOfMethod.toQuotedString())){
                            return m;
                        }else {
                            continue;
                        }
                    }
                    // 否则对比参数列表
                    //这里不应该所有的匹配才算匹配吗？
                    boolean tag=true;
                    for (int i = 0; i < m.getParameterCount(); i++) {
                        Type t = m.getParameterType(i);
                        Type t1 = method.getParameterType(i);
                        if (!t.toQuotedString().equals(t1.toQuotedString())) {
                            tag=false;
                        }
                    }
                    if(tag){
                        //每一个都匹配，根据返回值类型判断是否直接返回
                        Type returnTypeOfM = m.getReturnType();
                        Type returnTypeOfMethod = method.getReturnType();
                        if (returnTypeOfM.toQuotedString().equals(returnTypeOfMethod.toQuotedString())){
                            return m;
                        }
                    }
                }
            }
        }
        SootClass superClass = sootClass.getSuperclassUnsafe();
        if (superClass != null) {
            return dispatch(superClass, method);
        }
        return null;
    }

}
