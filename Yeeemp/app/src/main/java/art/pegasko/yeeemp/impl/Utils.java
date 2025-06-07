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
