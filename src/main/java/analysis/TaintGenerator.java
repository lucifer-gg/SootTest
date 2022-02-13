package analysis;

import elem.CallSite;
import elem.Method;
import elem.Obj;
import elem.TaintObj;

public class TaintGenerator {
    public static TaintObj getTaintObj(Method sourceMethod, CallSite callSite,Obj object) {
        return new TaintObj(sourceMethod,callSite,object);
    }
}
