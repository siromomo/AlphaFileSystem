package AlphaFileSystem.File;

import AlphaFileSystem.*;

/*
* FileManager管理一个File的集合，只负责记录FileMeta
* 具备File创建和检索的能力但不具备数据存储能力。
* */

public interface FileManager {
    File getFile(Id fileId);
    File newFile(Id fileId);
}
