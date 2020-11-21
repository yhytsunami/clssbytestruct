package deepin;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther:yhy
 * @Created:2019/10/24 17:23
 * @Description:解析class文件
 */
public class AnalyseClass {


    private static final String NL = "\r\n";
    byte[] u4 = new byte[4];
    byte[] u4b = new byte[4];
    byte[] u2 = new byte[2];
    byte[] u2b = new byte[2];
    byte[] u1 = new byte[1];
    byte[] u1b = new byte[1];

    private byte[] getUb(byte[] u,byte[] ub){
        for (int i = 0; i < u.length; i++) {
            ub[i] = u[i];
            u[i] = 0;
        }
        return ub;
    }

    public byte[] getU4(){
        return this.getUb(u4,u4b);
    }
    public byte[] getU2(){
        return this.getUb(u2,u2b);
    }
    public byte[] getU1(){
        return this.getUb(u1,u1b);
    }

    public void rwSpace(ByteBuffer bf, byte[] bytes, FileOutputStream fos){
        readAndWrite( bf, bytes, fos," ");
    }
    public void rwNl(ByteBuffer bf,byte[] bytes,FileOutputStream fos){
        readAndWrite( bf, bytes, fos,NL);
    }
    private void readAndWrite(ByteBuffer bf,byte[] bytes,FileOutputStream fos,String spliter){
        int a = bytes.length;
        bf.get(bytes,0,a);
        byte[] bs = getBytesByByteNum(a);
        String outStr = ""+ Hex.encodeHexString(bs)+spliter;
        writeStr(fos, outStr);
    }

    private void writeStr(FileOutputStream fos, String outStr) {
        try {
            fos.write(outStr.getBytes());
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getBytesByByteNum(int a) {
        byte[] bs = null;
        switch (a){
            case 1 : bs = getU1();break;
            case 2 : bs = getU2();break;
            case 4 : bs = getU4();
        }
        return bs;
    }
    private byte[] getUbyNum(int a) {
        byte[] bs = null;
        switch (a){
            case 1 : bs = u1;break;
            case 2 : bs = u2;break;
            case 4 : bs = u4;
        }
        return bs;
    }

    public void test_load(String filePath) throws Exception{
        //读取文件
        File file = new File(filePath);
        //获取文件长度
        long length = file.length();
        //获取文件通道
        FileChannel channel = new FileInputStream(file).getChannel();
        //创建字节缓存
        ByteBuffer allocate = ByteBuffer.allocate((int)length);
        //读取到字节缓存
        channel.read(allocate);
        //重置position位置为0
        Buffer flip = allocate.flip();
        //创建写出文件
        String userHome = System.getProperties().getProperty("user.home");
        File file1 = new File(userHome+"\\Desktop\\classFormat.txt");
        System.out.println(file1.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(file1);
        //取魔数
        rwNl(allocate,u4,fos);
        //取版本号
        rwSpace(allocate,u2,fos);
        rwNl(allocate,u2,fos);
        //取常量池项数
        final String constantPoolCount = getHexRead(allocate,u2);
        int constantPoolCounts = Integer.parseInt(constantPoolCount, 16);
        fos.write((""+constantPoolCount+" "+constantPoolCounts+NL).getBytes());
        fos.flush();

        System.out.println();
        //解析常量池
        int i = 1;
        ArrayList<String> indexs = new ArrayList<>();
        while (i < constantPoolCounts){
            writeStr(fos,"\t");
            //读取标志位
            String tagName = getHexRead(allocate, u1);
            writeStr(fos,""+tagName+" ");
            //按字节数列表解析 -1的时候按utf8处理
            analyseConstantPool(allocate, fos, indexs, tagName.toUpperCase());
            //换行
            writeStr(fos,NL);
            i++;
        }
        //类的访问标志符
        rwNl(allocate,u2,fos);
        //类索引
        rwNl(allocate,u2,fos);
        //父类索引
        rwNl(allocate,u2,fos);
        //接口计数器，0可以不进行解析
        final String interfaceCount = getHexRead(allocate,u2);
        int interfaceCounts = Integer.parseInt(interfaceCount, 16);
        fos.write((""+interfaceCount+" "+interfaceCounts+NL).getBytes());
        if(interfaceCounts > 0){
            for (int j = 0; j < interfaceCounts; j++) {
                writeStr(fos,"\t");
                rwSpace(allocate,u2,fos);
                writeStr(fos,NL);
            }
        }
        //字段表
        analyseFieldInfoAndMethodInfo(allocate, fos);
        //方法表
        analyseFieldInfoAndMethodInfo(allocate, fos);
        //类附加属性
        analseAttrInfo(allocate, fos);
        fos.close();
    }

    private void analyseFieldInfoAndMethodInfo(ByteBuffer allocate, FileOutputStream fos) throws IOException {
        final String fieldCount = getHexRead(allocate,u2);
        int fieldCounts = Integer.parseInt(fieldCount, 16);
        fos.write((""+fieldCount+" "+fieldCounts+NL).getBytes());
        if(fieldCounts > 0){
            for (int j = 0; j < fieldCounts; j++) {
                writeStr(fos,"\t");
                //访问标志
                rwSpace(allocate,u2,fos);
                //名称索引
                rwSpace(allocate,u2,fos);
                //描述符索引
                rwSpace(allocate,u2,fos);
                //属性表长度
                analseAttrInfo(allocate, fos);

            }
        }
    }

    private void analseAttrInfo(ByteBuffer allocate, FileOutputStream fos) throws IOException {
        final String attributeLenth = getHexRead(allocate,u2);
        int attributes = Integer.parseInt(attributeLenth, 16);
        fos.write((""+attributeLenth+" "+attributes+":"+NL).getBytes());
        //属性表内容
        for (int k = 0; k < attributes; k++) {
            writeStr(fos,"\t\t");
            //属性名称
            rwSpace(allocate,u2,fos);
            //属性内容长度
            final String aLenth = getHexRead(allocate,u4);
            int attrs = Integer.parseInt(aLenth, 16);
            fos.write((""+aLenth+" "+attrs+"->").getBytes());
            byte[] rdAttributes = new byte[attrs];
            allocate.get(rdAttributes,0,attrs);
            String encode = Hex.encodeHexString(rdAttributes);
            writeStr(fos,encode+NL);
        }
    }

    private void analyseConstantPool(ByteBuffer allocate, FileOutputStream fos, ArrayList<String> indexs, String tagName) {
        List<Integer> bytesLenthList = ConstantInfo.getBytesLenthList(tagName);
        if(CollectionUtils.isNotEmpty(bytesLenthList)){
            for (Integer in : bytesLenthList) {
                if(in != -1){
                    String rd = getHexRead(allocate, getUbyNum(in));
                    indexs.add(rd);
                    writeStr(fos,""+rd+" ");
                }else{
                    //解析utf8字面量
                    int byteLen = Integer.parseInt(indexs.get(0), 16);
                    byte[] utf8Bytes = new byte[byteLen];
                    allocate.get(utf8Bytes,0,byteLen);
                    String s = new String(utf8Bytes);
                    writeStr(fos,""+s+" ");
                }
            }
            indexs.clear();
        }
    }

    private String getHexRead(ByteBuffer allocate,byte[] bs) {
        allocate.get(bs,0,bs.length);
        return Hex.encodeHexString(getBytesByByteNum(bs.length));
    }

}
