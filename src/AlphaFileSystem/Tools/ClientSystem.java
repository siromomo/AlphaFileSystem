package AlphaFileSystem.Tools;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.File.*;
import AlphaFileSystem.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientSystem {
    public static void main(String[] args){
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "500");
        System.setProperty("sun.rmi.transport.tcp.readTimeout", "500");
        System.setProperty("sun.rmi.transport.connectionTimeout", "500");
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "500");
        System.setProperty("sun.rmi.transport.tcp.handshakeTimeout", "500");
        try{
            Server.main(null);
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(in);
            System.out.println("输入1进行文件写，2进行文件读，3进行block读，4进行文件复制，-1退出");
            int n = 0;
            while(true){
                try{
                    n = Integer.parseInt(reader.readLine());
                    if((n < 1 || n > 4) && n != -1){
                        System.out.println("非法输入");
                        System.out.println("输入1进行文件写，2进行文件读，3进行block读，4进行文件复制，-1退出");
                        continue;
                    }
                }catch (Exception e){
                    System.out.println("非法输入");
                    System.out.println("输入1进行文件写，2进行文件读，3进行block读，4进行文件复制，-1退出");
                    continue;
                }
                if(n == 1){
                    AlphaWrite.main(null);
                }else if(n == 2){
                    AlphaCat.main(null);
                }else if(n == 3){
                    AlphaHex.main(null);
                }else if(n == 4){
                    AlphaCopy.main(null);
                }else{
                    break;
                }
                System.out.println("输入1进行文件写，2进行文件读，3进行block读，4进行文件复制，-1退出");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
