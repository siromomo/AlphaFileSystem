package AlphaFileSystem.Tools;

import java.io.*;
import java.util.*;

public class ClearIndex {
    public static void main(String[] args){
        File file1 = new File("src/bm-01");
        deleteFiles(file1);
        file1 = new File("src/bm-02");
        deleteFiles(file1);
        file1 = new File("src/bm-03");
        deleteFiles(file1);
        file1 = new File("src/fm-01");
        deleteFiles(file1);
        file1 = new File("src/fm-02");
        deleteFiles(file1);
        file1 = new File("src/bm-01");
        file1.mkdir();
        file1 = new File("src/bm-02");
        file1.mkdir();
        file1 = new File("src/bm-03");
        file1.mkdir();
        file1 = new File("src/fm-01");
        file1.mkdir();
        file1 = new File("src/fm-02");
        file1.mkdir();

    }

    public static void deleteFiles(File srcFile) {

        if (srcFile.exists()) {

            //存放文件夹
            LinkedList<File> directories = new LinkedList<>();
            ArrayList<File> directoryList = new ArrayList<>();

            do {
                File[] files = directories.size() > 0 ? directories.removeFirst().listFiles() : new File[]{srcFile};
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            directories.add(f);
                            directoryList.add(f);
                        } else {
                            f.delete();
                        }
                    }
                }
            } while (directories.size() > 0);

            for (int i = directoryList.size() - 1; i >= 0; i--) {
                directoryList.get(i).delete();
            }
        }
    }
}
