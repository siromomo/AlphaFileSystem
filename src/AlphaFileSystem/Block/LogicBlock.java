package AlphaFileSystem.Block;

import AlphaFileSystem.Id;

import java.io.Serializable;
import java.util.*;

public class LogicBlock implements Block, Serializable {
    private List<PhysicalBlock> blockList;
    private int blockSize;

    public LogicBlock(){
        blockList = new ArrayList<>();
    }

    public List<PhysicalBlock> getBlockList() {
        return blockList;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void cleanList(){
        this.blockList = new ArrayList<>();
    }

    public void setBlockList(List<PhysicalBlock> blockList) {
        this.blockList = blockList;
    }

    public void addBlock(PhysicalBlock block){
        blockList.add(block);
    }
    public void removeBlock(String manager, int id){
        for(int i = 0; i < blockList.size(); i++){
            AFSBlockManager blockManager = (AFSBlockManager) blockList.get(i).getBlockManager();
            BlockId blockId = (BlockId)blockList.get(i).getIndexId();
            if(blockManager.getBlockManagerId().getId().equals(manager) && blockId.getId() == id){
                blockList.remove(i);
                break;
            }
        }
    }

    @Override
    public Id getIndexId() {
        return null;
    }

    @Override
    public BlockManager getBlockManager() {
        return null;
    }

    @Override
    public byte[] read() {
        return new byte[0];
    }

    @Override
    public int blockSize() {
        return 0;
    }
}
