package AlphaFileSystem.Block;

import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.AFSFile;
import AlphaFileSystem.Id;
import java.io.*;
import java.nio.Buffer;

public class PhysicalBlock implements Block, Serializable {
    private byte[] data = null;
    private boolean delayedWrite = false;
    private long checksum = -1;
    private BlockManager blockManager;
    private String blockManagerId;
    private int blockSize = DEFAULT_BLOCK_SIZE;
    private BlockId id;
    public static final int DEFAULT_BLOCK_SIZE = 512;
    public static final int BLOCK_MANAGER_NUM = 3;
    public static final int BLOCK_NUM_IN_A_LOGIC_BLOCK = 3;
    public boolean exist = true;
    public boolean valid = true;

    public PhysicalBlock(AFSBlockManager blockManager, BlockId id, int blockSize){
        this.id = id;
        this.blockManager = blockManager;
        this.blockSize = blockSize;
        getMetaFromFile();
        getDataFromFile();

        if(!isChecksumRight()){
            throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
        }
    }

    public PhysicalBlock(AFSBlockManager blockManager, String blockManagerId, BlockId id){
        this.id = id;
        this.blockManager = blockManager;
        this.blockManagerId = blockManagerId;
        getMetaFromFile();
        if(this.exist) {
            getDataFromFile();

            if (!isChecksumRight()) {
                valid = false;
                throw new ErrorCode(ErrorCode.CHECKSUM_CHECK_FAILED);
            }
        }
    }
    public PhysicalBlock(byte[] data, String blockManagerId, int bkId, AFSBlockManager manager){
        this(data, bkId, manager);
        this.blockManagerId = blockManagerId;
    }
    public PhysicalBlock(byte[] data, String blockManagerId, int bkId, AFSBlockManager manager, boolean delayedWrite){
        this(data, bkId, manager, delayedWrite);
        this.blockManagerId = blockManagerId;
    }

    public PhysicalBlock(byte[] data, int bkId, AFSBlockManager manager){
        this.data = data;
        this.id = new BlockId(bkId);
        this.blockManager = manager;
        setChecksum();
        createDataFile();
        createMetaFile();
    }

    public PhysicalBlock(byte[] data, int bkId, AFSBlockManager manager, boolean delayedWrite){
        this.data = data;
        this.id = new BlockId(bkId);
        this.blockManager = manager;
        this.blockSize = data.length;
        setChecksum();
        if(!delayedWrite){
           createDataFile();
           createMetaFile();
        }
    }

    public void startWrite(){
        createDataFile();
        createMetaFile();
    }

    private void getMetaFromFile(){
        String path = "src/" + blockManagerId + "/"
                + ((BlockId)this.getIndexId()).getId() + ".meta";
        java.io.File metaFile = new File(path);
        if(!metaFile.exists()){
            System.out.println("block not exist");
            this.exist = false;
            return;
        }
        try{
            RandomAccessFile accessFile = new RandomAccessFile(metaFile, "rws");
            String temp = null;
            while((temp = accessFile.readLine()) != null){
                if(temp.contains("size")){
                    String sizeString = temp.split(":")[1];
                    blockSize = Integer.parseInt(sizeString);
                }else if(temp.contains("checksum")){
                    String csString = temp.split(":")[1];
                    checksum = Long.parseLong(csString);
                }
            }
            accessFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getDataFromFile(){
        String path = "src/" + blockManagerId + "/"
                + ((BlockId)this.getIndexId()).getId() + ".data";
        java.io.File blockFile = new File(path);
        data = new byte[blockSize];
        try {
            RandomAccessFile accessFile = new RandomAccessFile(blockFile, "rws");
            accessFile.read(data, 0, data.length);
            accessFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createDataFile(){
        String path = "src/" + blockManagerId + "/"
                + ((BlockId)this.getIndexId()).getId() + ".data";

//        System.out.println(path);
        java.io.File blockFile = new File(path);
//        printData("when create data file");
        try{
           // System.out.println(path);
            blockFile.createNewFile();
            RandomAccessFile accessFile = new RandomAccessFile(blockFile, "rws");
            accessFile.write(data, 0, data.length);
            accessFile.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void printData(String hint){
        System.out.println(hint);
        System.out.println("data's length: " + data.length);
        for(byte b : data)
            System.out.print(b +" ");
        System.out.println();
    }

    private void createMetaFile(){
        String path = "src/" + blockManagerId + "/"
                + ((BlockId)this.getIndexId()).getId() + ".meta";
        java.io.File metaFile = new File(path);
        byte[] metaData = createMetaString().getBytes();
        try{
            metaFile.createNewFile();
            RandomAccessFile out = new RandomAccessFile(metaFile, "rws");
            out.write(metaData, 0, metaData.length);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String createMetaString(){
        StringBuilder meta = new StringBuilder();
        meta.append("size:");
        meta.append(blockSize);
        meta.append("\n");
        meta.append("checksum:");
        meta.append(checksum);
        return meta.toString();
    }

    public long getChecksum() {
        return checksum;
    }

    private long getChecksumFromData(){
        long h = data.length;
        for(byte b : data){
            long lb = (long)b;
            h = (h << 1) | (h >>> 63) | ((lb & 0xc3) << 41)
                    | ((lb & 0xa7) << 12) + lb * 1234223;
        }
//        printData("when read file ");
//        System.out.println(h);
        return h;
    }
    private void setChecksum(){
        this.checksum = getChecksumFromData();
    }

    private boolean isChecksumRight(){
        return this.checksum == getChecksumFromData();
    }

    @Override
    public Id getIndexId() {
        return this.id;
    }

    @Override
    public BlockManager getBlockManager() {
        return blockManager;
    }

    @Override
    public byte[] read() {
        return data;
    }

    @Override
    public int blockSize() {
        return this.blockSize;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder();
        res.append(new String(data));
        res.append("|");
        res.append(createMetaString());
        return res.toString();
    }
}
