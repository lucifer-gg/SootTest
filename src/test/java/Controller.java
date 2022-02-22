import java.util.ArrayList;
import java.util.List;

public class Controller extends father{

    public static void main(String[] args) {
        Controller controller=new Controller();
        controller.LoginController();
    }

    public boolean LoginController(){


        String adminString="admin";
        String passwordString="123456";
        LoginParam loginParam=new LoginParam("admin","123456");
        String password = loginParam.getPassword();
        String userName = loginParam.getUserName();



        String p3=super.passFromFather(password);


        String p2=loginParam.getPassword();
        Dao dao=new Dao();
        dao.checkPassword("admin",p2);
        Service service=new Service();

        service.login("a1",p3);
        return service.login(userName,password);

    }

    public boolean testAA(){
        String s1="aa";
        String p3=passFromFather(s1);
        return p3==null;

    }
//
//    private boolean aaa(){
//        return false;
//    }

    private String testPrivate(String input){
        String inner=input;
        return inner;
    }

}
