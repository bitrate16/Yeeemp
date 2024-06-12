/**
 * Copyright 2024 pegasko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package art.pegasko.yeeemp.impl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import art.pegasko.yeeemp.ui.activity.Utils;

public class DataUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";

    public static String formatTs(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(DataUtils.DATE_FORMAT);
        return sdf.format(timestamp);
    }

    private static void copyStream(InputStream fis, OutputStream fos) throws IOException {
        byte[] buffer = new byte[1024];
        int lengthRead;
        while ((lengthRead = fis.read(buffer)) > 0) {
            fos.write(buffer, 0, lengthRead);
            fos.flush();
        }
    }

    /* just copy internal db to external storage */
    public static void exportDatabase(Context context, File directory) throws Exception {
        directory.mkdirs();

        File internalFile = DBWrapper.getDBPath(context);
        File externalFile = new File(directory, "export_" + formatTs(System.currentTimeMillis()) + ".db");
        FileInputStream fis = new FileInputStream(internalFile);
        FileOutputStream fos = new FileOutputStream(externalFile);

        copyStream(fis, fos);

        fis.close();
        fos.close();
    }

    /* just copy internal db to external storage */
    public static void exportDatabase(Context context, Uri uri) throws Exception {
        File internalFile = DBWrapper.getDBPath(context);
        InputStream fis = new FileInputStream(internalFile);
        OutputStream fos = context.getContentResolver().openOutputStream(uri);

        copyStream(fis, fos);

        fis.close();
        fos.close();
    }

    /* just copy external db to internal storage */
    public static void importDatabase(Context context, Uri uri) throws Exception {
        File internalFile = DBWrapper.getDBPath(context);
        InputStream fis = context.getContentResolver().openInputStream(uri);
        OutputStream fos = new FileOutputStream(internalFile);

        copyStream(fis, fos);

        fis.close();
        fos.close();
    }

    public static void backupDatabase(Context context) throws IOException {
        File internalFile = DBWrapper.getDBPath(context);
        File backupFile = new File(context.getFilesDir(), "backup.db");

        FileInputStream fis = new FileInputStream(internalFile);
        FileOutputStream fos = new FileOutputStream(backupFile);

        copyStream(fis, fos);

        fis.close();
        fos.close();
    }

    public static void restoreDatabase(Context context) throws IOException {
        File internalFile = DBWrapper.getDBPath(context);
        File backupFile = new File(context.getFilesDir(), "backup.db");

        FileInputStream fis = new FileInputStream(backupFile);
        FileOutputStream fos = new FileOutputStream(internalFile);

        copyStream(fis, fos);

        fis.close();
        fos.close();
    }

    public static void deleteBackupDatabase(Context context) throws IOException {
        File backupFile = new File(context.getFilesDir(), "backup.db");
        backupFile.delete();
    }
}
