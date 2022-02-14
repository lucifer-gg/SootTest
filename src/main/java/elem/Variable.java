package elem;

import soot.Local;
import stmt.Call;
import stmt.InstanceLoad;
import stmt.InstanceStore;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Variable {
    //这个代表变量，不包括域
    //包含method用来记录是哪个方法
    //local记录方法里的哪个局部变量
    //三个set记录和这个语句相关的三类语句
    //这三个在method的initialize里面被填充


    private final Local local;

    private final Method method;

    private final Set<InstanceStore> stores;

    private final Set<InstanceLoad> loads;

    private final Set<Call> calls;

    public Variable(Local local, Method method) {
        this.local = local;
        this.method = method;
        this.stores = new LinkedHashSet<>();
        this.loads = new LinkedHashSet<>();
        this.calls = new LinkedHashSet<>();
    }

    /**
     * @return 返回函数内对当前变量的store语句
     */
    public Set<InstanceStore> getStores() {
        return stores;
    }

    /**
     * @return 返回函数内对当前变量的load语句
     */
    public Set<InstanceLoad> getLoads() {
        return loads;
    }

    public Set<Call> getCalls() {
        return calls;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(local, variable.local);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local);
    }

    @Override
    public String toString() {
        return toUniqueString();
    }

    public String toUniqueString() {
        if(this instanceof GlobalVariable){
            return ((GlobalVariable) this).toString();
        }
        return method.getSootMethod().getSignature() + ": " + local.getName();
    }
}
