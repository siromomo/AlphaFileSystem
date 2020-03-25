package AlphaFileSystem.File;


import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.Id;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;

import static java.lang.Thread.sleep;

public class FileManagerServer extends UnicastRemoteObject implements IFileManagerServer,FileManager {
    private AFSFileManager fileManager;


    private LinkedHashMap<String, String> buffer = new LinkedHashMap<>();

    public String getMetaDataFromFile(String fileName) throws ErrorCode {
        if (buffer.get(fileName) != null){
            return buffer.get(fileName);
        }
        String metaString = "";
        try {
            String currentPath = "src/" + ((FileManagerId)fileManager.getManagerId()).getId() + "/";
            java.io.File metaFile = new java.io.File(currentPath + fileName + ".meta");
            if(!metaFile.exists()){
                metaFile = new java.io.File(currentPath + fileName + "_temp.meta");
                if(!metaFile.exists()){
                    System.out.println("meta文件丢失");
                    return null;
                }
                java.io.File metaFile_1 = new java.io.File(currentPath + fileName + ".meta");
                metaFile.renameTo(metaFile_1);
            }
            RandomAccessFile in = new RandomAccessFile(
                    metaFile, "rws"
            );
            String str = null;
            while ((str = in.readLine()) != null) {
                metaString += str;
                metaString += "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.GET_META_DATA_FAILED);
        }
        buffer.put(fileName, metaString);
        return metaString;
    }

    public FileManagerServer(Id fileManagerId, int port) throws RemoteException{
        super(port);
        this.fileManager = new AFSFileManager(fileManagerId);
    }

    protected FileManagerServer(int port) throws RemoteException {
        super(port);
    }

    public void writeMetaFile(String meta, String fileName) throws ErrorCode,RemoteException {
        LinkedHashMap<String,String> bufferBackup = new LinkedHashMap<>(buffer);
        try {
            //System.out.println(manager);
            String pathTempMeta = "src" + java.io.File.separator + ((FileManagerId)fileManager.getManagerId()).getId() +
                    java.io.File.separator + fileName + "_temp.meta";
            String pathMeta = "src" + java.io.File.separator + ((FileManagerId)fileManager.getManagerId()).getId() +
                    java.io.File.separator + fileName + ".meta";
            java.io.File newMeta = new java.io.File(pathTempMeta);
            boolean r = newMeta.createNewFile();
            while(!r){
                r = newMeta.createNewFile();
            }
            RandomAccessFile out = new RandomAccessFile(pathTempMeta,"rws");
            out.seek(0);
            out.write(meta.getBytes());
            java.io.File oldMeta = new java.io.File(pathMeta);
            boolean r1 = oldMeta.delete();
            out.close();
            //如果在这里断电了，文件系统重启后发现meta文件不存在会检查是否有_temp.meta文件，如果有的话就把它写进meta
            if(r1) {
                boolean r2 = newMeta.renameTo(oldMeta);
                if(!r2){
                    oldMeta.createNewFile();
                    RandomAccessFile out2 = new RandomAccessFile(pathMeta, "rws");
                    out2.seek(0);
                    //这个时候oldMeta被清空，newMeta未被重命名成功，若在这里断电了则重启后会检查到newMeta的存在，则会把newMeta内的文件取出
                    out2.write(meta.getBytes());
                }
            }else{
                RandomAccessFile out2 = new RandomAccessFile(pathMeta, "rws");
                out2.seek(0);
                //同理，这个时候oldMeta被清空，newMeta未被删除，若在这里断电了则重启后会检查到newMeta的存在，则会把newMeta内的文件取出
                //我也不想先删除那个oldMeta不过java似乎如果想要重命名成功就必须先删除，然后我在java.io.File类里也没有找到link相关的方法…
                out2.write(meta.getBytes());
                newMeta.delete();
            }
            bufferBackup.remove(fileName);
            bufferBackup.put(fileName, meta);
            buffer = bufferBackup;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("write meta file failed");
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
   }

    @Override
    public File getFile(Id fileId) {
        String meta = getMetaDataFromFile(((FileId)fileId).getId());
        return fileManager.getFile(fileId, meta);
    }

    @Override
    public File newFile(Id fileId) {
        return fileManager.newFile(fileId);
    }

    @Override
    public AFSFile getRemoteFile(Id fileId) throws RemoteException {
        /*try {
            sleep(2000);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        return (AFSFile) this.getFile(fileId);
    }

    @Override
    public AFSFile newRemoteFile(Id fileId) throws RemoteException {
        return (AFSFile) this.newFile(fileId);
    }

    public void copyFile(String originMetaPath, String metaPath) throws RemoteException, IOException {
        BufferedInputStream originInput = new BufferedInputStream(new FileInputStream(originMetaPath));
        RandomAccessFile out = new RandomAccessFile(new java.io.File(metaPath),"rws");
        byte[] bytes = new byte[2048];
        int n = -1;
        while ((n = originInput.read(bytes,0,bytes.length)) != -1) {
            String str = new String(bytes,0,n,"GBK");
            System.out.println(str);
            out.write(bytes, 0, n);
        }
        originInput.close();
        out.close();
    }
}
