public class Service {

    public boolean login(String userName,String password){
        String name=userName;
        String pa=password;
        Dao dao=new Dao();
        return dao.checkPassword(name,pa);

    }
}
