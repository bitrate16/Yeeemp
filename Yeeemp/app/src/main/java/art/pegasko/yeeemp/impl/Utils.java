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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Utils {
    public static boolean findResultAndClose(Cursor cursor) {
        if (cursor == null)
            return false;

        boolean hasResult = cursor.moveToFirst();
        if (!hasResult) {
            cursor.close();
            return false;
        }

        boolean result = cursor.getCount() != 0;
        cursor.close();

        return result;
    }

    public static int getIntAndClose(Cursor cursor, int def) {
        if (cursor == null)
            return def;

        boolean hasResult = cursor.moveToFirst();
        if (!hasResult) {
            cursor.close();
            return def;
        }

        if (cursor.getCount() == 0) {
            cursor.close();

            return def;
        }

        int result = cursor.getInt(0);
        cursor.close();

        return result;
    }

    public static long getLongAndClose(Cursor cursor, long def) {
        if (cursor == null)
            return def;

        boolean hasResult = cursor.moveToFirst();
        if (!hasResult) {
            cursor.close();
            return def;
        }

        if (cursor.getCount() == 0) {
            cursor.close();

            return def;
        }

        long result = cursor.getLong(0);
        cursor.close();

        return result;
    }

    public static String getStringAndClose(Cursor cursor, String def) {
        if (cursor == null)
            return def;

        boolean hasResult = cursor.moveToFirst();
        if (!hasResult) {
            cursor.close();
            return def;
        }

        if (cursor.getCount() == 0) {
            cursor.close();

            return def;
        }

        String result = cursor.getString(0);
        cursor.close();

        return result;
    }
}
