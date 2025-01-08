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

import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.EventOrder;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.QueueMaker;
import art.pegasko.yeeemp.base.QueueOrder;

public class QueueMakerImpl implements QueueMaker {
    public static final String TAG = EventMakerImpl.class.getSimpleName();

    private SQLiteDatabase db;

    public QueueMakerImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public Queue getById(int id) {
        synchronized (this.db) {
            try {
                Cursor cursor = db.query(
                    "queue",
                    new String[] { "1" },
                    "id = ?",
                    new String[] { Integer.toString(id) },
                    null,
                    null,
                    null
                );

                if (Utils.findResultAndClose(cursor)) {
                    return new QueueImpl(this.db, id);
                }

            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }

            return null;
        }
    }

    @Override
    public Queue create() {
        synchronized (this.db) {
            try {
                ContentValues cv = new ContentValues();
                cv.put("id", (Integer) null);
                long rowId = db.insertOrThrow("queue", null, cv);
                return new QueueImpl(this.db, (int) rowId);
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }

            return null;
        }
    }

    @Override
    public Queue[] list(QueueOrder.Order order) {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "queue",
                new String[] { "id" },
                null,
                null,
                null,
                null,
                ( order == QueueOrder.Order.ID ?
                    "rowid" :
                ( order == QueueOrder.Order.NAME ?
                    "name" :
                    null
                ))
            );

            if (cursor == null) {
                return new Queue[0];
            }

            Queue[] queues = new Queue[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                queues[index++] = new QueueImpl(this.db, cursor.getInt(0));
            }
            cursor.close();

            return queues;
        }
    }

    @Override
    public void delete(Queue queue) {
        synchronized (this.db) {

            // Drop events
            try {
                // TODO: Foreign key for cascade delete
                for (Event event : queue.getEvents(EventOrder.Order.ID_DESC)) {
                    db.delete("event", "id = ?", new String[] { Integer.toString(event.getId()) });
                }
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
                return;
            }

            // Drop queue <-> event
            try {
                db.delete("queue_event", "queue_id = ?", new String[] { Integer.toString(queue.getId()) });
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }

            // Drop queue
            try {
                db.delete("queue", "id = ?", new String[] { Integer.toString(queue.getId()) });
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }
}
