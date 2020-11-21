package deepin;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Auther:yhy
 * @Created:2019/10/23 14:14
 * @Description:常量池类型,字符串变长单独判断
 */
public enum ConstantInfo {
    UTF8("01",2,-1),//UTF8先获取utf8编码的长度，之后读取长度个字节
    INTEGER("03",4),
    FLOAT("04",4),
    LONG("05",8),
    DOUBLE("06",8),
    CLASS("07",2),
    STRING("08",2),
    FIELDREF("09",2,2),
    METHODREF("0A",2,2),
    INTERFACEMETHODREF("0B",2,2),
    NAMEANDTYPE("0C",2,2),
    METHODHANDLE("0F",1,2),
    METHODTYPE("10",2),
    INVOKEDYNAMIC("12",2,2);

    ConstantInfo(String tagName, Integer... i){
        this.tag = tagName;
        this.indexNum = new ArrayList<>();
        this.indexNum.addAll(Arrays.asList(i));
    }

    public static List<Integer> getBytesLenthList(String tagName){
        ConstantInfo[] values = ConstantInfo.values();
        ArrayList<Integer> objects = new ArrayList<>();
        if (StringUtils.isEmpty(tagName)){
            return objects;
        }
        for (int i = 0; i < values.length; i++) {
            if (tagName.equals(values[i].tag)){
                return values[i].indexNum;
            }
        }
        return objects;
    }

    private String tag;//标志位 占用一个字节
    private List<Integer> indexNum;//内容分别包含的字节数
}
