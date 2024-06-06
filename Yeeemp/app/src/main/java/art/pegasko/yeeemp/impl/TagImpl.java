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

import androidx.annotation.NonNull;

import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.Queue;

public class TagImpl implements Tag {
    public static final String TAG = TagImpl.class.getSimpleName();

    private final SQLiteDatabase db;
    private final int id;

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
            Cursor cursor = db.query(
                "tag",
                new String[] { "name" },
                "id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            if (Utils.findResult(cursor)) {
                return cursor.getString(0);
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
