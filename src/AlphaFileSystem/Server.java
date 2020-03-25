package AlphaFileSystem;

import AlphaFileSystem.Block.*;
import AlphaFileSystem.File.*;

import java.rmi.Naming;
import java.rmi.registry.*;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;

class ManagerThread extends Observable implements Runnable {
    private String managerName;
    private int managerType;
    public static int BLOCK_MANAGER = 1;
    public static int FILE_MANAGER = 2;
    public static int port = 11451;

    public static String brokenManager = "";
    public static int brokenManagerType = 0;

    public ManagerThread(String managerName, int managerType) {
        this.managerName = managerName;
        this.managerType = managerType;
    }

    public void doBusiness(){
        if(true){
            super.setChanged();
        }
        notifyObservers();
    }


    public void run() {
        try {
            if (managerType == BLOCK_MANAGER) {
                BlockManagerServer blockManagerServer = new BlockManagerServer(managerName, port);
                Naming.rebind("rmi://127.0.0.1:" + port + "/" + managerName, blockManagerServer);
            }else{
                FileManagerServer fileManagerServer = new FileManagerServer(new FileManagerId(managerName), port);
                Naming.rebind("rmi://127.0.0.1:" + port + "/" + managerName, fileManagerServer);
            }
        } catch (Exception e) {
            brokenManager = managerName;
            brokenManagerType = managerType;
            doBusiness();
            e.printStackTrace();
        }
    }
}

class Listener implements Observer {

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("ManagerThread死机");
        ManagerThread managerThread = new ManagerThread(ManagerThread.brokenManager, ManagerThread.brokenManagerType);
        managerThread.addObserver(this);
        new Thread(managerThread).start();
        System.out.println("ManagerThread重启");

    }

}

public class Server {
    public static void main(String[] args) {
        ManagerThread bm1 = new ManagerThread("bm-01", ManagerThread.BLOCK_MANAGER);
        ManagerThread bm2 = new ManagerThread("bm-02", ManagerThread.BLOCK_MANAGER);
        ManagerThread bm3 = new ManagerThread("bm-03", ManagerThread.BLOCK_MANAGER);
        ManagerThread fm1 = new ManagerThread("fm-01", ManagerThread.FILE_MANAGER);
        ManagerThread fm2 = new ManagerThread("fm-02", ManagerThread.FILE_MANAGER);
        Listener listener = new Listener();
        bm1.addObserver(listener);
        bm2.addObserver(listener);
        bm3.addObserver(listener);
        fm1.addObserver(listener);
        fm2.addObserver(listener);
        try {
            LocateRegistry.createRegistry(11451);
            bm1.run();
            bm2.run();
            bm3.run();
            fm1.run();
            fm2.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
