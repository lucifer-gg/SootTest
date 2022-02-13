package analysis.ci;

import elem.Field;
import elem.Obj;
import elem.Variable;
import soot.toolkits.scalar.Pair;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class PointerFlowGraph {
    //这个东西就是最终的PFG图
    //由于Var，InstanceField是我们对于变量以及域在PFG图中表示的封装，所以要有两个map来存封装和不封装的对应关系。
    //还有一个map就存一个指针和它后继节点的对应关系。

    protected Map<Variable, Var> varMap = new HashMap<>();

    protected Map<Pair<Obj, Field>, InstanceField> instanceFieldMap = new HashMap<>();

    protected Map<Pointer, Set<Pointer>> node2Succes = new HashMap<>();

    /**
     * @param variable
     * @return 返回给定变量所关联的PFG上的Var指针节点
     */
    public Var getVar(Variable variable) {
        Var var;
        if ((var = varMap.get(variable)) == null) {
            var = new Var(variable);
            varMap.put(variable, var);
        }
        return var;
    }

    /**
     * @param obj
     * @param field
     * @return 返回给定对象+字段所关联的PFG上的InstanceField指针节点
     */
    public InstanceField getInstanceField(Obj obj, Field field) {
        InstanceField ret;
        Pair<Obj, Field> pair = new Pair<>(obj, field);
        if ((ret = instanceFieldMap.get(pair)) == null) {
            ret = new InstanceField(obj, field);
        }
        return ret;
    }

    /**
     * 添加一条 s -> t 的边到当前PFG中
     * @param s 边的source
     * @param s 变得destination
     * @return 如果边早已经存在，返回false；否则返回true
     */
    public boolean addEdge(Pointer s, Pointer t) {
        //保存节点，并且添加一条边
        addIndex(s);
        addIndex(t);
        Set<Pointer> succes = node2Succes.computeIfAbsent(s, n -> new LinkedHashSet<>());
        return succes.add(t);
    }

    //其实就是把节点保存起来
    public void addIndex(Pointer p) {
        if (p instanceof Var) {
            Var var = (Var) p;
            Variable variable = var.getVariable();
            varMap.put(variable, var);
        } else if (p instanceof InstanceField) {
            InstanceField instanceField = (InstanceField) p;
            Obj obj = instanceField.getBase();
            Field field = instanceField.getField();
            Pair<Obj, Field> key = new Pair<>(obj, field);
            instanceFieldMap.put(key, instanceField);
        }
    }

    /**
     * @param pointer
     * @return 返回给定指针节点在PFG上的后继节点
     */
    public Set<Pointer> getSuccessorOf(Pointer pointer) {
        return node2Succes.computeIfAbsent(pointer, n -> new LinkedHashSet<>());
    }
}
