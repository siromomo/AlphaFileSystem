package AlphaFileSystem.File;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.Id;

import java.util.*;
import java.io.*;

/*
 * 初始：2个fm，3个bm
 * */

public class AFSFile implements File, Serializable {
    private java.io.File metaFile;
    private String metaString = "";
    private List<LogicBlock> logicBlockList;
    private long size;
    private String manager;
    private int blockSize = PhysicalBlock.DEFAULT_BLOCK_SIZE;
    private long currentPos = 0;
    private FileId id;
    private int clientOrServer;
    public static final int CLIENT = 1;
    public static final int SERVER = 0;

    public int getClientOrServer() {
        return clientOrServer;
    }

    public void setClientOrServer(int clientOrServer) {
        this.clientOrServer = clientOrServer;
    }

    public AFSFile(FileId id, String manager, int clientOrServer) {
        this(id, manager);
        this.clientOrServer = clientOrServer;
    }

    public AFSFile(String metaString, String manager, FileId id) {
        this.id = id;
        this.manager = manager;
        this.metaString = metaString;
        logicBlockList = new ArrayList<>();
        getMetaDataFromMetaString();
    }

    public AFSFile(java.io.File metaFile, String manager, FileId id) {
        this.id = id;
        this.manager = manager;
        this.metaFile = metaFile;
        getFileManager();
        logicBlockList = new ArrayList<>();
        getMetaDataFromFile();
    }

    public AFSFile(FileId id, String manager) {
        this.id = id;
        this.manager = manager;
        this.metaFile = new java.io.File("src/" + manager + "/" + id.getId() + ".meta");
        logicBlockList = new ArrayList<>();
        getMetaDataFromFile();
    }

    public String getMetaString() {
        return metaString;
    }

    public void setMetaString(String metaString) {
        this.metaString = metaString;
    }

    public void resetCursor() {
        this.currentPos = 0;
    }

    private void getMetaDataFromMetaString() {
        String[] metas = metaString.split("\n");
        for (String str : metas)
            getMetaDataFromString(str);
    }

    private void getMetaDataFromString(String str) {
        if (str.contains("size") && !str.contains("block")) {
            String sizeString = str.split(":")[1];
            size = Integer.parseInt(sizeString);
            //System.out.println(sizeString);
        } else if (str.contains("block size")) {
            String blockSizeString = str.split(":")[1];
            blockSize = Integer.parseInt(blockSizeString);
        } else if (str.contains("[")) {
            String[] blockData = str.split("[\\[\\]]");
            LogicBlock logicBlock = new LogicBlock();
            for (int i = 1; i < blockData.length; i++) {
                //System.out.println(blockData[i]);
                String[] temp = blockData[i].split(",");
                if (temp.length < 2)
                    continue;
                BlockManager manager;
                try {
                    manager = new BlockManagerClient(temp[0]);
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
                BlockId blockId = new BlockId(Integer.parseInt(temp[1]));
                boolean inBuffer = false;
                for (PhysicalBlock block : BufferBlocks.blocks) {
                    if (((AFSBlockManager) block.getBlockManager()).getName().equals(temp[0])
                            && ((BlockId) block.getIndexId()).getId() == Integer.parseInt(temp[1])) {
                        inBuffer = true;
                        logicBlock.addBlock(block);
                    }
                }
                if (!inBuffer) {
                    PhysicalBlock block = null;
                    try {
                        block = (PhysicalBlock) manager.getBlock((Id) blockId);
                    } catch (ErrorCode e) {
                        e.printStackTrace();
                        System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
                        continue;
                    }
                    if (!block.exist) {
                        System.out.println("request a block that not exist");
                        continue;
                       // throw new ErrorCode(ErrorCode.REQUEST_A_BLOCK_NOT_EXIST);
                    }
                    logicBlock.addBlock(block);
                    break;
                }
            }
            logicBlock.setBlockSize(blockSize);
            logicBlockList.add(logicBlock);
            //System.out.println(logicBlockList);
        }
    }

    private void getMetaDataFromFile() throws ErrorCode {
        LinkedList<PhysicalBlock> oldBuffer = new LinkedList<>(BufferBlocks.blocks);
        try {
            if (metaString.length() > 0) {
                getMetaDataFromMetaString();
                return;
            }
            RandomAccessFile in = new RandomAccessFile(
                    metaFile, "rws"
            );
            String str = null;
            while ((str = in.readLine()) != null) {
                this.metaString += str;
                this.metaString += "\n";
                getMetaDataFromString(str);
            }
        } catch (Exception e) {
            BufferBlocks.blocks = oldBuffer;    // 回滚缓存
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.GET_META_DATA_FAILED);
        }
    }


    @Override
    public Id getFileId() {
        return id;
    }

    @Override
    public FileManager getFileManager() {
        String metaPath = metaFile.getPath();
        String[] parts = metaPath.split("\\\\");
        for (String part : parts) {
            if (part.contains("fm")) {
                this.manager = part;
                return new AFSFileManager(new FileManagerId(part));
            }
        }
        return null;
    }

