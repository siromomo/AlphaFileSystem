package AlphaFileSystem.File;

import AlphaFileSystem.*;

public class FileManagerId implements Id {
    private String id;
    public FileManagerId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
