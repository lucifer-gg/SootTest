public class Dao {
    public boolean checkPassword(String userName,String password){
        if("admin".equals(userName) && check(password))
            return true;
        else return false;
    }
    private boolean check(String password){
        if(Math.random()<0.5)return true;else return false;
    };
}
