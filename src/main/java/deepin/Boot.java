package deepin;

/**
 * @Auther:yhy
 * @Created:2019/10/24 17:48
 * @Description:
 */
public class Boot {
    public static void main(String[] args){
        try {
            new AnalyseClass().test_load(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
