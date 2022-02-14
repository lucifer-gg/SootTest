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

        for(int i=0;i<5;i++){
            arr[i]=password;
        }
        String p4="";
        for(int i=0;i<5;i++){
            p4=arr[i];
        }

        String p2=loginParam.getPassword();
        Dao dao=new Dao();
        dao.checkPassword("admin",p2);
        Service service=new Service();

        dao.checkPassword("bb",p4);
        return service.login(userName,password);

    }

//    public boolean testAA(){
//        String arr[]=new String[5];
//        String adminString="admin";
//        String passwordString="123456";
//        LoginParam loginParam=new LoginParam("admin","123456");
//        String password = loginParam.getPassword();
//        String userName = loginParam.getUserName();
//        Test.pass=password;
//        String p3=Test.pass;
//
//
//
//
//
//        String p2=loginParam.getPassword();
//        Dao dao=new Dao();
//        dao.checkPassword("admin",p2);
//        Service service=new Service();
//
//
//        dao.checkPassword("aa",p3);
//        return service.login(userName,password);
//    }

}
