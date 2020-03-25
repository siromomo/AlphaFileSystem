package AlphaFileSystem.File;

import AlphaFileSystem.Block.AFSBlockManager;
import AlphaFileSystem.Block.Block;
import AlphaFileSystem.Block.BlockId;
import AlphaFileSystem.Block.PhysicalBlock;

import java.util.HashMap;
import java.util.LinkedList;

public class BufferBlocks {
    static LinkedList<PhysicalBlock> blocks = new LinkedList<>();
    public static int MAX_SIZE = 5;

    public static void addBlockToBuffer(PhysicalBlock block){
        if(blocks.contains(block)){
            blocks.remove(block);
            blocks.addFirst(block);
        }else {
            blocks.addFirst(block);
            if (blocks.size() == MAX_SIZE) {
                PhysicalBlock lastBlock = blocks.removeLast();
                lastBlock.startWrite();
            }
        }
    }

    public static void flush(){
        for(PhysicalBlock block : blocks)
            block.startWrite();
        blocks = new LinkedList<>();
    }
}
