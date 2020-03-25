package AlphaFileSystem.Tools;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.*;
import AlphaFileSystem.Server;

import java.io.*;
import java.io.File;

public class AlphaWrite {
    public static void main(String[] args){
        try{
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(in);
            System.out.println("请输入文件管理器序号");
            String temp;
            while(!((temp = reader.readLine()).equals("1") || temp.equals("2"))){
                System.out.println("请输入合法序号（1或2）");
            }
            String fm = temp;
            System.out.println("请输入文件名");
            String fileName = reader.readLine();
            System.out.println("请输入要写入的内容");
            String data = reader.readLine();
            System.out.println("请输入插入点");
            String cursor = reader.readLine();
            String fmName= "fm-0" + fm;
            String metaFilePath = "src/" + fmName + "/" + fileName + ".meta";
            java.io.File metaFile = new java.io.File(metaFilePath);
            FileManagerClient manager = new FileManagerClient(fmName);
            AFSFile file = null;
            if(!metaFile.exists())
                file = (AFSFile) manager.newFile(new FileId(fileName));
            else
                file = (AFSFile) manager.getFile(new FileId(fileName));
            file.move(Integer.parseInt(cursor), AlphaFileSystem.File.File.MOVE_HEAD);
            file.write(data.getBytes());
            file.close();
        }catch (ErrorCode e) {
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
