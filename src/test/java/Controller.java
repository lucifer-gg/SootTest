import java.util.ArrayList;
import java.util.List;

public class Controller {

    public boolean LoginController(){

        String arr[]=new String[5];

        String adminString="admin";
        String passwordString="123456";
        LoginParam loginParam=new LoginParam("admin","123456");
        String password = loginParam.getPassword();
        String userName = loginParam.getUserName();


        String p2=loginParam.getPassword();
        Dao dao=new Dao();
        dao.checkPassword("admin",p2);
        Service service=new Service();


        Test.pass=password;
        String p3=Test.getPass();

        service.login("a1",p3);
        return service.login(userName,password);

    }

    public boolean testAA(){
        String res=A.foo();
        return true;

    }

}
