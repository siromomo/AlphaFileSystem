package AlphaFileSystem.File;

import AlphaFileSystem.*;

import java.io.Serializable;

public class FileId implements Id, Serializable {
    private String id;
    private String managerId;

    public FileId(String id){
        this.id = id;
    }
    public FileId(String id, String managerId){
        this.id = id;
        this.managerId = managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
