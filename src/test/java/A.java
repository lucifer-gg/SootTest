import soot.jimple.StaticFieldRef;

public class A {
    public String load;
    public static String staticLoadA;
    public static B b=new B();
    public static String foo(){
        int a=1;
        a+=1;
        String res=a+"";
        return res;
    }
}
