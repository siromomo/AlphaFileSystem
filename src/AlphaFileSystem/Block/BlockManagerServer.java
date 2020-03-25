package AlphaFileSystem.Block;

import AlphaFileSystem.Id;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.LinkedHashMap;

public class BlockManagerServer extends UnicastRemoteObject implements BlockManager,IBlockManagerServer {
    private AFSBlockManager blockManager;

    private LinkedHashMap<Integer, PhysicalBlock> buffer;
    private final static int BUFFER_SIZE = 10;

    public BlockManagerServer(String id, int port) throws RemoteException {
        super(port);
        buffer = new LinkedHashMap<>();
        this.blockManager = new AFSBlockManager(id);
    }

    protected BlockManagerServer(int port) throws RemoteException {
        super(port);
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
    public Block getBlock(Id indexId) {
        return blockManager.getBlock(indexId);
    }

    @Override
    public Block newBlock(byte[] b) {
        return blockManager.newBlock(b);
    }

    @Override
    public Block newEmptyBlock(int blockSize) {
        return blockManager.newEmptyBlock(blockSize);
    }

    @Override
    public PhysicalBlock sendBlock(Id id) throws RemoteException {
        PhysicalBlock block = null;
        try {
            if(buffer.get(((BlockId)id).getId()) != null){
                block = buffer.get(((BlockId)id).getId());
                return block;
            }
            block = (PhysicalBlock) this.getBlock(id);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(block != null)
            putBlockInBuffer(((BlockId)block.getIndexId()).getId(), block);
        return block;
    }

    @Override
    public PhysicalBlock createNewBlock(int size) throws RemoteException {
        return (PhysicalBlock) this.newEmptyBlock(size);
    }

    @Override
    public PhysicalBlock newBlockDelayedWrite(byte[] data) throws RemoteException {
        PhysicalBlock block = null;
        try {
            block = (PhysicalBlock) this.blockManager.newBlockDelayedWrite(data);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(block != null)
            putBlockInBuffer(((BlockId)block.getIndexId()).getId(), block);
        return block;
    }

    @Override
    public PhysicalBlock createNewBlockWithData(byte[] data) throws RemoteException {
        PhysicalBlock block = null;
        try {
            block = (PhysicalBlock) this.blockManager.newBlock(data);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(block != null)
            putBlockInBuffer(((BlockId)block.getIndexId()).getId(), block);
        return block;
    }


}
