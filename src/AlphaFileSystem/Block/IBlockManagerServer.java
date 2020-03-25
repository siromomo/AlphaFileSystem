package AlphaFileSystem.Block;

import AlphaFileSystem.Id;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IBlockManagerServer extends Remote {
    public PhysicalBlock sendBlock(Id id) throws RemoteException;
    public PhysicalBlock createNewBlock(int size) throws RemoteException;
    public PhysicalBlock newBlockDelayedWrite(byte[] data) throws RemoteException;
    public PhysicalBlock createNewBlockWithData(byte[] data) throws RemoteException;

}
