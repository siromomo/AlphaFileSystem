package AlphaFileSystem.File;

import AlphaFileSystem.*;

/*
* File分为Data和Meta两部分
* Data按照某种方式拆分字节序列并将拆分好的数据存放在各自的Block中
* Meta表现为“file23-meta”这样的文件
* 至少需要记录File保存的字节序列的字节数目和找到Data的方法
* 获取 FileData 的方法可以简单理解为:
* 从 FileMeta 中获得所有 Block 的定位信息
* (Block 归属的 BlockManager 和在该 BlockManager 中的索引),
* 然后读取对应的 BlockData,
* 然后将这些数据组合起来复原得到 FileData;
* */

public interface File {
    int MOVE_CURR = 0;
    int MOVE_HEAD = 1;
    int MOVE_TAIL = 2;

    Id getFileId();
    FileManager getFileManager();

    byte[] read(int length);
    void write(byte[] b);
    default long pos() {
        return move(0, MOVE_CURR);
    }
    long move(long offset, int where);

    void close();

    long size();
    void setSize(long newSize);

}
