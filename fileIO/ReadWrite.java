import java.io.*;

/**
 * 从 input.txt 中读取文件，写入到 outfile.txt 中
 * 采取按行读取的方式
 */
public class ReadWrite {

    public static void main(String[] args){
        try{
            String pathname = "fileIO/input.txt";       //读入文件位置
            File filename = new File(pathname);         
            // 建立一个输入流对象并设置编码
            InputStreamReader fin = new InputStreamReader(new FileInputStream(filename), "UTF-8");    
            BufferedReader br = new BufferedReader(fin);    //读取字符进入缓冲区

            File outfile = new File("fileIO/outfile.txt");
            outfile.createNewFile();            // 创建一个新的文件，如果有名称相同的文件直接覆盖
            // 建立一个输出流对象并设置编码
            OutputStreamWriter tofile = new OutputStreamWriter(new FileOutputStream(outfile), "UTF-8");
            BufferedWriter out = new BufferedWriter(tofile);

            String line = null;                     //存储读出的每一行内容
            while ((line = br.readLine()) != null){
                out.write(line);                    //将改行文本写入到缓冲区中
                out.write("\n");                    //加入换行符，原文本是换行的
                out.flush();                        //将缓冲的数据发送到输入文件
            }
            //关闭缓冲区
            br.close();
            out.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}