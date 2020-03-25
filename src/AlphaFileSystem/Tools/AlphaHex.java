package AlphaFileSystem.Tools;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AlphaHex {
    public static void main(String[] args){
        try{
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader reader = new BufferedReader(in);
            System.out.println("请输入块管理器序号");
            String bm = "bm-0" + reader.readLine();
            System.out.println("请输入块序号");
            String bIndex = reader.readLine();
            BlockManagerClient manager = new BlockManagerClient(bm);
            BlockId id = new BlockId(Integer.parseInt(bIndex));
            PhysicalBlock block = (PhysicalBlock) manager.getBlock(id);
            byte[] data = block.read();
            if(data != null)
                System.out.println(toHexString4(data));
        }catch (ErrorCode e) {
            System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    static String toHexString4(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 使用String的format方法进行转换
        int count = 0;
        for (byte b : bytes) {
            sb.append("0x");
            sb.append(String.format("%02x", new Integer(b & 0xff)));
            sb.append(" ");
            count++;
            if(count == 7){
                sb.append("\n");
                count = 0;
            }
        }

        return sb.toString();
    }
}
