package AlphaFileSystem.File;

import AlphaFileSystem.Id;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IFileManagerServer extends Remote {
    public AFSFile getRemoteFile(Id fileId) throws RemoteException;
    public AFSFile newRemoteFile(Id fileId) throws RemoteException;
    public void copyFile(String originMetaPath, String metaPath) throws RemoteException, IOException;
    public void writeMetaFile(String meta, String fileName) throws RemoteException;
}
