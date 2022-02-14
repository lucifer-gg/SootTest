package elem;

import soot.SootField;

import java.util.HashMap;
import java.util.Map;

public class Globals {
    //这个类是为了存放所有的局部变量而存在的
    private static Map<SootField,GlobalVariable> map=new HashMap<>();

    public static GlobalVariable getGlobalVariableFromSootField(SootField sootField){
        return map.computeIfAbsent(sootField, k -> new GlobalVariable(sootField));
    }
}
