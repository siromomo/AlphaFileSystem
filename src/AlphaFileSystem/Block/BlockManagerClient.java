package AlphaFileSystem.Block;

import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.File.IFileManagerServer;
import AlphaFileSystem.Id;

import java.rmi.Naming;
import java.util.*;
import java.rmi.registry.*;

public class BlockManagerClient implements BlockManager {
    private IBlockManagerServer remoteBm;
    private LinkedHashMap<Integer, PhysicalBlock> buffer;
    private LinkedHashMap<Integer, PhysicalBlock> bufferBackup;
    private final static int BUFFER_SIZE = 10;

    public static String getURLFromId(String id){
        return "rmi://127.0.0.1:11451/" + id;
    }

    public BlockManagerClient(String id){
        buffer = new LinkedHashMap<>();
        bufferBackup = new LinkedHashMap<>(buffer);
        try{
            remoteBm = (IBlockManagerServer) Naming.lookup(getURLFromId(id));
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Block manager " + id + " is not available yet");
            throw new ErrorCode(ErrorCode.GET_REMOTE_MANAGER_FAILED);
        }
    }

    public PhysicalBlock newBlockDelayedWrite(byte[] data){
        try {
            return remoteBm.newBlockDelayedWrite(data);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Block getBlock(Id indexId){
        bufferBackup = new LinkedHashMap<>(buffer);
        try {
            int id = ((BlockId)indexId).getId();
            if(buffer.get(id) != null){
                PhysicalBlock block = buffer.get(id);
                buffer.remove(id);
                buffer.put(id, block);
                return block;
            }
            PhysicalBlock block = null;
            try {
                block = (PhysicalBlock) remoteBm.sendBlock(indexId);
            }catch (Exception e){
                e.printStackTrace();
                throw new ErrorCode(ErrorCode.GET_DATA_FROM_REMOTE_FAILED);
            }
            putBlockInBuffer(id, block);
            return block;
        }catch (Exception e){
            e.printStackTrace();
            buffer = new LinkedHashMap<>(bufferBackup);
            throw new ErrorCode(ErrorCode.GET_DATA_FROM_REMOTE_FAILED);
        }
    }

    private void putBlockInBuffer(int id, PhysicalBlock block){
        if (buffer.size() >= BUFFER_SIZE) {
            int keyToRemove = -1;
            for (int key : buffer.keySet())
                keyToRemove = key;
            buffer.remove(keyToRemove);
            buffer.put(id, block);
        }else {
            buffer.put(id, block);
        }
    }

    @Override
    public Block newBlock(byte[] b) {
        PhysicalBlock block = null;
        bufferBackup = new LinkedHashMap<>(buffer);
        try{
            block = remoteBm.createNewBlockWithData(b);
            int id = ((BlockId)block.getIndexId()).getId();
            putBlockInBuffer(id, block);
        }catch (Exception e){
            buffer = new LinkedHashMap<>(bufferBackup);
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.TRANSPORT_DATA_TO_REMOTE_FAILED);
        }
        return block;
    }

    @Override
    public Block newEmptyBlock(int size){
        try{
            return remoteBm.createNewBlock(size);
        }catch (Exception e){
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.TRANSPORT_DATA_TO_REMOTE_FAILED);
        }
    }


}
