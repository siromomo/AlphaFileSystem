package AlphaFileSystem.File;

import AlphaFileSystem.Block.BlockManagerClient;
import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.Id;

import java.io.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.LinkedHashMap;

public class FileManagerClient implements FileManager{
    private IFileManagerServer remoteFm;
    private String managerName;
    private LinkedHashMap<String, String> buffer;
    private LinkedHashMap<String, String> bufferBackup;
    private final static int BUFFER_SIZE = 10;

    public FileManagerClient(String id){
        try{
            buffer = new LinkedHashMap<>();
            bufferBackup = new LinkedHashMap<>(buffer);
            managerName = id;
            remoteFm = (IFileManagerServer) Naming.lookup(BlockManagerClient.getURLFromId(id));
            //Registry registry = LocateRegistry.getRegistry("localhost");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("File manager " + id + " is not available yet");
            throw new ErrorCode(ErrorCode.GET_REMOTE_MANAGER_FAILED);
        }
    }

    public void copyFile(String originMetaPath, String metaPath) throws RemoteException, IOException {
        try {
            remoteFm.copyFile(originMetaPath, metaPath);
        }catch (Exception e){
            e.printStackTrace();
            throw new ErrorCode(ErrorCode.TRANSPORT_DATA_TO_REMOTE_FAILED);
        }
    }

    private void putFileInBuffer(String name, String meta){
        if (buffer.size() >= BUFFER_SIZE) {
            String keyToRemove = "";
            for (String key : buffer.keySet())
                keyToRemove = key;
            buffer.remove(keyToRemove);
            buffer.put(name, meta);
        }else {
            buffer.put(name, meta);
        }
    }

    @Override
    public File getFile(Id fileId) {
        bufferBackup = new LinkedHashMap<>(buffer);
        try {
            if(buffer.get(((FileId)fileId).getId()) != null){
                AFSFile file = new AFSFile(buffer.get(((FileId)fileId).getId()), managerName, (FileId) fileId);
                file.setClientOrServer(AFSFile.CLIENT);
                return file;
            }
            AFSFile file = remoteFm.getRemoteFile(fileId);
            file.setClientOrServer(AFSFile.CLIENT);
            putFileInBuffer(((FileId)fileId).getId(), file.getMetaString());
            //System.out.println(file.getMetaString().length());
            return file;
        }catch (Exception e){
            e.printStackTrace();
            buffer = new LinkedHashMap<>(bufferBackup);
            throw new ErrorCode(ErrorCode.GET_DATA_FROM_REMOTE_FAILED);
        }
    }

    @Override
    public File newFile(Id fileId) {
        bufferBackup = new LinkedHashMap<>(buffer);
        try{
            AFSFile file = remoteFm.newRemoteFile(fileId);
            file.setClientOrServer(AFSFile.CLIENT);
            putFileInBuffer(((FileId)fileId).getId(), file.getMetaString());
            return file;
        }catch (Exception e){
            e.printStackTrace();
            buffer = new LinkedHashMap<>(bufferBackup);
            throw new ErrorCode(ErrorCode.TRANSPORT_DATA_TO_REMOTE_FAILED);
        }
    }

    public void writeMetaFile(String meta, String fileName) throws IOException{
        remoteFm.writeMetaFile(meta, fileName);
    }
}
