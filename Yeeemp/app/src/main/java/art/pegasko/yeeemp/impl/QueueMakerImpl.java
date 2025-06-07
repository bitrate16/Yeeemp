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