    @Override
    public byte[] read(int length) {
        byte[] res = new byte[length];
        if (logicBlockList.size() == 0)
            return res;
        int currIndex = (int) currentPos / blockSize;
        LogicBlock currBlock = logicBlockList.get(currIndex);
        int cursor = (int) currentPos % blockSize;
        for (int targetPos = 0, dataPos = cursor, i = 0; i < length && i < size; ) {
            int toRead = Math.min(length - i, blockSize);
            List<PhysicalBlock> blockList = currBlock.getBlockList();
            boolean currBlockValid = false;
            for (PhysicalBlock block : blockList) {
                int blocksizeNow = block.blockSize();
                if (i == 0) cursor = (int) currentPos % blocksizeNow;
                try {
                    if (!block.valid) {
                        continue;
                    }
                    byte[] data = block.read();
                    toRead = Math.min(length - i, block.blockSize());
                    //System.out.println(new String(data));
                    //System.out.print(" ");
                    //System.out.print(new String(res));
                    //System.out.println(" i: " + i + " toRead: " + toRead);

                    System.arraycopy(data, dataPos, res, targetPos, toRead);
                    i += blocksizeNow;
                    targetPos += blocksizeNow - cursor;
                    currBlockValid = true;
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new ErrorCode(ErrorCode.READ_OUT_BOUND);
                }
            }
            if (!currBlockValid) {
                throw new ErrorCode(ErrorCode.CURR_LOGIC_BLOCK_NOT_VALID);
            }
            dataPos = 0;
            cursor = 0;
            currIndex++;
            if (currIndex >= logicBlockList.size())
                break;
            currBlock = logicBlockList.get(currIndex);
        }
        currentPos += length;
        return res;
    }

    /**
     * int MOVE_CURR = 0;
     * int MOVE_HEAD = 1;
     * int MOVE_TAIL = 2;
     */

    @Override
    public void write(byte[] b) throws ErrorCode {
        String oldMeta = AFSFileManager.createMetadata((int)size, blockSize, logicBlockList);
        try {
            long offset = currentPos % PhysicalBlock.DEFAULT_BLOCK_SIZE;
            byte[] originData = new byte[(int) offset];
            boolean originValid = false;
            LogicBlock currBlock = null;
            int currBlockIndex = (int) currentPos / PhysicalBlock.DEFAULT_BLOCK_SIZE;
            int minus = 0;
            if (currBlockIndex < logicBlockList.size()) {
                currBlock = logicBlockList.get(currBlockIndex);
                if (currBlock.getBlockList().size() > 0) {
                    List<PhysicalBlock> blocks = currBlock.getBlockList();
                    for (PhysicalBlock block : blocks) {
                        try {
                            byte[] allData = block.read();
                            int originDataSize = 0;
                            for (int i = 0; i < allData.length && allData[i] != 0; i++, originDataSize++) {
                            }
                            minus = (int) (originDataSize - offset);
                            System.arraycopy(allData, 0, originData, 0, (int) offset);
                            originValid = true;
                            // System.out.println("origin data: " + new String(originData));
                            currBlock.cleanList();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
                        }
                    }
                }
            }
            while (logicBlockList.size() > currBlockIndex + 1) {
                logicBlockList.remove(currBlockIndex + 1);
            }
            size = currentPos;
            if (currBlockIndex < logicBlockList.size() && !originValid) {
                throw new ErrorCode(ErrorCode.GET_FILE_DATA_FAILED);
            }
            LogicBlock logicBlock = currBlock == null ? new LogicBlock() : currBlock;
            for (int i = -originData.length; i < b.length; i += PhysicalBlock.DEFAULT_BLOCK_SIZE, currBlockIndex++) {
                for (int n = 0; n < PhysicalBlock.BLOCK_NUM_IN_A_LOGIC_BLOCK; n++) {
                    int rand = (int) (PhysicalBlock.BLOCK_MANAGER_NUM * Math.random() + 1);
                    if (rand > PhysicalBlock.BLOCK_MANAGER_NUM) rand = 1;
                    String bmId = "bm-0" + rand;
                    BlockManager blockManager = null;
                    HashSet<String> managerToChoose = new HashSet<>();
                    if (!bmId.equals("bm-01")) managerToChoose.add("bm-01");
                    if (!bmId.equals("bm-02")) managerToChoose.add("bm-02");
                    if (!bmId.equals("bm-03")) managerToChoose.add("bm-03");
                    if (clientOrServer == CLIENT) {
                        while (blockManager == null) {
                            try {
                                blockManager = new BlockManagerClient(bmId);
                            } catch (ErrorCode e) {
                                System.out.println(ErrorCode.getErrorText(e.getErrorCode()));
                                blockManager = null;
                                if (managerToChoose.size() == 0) {
                                    System.out.println("There is no block manager available now");
                                    return;
                                }
                                bmId = managerToChoose.iterator().next();
                                managerToChoose.remove(bmId);
                            }
                        }
                    } else
                        blockManager = new AFSBlockManager(bmId);
                    byte[] dataToWrite;
                    if (i <= 0) {
                        int minOffset = Math.min(PhysicalBlock.DEFAULT_BLOCK_SIZE - originData.length, b.length);
                        dataToWrite = new byte[PhysicalBlock.DEFAULT_BLOCK_SIZE];
                        System.arraycopy(originData, 0, dataToWrite, 0, originData.length);

                        System.arraycopy(b, 0, dataToWrite, originData.length, minOffset);
                        //System.out.println("origin data plus to write: " + new String(dataToWrite));
                    } else {
                        int minOffset = Math.min(b.length - i, PhysicalBlock.DEFAULT_BLOCK_SIZE);
                        dataToWrite = new byte[PhysicalBlock.DEFAULT_BLOCK_SIZE];
                        System.arraycopy(b, i, dataToWrite, 0, minOffset);
                    }
                    PhysicalBlock newBlock;
                    if (clientOrServer == CLIENT)
                        newBlock = (PhysicalBlock) (((BlockManagerClient) blockManager).newBlockDelayedWrite(dataToWrite));
                    else
                        newBlock = (PhysicalBlock) (((AFSBlockManager) blockManager).newBlockDelayedWrite(dataToWrite));
                    logicBlock.addBlock(newBlock);
                    BufferBlocks.addBlockToBuffer(newBlock);
                }

                if (i > 0 || currBlock == null)
                    logicBlockList.add(currBlockIndex, logicBlock);
                logicBlock = new LogicBlock();
            }
            currentPos += b.length;

            //  System.out.println(logicBlockList);
            //  System.out.println(logicBlock.getBlockList());
            //  System.out.println(b.length);
            //  size -= minus;
            size += b.length;
            String metaData =
                    AFSFileManager.createMetadata((int) size, blockSize, logicBlockList);
            writeMetaFile(metaData);
        } catch (Exception e) {
            writeMetaFile(oldMeta);
            e.printStackTrace();
        }
    }

