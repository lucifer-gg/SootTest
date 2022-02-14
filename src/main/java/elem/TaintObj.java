package elem;

import analysis.ci.Pointer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TaintObj extends Obj{

    public Method sourceMethod;
    public CallSite callSite;
    public Obj fatherObj;
    public List<Pointer> path;

    public TaintObj(Method sourceMethod,CallSite callSite,Obj obj) {
        super(obj.getAllocSite(),obj.getType(),obj.getMethod());
        this.sourceMethod=sourceMethod;
        this.callSite=callSite;
        this.path=new ArrayList<>();
        this.fatherObj=obj;
    }

    public TaintObj(Method sourceMethod,CallSite callSite){
        super();
        this.sourceMethod=sourceMethod;
        this.callSite=callSite;
        this.path=new ArrayList<>();
        this.fatherObj=null;

    }

    @Override
    public String toString() {

        if(callSite instanceof StaticCallSite){
            String s1="这是一个污点对象，来源方法是：+\n" +
                    ""+sourceMethod.toString()+"\n"+" " +
                    "产生时的调用点为：\n"
                    +callSite.toString()+"\n";

            s1+="到当前变量的传播路径为：\n";
            for (Pointer pointer:path){
                s1+=pointer.toString();
                s1+="\n";
                s1+="-->";
            }
            return s1;


        }

        String s1="这是一个污点对象，来源方法是：+\n" +
                ""+sourceMethod.toString()+"\n"+" " +
                "产生时的调用点为：\n"
                +callSite.getReceiver().getMethod().getSootMethod()+":"+callSite.toString()+"\n";
        s1+="到当前变量的传播路径为：\n";
        for (Pointer pointer:path){
            s1+=pointer.toString();
            s1+="\n";
            s1+="-->";
        }
        return s1;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaintObj obj = (TaintObj) o;

        return
                Objects.equals(sourceMethod,obj.sourceMethod)
                && Objects.equals(callSite,obj.callSite)
                && Objects.equals(path,obj.path);
    }

    public TaintObj clone(){
        if(this.fatherObj==null){
            TaintObj obj = new TaintObj(this.sourceMethod, this.callSite);
            obj.path = new ArrayList<>(this.path);
            return obj;
        }
        else {
            TaintObj obj = new TaintObj(this.sourceMethod, this.callSite, this.fatherObj);
            obj.path = new ArrayList<>(this.path);
            return obj;
        }
    }
}
