package com.andrewyunt.warfare.utilities;

import com.andrewyunt.warfare.Warfare;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileNotFoundException;

public class FileUtils {

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (String child : dir.list()) {
                deleteDir(new File(dir, child));
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }

    public static void copy(File src, File dest) throws IOException {
        if (src.isDirectory()) {
            if (!dest.exists()) {
                dest.mkdirs();
            }
            String[] files = src.list();
            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                copy(srcFile, destFile);
            }
        } else {
            InputStream in;
            Object out;
            try {
                in = new FileInputStream(src);
                out = new FileOutputStream(dest);
            } catch (FileNotFoundException e) {
                Warfare.getInstance().getLogger().severe(src.getName() + " could not be found.");
                return;
            }
            byte[] buffer = new byte['?'];
            int length;
            while ((length = in.read(buffer)) > 0) {
                ((OutputStream) out).write(buffer, 0, length);
            }
            in.close();
            ((OutputStream) out).close();
        }
    }
}