package AlphaFileSystem.Block;

import AlphaFileSystem.ErrorCode;
import AlphaFileSystem.Id;
import java.io.*;

public class AFSBlockManager implements BlockManager,Serializable {
    private BlockManagerId blockManagerId;

    public String getName(){
        return blockManagerId.getId();
    }

    private int getCount(){
        File index = new File("src/" + blockManagerId.getId());
        if(!index.exists()){
            System.out.println("block manager index doesn't exist");
            return -1;
        }
        for(File f : index.listFiles()){
            if(f.getName().equals("id.count")){
                try{
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream("src/" + blockManagerId.getId() + "/id.count")
                        )
                    );
                    int count =  Integer.parseInt(reader.readLine());
                    BufferedWriter writer = new BufferedWriter(
                            new FileWriter("src/" + blockManagerId.getId() + "/id.count")
                    );
                    writer.write("" + (count + 1));
                    writer.close();
                    return count;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        File countFile = new File("src/" + blockManagerId.getId() + "/id.count");
        try{
            countFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(countFile.getPath())
                    )
            );
            writer.write("1");
          //  System.out.println("writer has write in block id.count file, path is " + countFile.getPath());
            writer.close();
            return 0;
        }catch (Exception e){
            e.printStackTrace();
            throw new ErrorCode(7);
        }
    }

    public AFSBlockManager(String id){
        this.blockManagerId = new BlockManagerId(id);
    }

    public BlockManagerId getBlockManagerId() {
        return blockManagerId;
    }

    public void setBlockManagerId(BlockManagerId blockManagerId) {
        this.blockManagerId = blockManagerId;
    }

    @Override
    public Block getBlock(Id indexId) {
        return new PhysicalBlock(this, blockManagerId.getId(),(BlockId)indexId);
    }

    @Override
    public Block newBlock(byte[] b) {
        return new PhysicalBlock(b, blockManagerId.getId(), getCount(), this);
    }

    public Block newBlockDelayedWrite(byte[] b){
        return new PhysicalBlock(b, blockManagerId.getId(), getCount(), this, true);
    }

    public void copyFile(String originMetaPath, String metaPath) throws Exception{
        BufferedInputStream originInput = new BufferedInputStream(new FileInputStream(originMetaPath));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(metaPath));
        byte[] bytes = new byte[2048];
        int n = -1;
        while ((n = originInput.read(bytes,0,bytes.length)) != -1) {
            String str = new String(bytes,0,n,"GBK");
            System.out.println(str);
            out.write(bytes, 0, n);
        }
        out.flush();
        originInput.close();
        out.close();
    }
}
