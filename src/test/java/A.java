import soot.jimple.StaticFieldRef;

public class A extends father{
    public String load;
    public static String staticLoadA;
    public static B b=new B();
    public static String foo(){
        int a=1;
        a+=1;
        String res=a+"";
        return res;
    }
    public A(String load){
        this.load=load;
    }

    public String getLoad() {
        return load;
    }
}
