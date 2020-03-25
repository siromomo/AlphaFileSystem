package AlphaFileSystem.Block;

import AlphaFileSystem.*;

public interface Block {
    Id getIndexId();
    BlockManager getBlockManager();

    byte[] read();
    int blockSize();
}
