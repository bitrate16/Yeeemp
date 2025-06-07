/**
 * Yeeemp - tag based event counter
 * Copyright (C) 2024-2025  pegasko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 * Notwithstanding the freedoms granted by the AGPL 3.0 license, the following restrictions apply:
 *
 * Commercial usage of any kind of the project source code and/or project build artifacts (binaries, executables, packages, archives, libraries and/or any other artifacts) is strictly prohibited.
 *
 * Using the project source code and/or project build artifacts (binaries, executables, packages, archives, libraries and/or any other artifacts) for AI (Artificial Intelligence)/ML (Machine Learning)/NN (Neural Network) and/or any other kind of machine learning algorhitms and systems training is strictly prohibited.
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

import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.ui.activity.Utils;

// TODO: Better data management API
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

    public static void closeDatabase() {
        // Drop current database
        ((DBWrapper) DBWrapper.instance()).db.close();
        Wrapper.setInstance(null);
    }

    public static void deleteDatabase(Context context) throws IOException {
        File internalFile = DBWrapper.getDBPath(context);
        internalFile.delete();
    }
}
