import java.util.ArrayList;
import java.util.List;

public class Controller {

    public boolean LoginController(){
        String adminString="admin";
        String passwordString="123456";
        LoginParam loginParam=new LoginParam("admin","123456");
        String password = loginParam.getPassword();
        String userName = loginParam.getUserName();
        String p2=loginParam.getPassword();
        Dao dao=new Dao();
        dao.checkPassword("admin",p2);
        Service service=new Service();
        String test = Test.test(password);
        dao.checkPassword("a",test);
        return service.login(userName,password);

    }

    public boolean testAA(){
        A a=new A();
        B b=new B();
        a.load="ad";
        b.loadB=a.load;

        String arr[]=new String[5];
        arr[0]="aa";
        arr[1]="bb";
        return true;

    }
}
