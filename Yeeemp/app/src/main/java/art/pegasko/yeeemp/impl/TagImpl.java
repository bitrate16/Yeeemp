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

import androidx.annotation.NonNull;

import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.Queue;

public class TagImpl implements Tag {
    public static final String TAG = TagImpl.class.getSimpleName();

    private final SQLiteDatabase db;
    private final int id;
    private String _cached_name;

    protected TagImpl(SQLiteDatabase db, int id) {
        this.db = db;
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getName() {
        synchronized (this.db) {
            if (this._cached_name != null)
                return this._cached_name;

            Cursor cursor = db.query(
                "tag",
                new String[] { "name" },
                "id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            String result = Utils.getStringAndClose(cursor, null);
            if (result != null) {
                return this._cached_name = result;
            }

            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tag{id=");
        sb.append(this.getId());
        sb.append(",name=");
        sb.append(this.getName());
        sb.append("}");
        return sb.toString();
    }
}
