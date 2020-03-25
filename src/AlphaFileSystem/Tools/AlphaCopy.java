package AlphaFileSystem.Tools;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.*;

import java.io.*;

public class AlphaCopy {
    public static void main(String[] args){
        try{
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(in);
            System.out.println("复制源文件：");
            System.out.println("请输入文件管理器序号");
            String temp;
            while(!((temp = reader.readLine()).equals("1") || temp.equals("2"))){
                System.out.println("请输入合法序号（1或2）");
            }
            String originFm = "fm-0" + temp;
            System.out.println("请输入文件名");
            String originFileName = reader.readLine();
            System.out.println("目标文件：");
            System.out.println("请输入文件管理器序号");
            while(!((temp = reader.readLine()).equals("1") || temp.equals("2"))){
                System.out.println("请输入合法序号（1或2）");
            }
            String fm = "fm-0" + temp;
            System.out.println("请输入文件名");
            String fileName = reader.readLine();
            String originMetaPath = "src/" + originFm + "/" + originFileName + ".meta";
            String metaPath = "src/" + fm + "/" + fileName + ".meta";
            FileManagerClient managerClient = new FileManagerClient(fm);
            managerClient.copyFile(originMetaPath, metaPath);
        }catch (ErrorCode e) {
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
