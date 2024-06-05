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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.TagMaker;

public class TagMakerImpl implements TagMaker {
    public static final String TAG = TagMakerImpl.class.getSimpleName();

    private SQLiteDatabase db;

    public TagMakerImpl(SQLiteDatabase db) {
        this.db = db;
    }

    /* !synchronized */
    private Tag getExisting(Queue queue, String name) {
        try {
            Cursor cursor = db.query(
                "tag",
                new String[] { "id", "name" },
                "query_id = ? AND name = ?",
                new String[] { Integer.toString(queue.getId()), name },
                null,
                null,
                null
            );
            if (Utils.findResult(cursor)) {
                return new TagImpl(
                    db,
                    queue,
                    cursor.getInt(0)
                );
            }
        } catch (SQLiteException e) {
            Log.w(TAG, e);
        }

        return null;
    }

    /* !synchronized */
    private boolean create(Queue queue, String name) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("queue_id", queue.getId());
            cv.put("name", name);
            db.insertOrThrow("tag", null, cv);
        } catch (SQLiteException e) {
            Log.w(TAG, e);
            return false;
        }
        return true;
    }

    @Override
    public Tag getOrCreateInQueue(Queue queue, String name) {
        synchronized (db) {
            name = name.trim().toLowerCase();

            // Try get existing
            Tag existingTag = getExisting(queue, name);
            if (existingTag != null)
                return existingTag;

            // Create new
            create(queue, name);

            // Finally get
            return getExisting(queue, name);
        }
    }
}
