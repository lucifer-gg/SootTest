package analysis;

import elem.Method;
import elem.Obj;

public class HeapModel {
    //这个类模拟堆的作用，传入分配点的标识，所在方法，type（没用）就可以创建一个obj对象，来模拟指针指向的对象

    private static HeapModel instance;

    private HeapModel() {  }

    public static HeapModel v() {
        if (instance == null) {
            instance = new HeapModel();
        }
        return instance;
    }

    /**
     * @param allocSite 分配点的标识
     * @param type 默认为 allocation-site abstraction, 这里不会用到它的值
     * @param m 分配点所在的方法
     * @return 从当前堆模型中获取 抽象对象
     */

    public Obj getObj(Object allocSite, String type, Method m) {
        return new Obj(allocSite, type, m);
    }
}