    private void writeMetaFile(String meta) throws ErrorCode{
        FileManagerClient fm = new FileManagerClient(manager);
        try {
            fm.writeMetaFile(meta, id.getId());
            //System.out.println(manager);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("write meta file failed");
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
    }



    @Override
    public String toString() {
        return AFSFileManager.createMetadata((int) size, blockSize, logicBlockList);
    }

    @Override
    public long move(long offset, int where) {
        long lastPos = currentPos;
        if (where == MOVE_CURR) {
            currentPos += offset;
        } else if (where == MOVE_HEAD) {
            currentPos = offset;
        } else {
            currentPos = size + offset;
        }

        if (currentPos < 0) {
            System.out.println("cursor < 0 is invalid, the cursor won't be changed");
            currentPos = lastPos;
            return currentPos;
        }

        if (currentPos > size) {
            byte[] bytes = new byte[(int) currentPos - (int) size];
            currentPos = size;
            write(bytes);
            size = (int) currentPos;
        }

        return currentPos;
    }

    @Override
    public void close() {
        BufferBlocks.flush();
    }

    @Override
    public long size() {
        return this.size;
    }

    @Override
    public void setSize(long newSize) {
        if (newSize == size)
            return;
        if (newSize < size) {
            int currBlockIndex = (int) (newSize / PhysicalBlock.DEFAULT_BLOCK_SIZE);
            int offset = (int) (newSize % PhysicalBlock.DEFAULT_BLOCK_SIZE);
            byte[] originData = new byte[offset];
            LogicBlock currBlock = null;
            boolean originValid = false;
            if (currBlockIndex < logicBlockList.size()) {
                currBlock = logicBlockList.get(currBlockIndex);
                if (currBlock.getBlockList().size() > 0) {
                    List<PhysicalBlock> blocks = currBlock.getBlockList();
                    for (PhysicalBlock block : blocks) {
                        try {
                            byte[] allData = block.read();
                            System.arraycopy(allData, 0, originData, 0, (int) offset);

                            originValid = true;
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (!originValid) {
                System.out.println("get origin data failed");
            }
            for (; currBlockIndex < logicBlockList.size(); ) {
                logicBlockList.remove(currBlockIndex);
            }
            // System.out.println(logicBlockList.size());
            currentPos = currBlockIndex * PhysicalBlock.DEFAULT_BLOCK_SIZE;
            write(originData);
        } else {
            byte[] dataToWrite = new byte[(int) (newSize - size)];
            currentPos = size;
            write(dataToWrite);
        }
        size = newSize;
        String metaData =
                AFSFileManager.createMetadata((int) size, blockSize, logicBlockList);
        writeMetaFile(metaData);
    }
}
