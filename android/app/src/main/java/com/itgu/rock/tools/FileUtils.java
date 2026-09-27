package com.itgu.rock.tools;
import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * 根据 Uri 创建一个临时 File，用于上传或缓存
     * @param context 上下文
     * @param uri 文件 Uri
     * @return 临时 File
     * @throws IOException
     */
    public static File getTempFileFromUri(Context context, Uri uri) throws IOException {
        File tempFile = new File(context.getCacheDir(), System.currentTimeMillis() + ".jpg");
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        }
        return tempFile;
    }
}
