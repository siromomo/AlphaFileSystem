package AlphaFileSystem.File;

import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.Id;
import AlphaFileSystem.Block.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AFSFileManager implements FileManager {
    private Id managerId;


    public AFSFileManager(Id managerId){
        this.managerId = managerId;
    }

    public Id getManagerId() {
        return managerId;
    }

    public void setManagerId(Id managerId) {
        this.managerId = managerId;
    }

    @Override
    public File getFile(Id fileId) {
        String currentPath = "src/" + ((FileManagerId)managerId).getId() + "/";
        java.io.File index = new java.io.File(currentPath);
        java.io.File[] files = index.listFiles();
        for (java.io.File file : files){
            if(file.getName().contains(((FileId)fileId).getId())){
                return createAFSFile(file);
            }
        }

        return null;
    }

    public File getFile(Id fileId, String fileMeta){
        return new AFSFile(fileMeta, ((FileManagerId)managerId).getId(),(FileId) fileId);
    }

    private AFSFile createAFSFile(java.io.File file){
        String fileName = file.getName().split("\\.")[0];
        return new AFSFile(file, ((FileManagerId)managerId).getId(), new FileId(fileName));
    }

    public static String createMetadata(int size, int blockSize,
                                  List<LogicBlock> logicBlockList){
        StringBuilder data = new StringBuilder();
        data.append("size:");
        data.append(size);
        data.append("\n");
        data.append("block size:");
        data.append(blockSize);
        data.append("\n");
        data.append("logic block:");
        data.append("\n");
        if(logicBlockList != null) {
            for (int i = 0; i < logicBlockList.size(); i++) {
                LogicBlock logicBlock = logicBlockList.get(i);
                data.append(i);
                data.append(":");
                for (PhysicalBlock block : logicBlock.getBlockList()) {
                    data.append("[");
                    data.append(((AFSBlockManager)block.getBlockManager()).getName());
                    data.append(",");
                    data.append(((BlockId)block.getIndexId()).getId());
                    data.append("]");
                }
                data.append("\n");
            }
        }
        //System.out.println(data.toString());
        return data.toString();
    }

    @Override
    public File newFile(Id fileId) throws ErrorCode {
        FileId fi = (FileId)fileId;
        String path = "src/" + ((FileManagerId)managerId).getId() + "/" + fi.getId() + ".meta";
        java.io.File meta = new java.io.File(path);
        String metaString = createMetadata(0, PhysicalBlock.DEFAULT_BLOCK_SIZE,
                null);
        try {
            meta.createNewFile();
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(path)
            );

            byte[] metaData =
                    metaString.getBytes();
            out.write(metaData, 0, metaData.length);
        }catch (Exception e){
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
        AFSFile file = new AFSFile(fi, ((FileManagerId) managerId).getId());
        file.setMetaString(metaString);
        return file;
    }
}
