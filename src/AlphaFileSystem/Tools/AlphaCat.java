package AlphaFileSystem.Tools;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.*;
import AlphaFileSystem.File.File;
import AlphaFileSystem.Server;

import java.io.*;

public class AlphaCat {
    public static void main(String[] args){
        try{
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(in);
            System.out.println("请输入文件管理器序号");
            String temp;
            while(!((temp = reader.readLine()).equals("1") || temp.equals("2"))){
                System.out.println("请输入合法序号（1或2）");
            }
            String fm = "fm-0" + temp;
            System.out.println("请输入文件名");
            String fileName = reader.readLine();
            System.out.println("请输入读取位置");
            int pos = Integer.parseInt(reader.readLine());
            FileManagerClient fileManagerClient = new FileManagerClient(fm);
            AFSFile file = (AFSFile) fileManagerClient.getFile(new FileId(fileName));
            if(pos > file.size()) {
                System.out.println("读的位置大于文件大小");
                return;
            }
            file.move(pos, File.MOVE_HEAD);
            System.out.println(new String(file.read((int)file.size() - pos)));
            file.close();
        }catch (ErrorCode e) {
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
