package AlphaFileSystem.Block;

import AlphaFileSystem.*;

import java.io.Serializable;

public class BlockId implements Id, Serializable {
    private int count;
    private int id;

    public BlockId(){
        getCount();
    }

    public BlockId(int id){
        this.id = id;
    }

    private void getCount(){
        count = 0;
        id = count++;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
