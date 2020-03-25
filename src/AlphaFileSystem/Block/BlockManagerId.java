package AlphaFileSystem.Block;

import AlphaFileSystem.*;

import java.io.Serializable;

public class BlockManagerId implements Id, Serializable {
    private String id;
    public BlockManagerId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
