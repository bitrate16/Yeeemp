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
import art.pegasko.yeeemp.base.QueueMaker;

public class QueueMakerImpl implements QueueMaker {
    public static final String TAG = EventMakerImpl.class.getSimpleName();

    private SQLiteDatabase db;

    public QueueMakerImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public Queue create() {
        synchronized (this.db) {
            try {
                ContentValues cv = new ContentValues();
                cv.put("id", (Integer) null);
                long rowId = db.insertOrThrow("queue", null, cv);
                return new QueueImpl(
                    this.db,
                    (int) rowId
                );
            } catch (SQLiteException e) {
                Log.w(TAG, e);
            }

            return null;
        }
    }

    @Override
    public Queue[] list() {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "queue",
                new String[] { "id" },
                null,
                null,
                null,
                null,
                null
            );

            if (cursor == null) {
                return new Queue[0];
            }

            Queue[] queues = new Queue[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                queues[index++] = new QueueImpl(
                    this.db,
                    cursor.getInt(0)
                );
            }

            return queues;
        }
    }
}
