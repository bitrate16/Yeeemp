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
import art.pegasko.yeeemp.base.EventMaker;
import art.pegasko.yeeemp.base.Queue;

public class EventMakerImpl implements EventMaker {
    public static final String TAG = EventMakerImpl.class.getSimpleName();

    private SQLiteDatabase db;

    public EventMakerImpl(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public Event getById(int id) {
        synchronized (this.db) {
            try {
                Cursor cursor = db.query(
                    "event",
                    new String[] { "1" },
                    "id = ?",
                    new String[] { Integer.toString(id) },
                    null,
                    null,
                    null
                );

                if (Utils.findResultAndClose(cursor)) {
                    return new EventImpl(this.db, id);
                }

            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }

            return null;
        }
    }

    @Override
    public Event create() {
        synchronized (this.db) {
            try {
                ContentValues cv = new ContentValues();
                cv.put("id", (Integer) null);
                long rowId = db.insertOrThrow("event", null, cv);
                return new EventImpl(this.db, (int) rowId);
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }

            return null;
        }
    }

    @Override
    public void delete(Event event) {
        synchronized (this.db) {
            try {
                // Delete queue <-> event
                db.delete("queue_event", "event_id = ?", new String[] { Integer.toString(event.getId()) });

                // Delete event <-> tag
                db.delete("event_tag", "event_id = ?", new String[] { Integer.toString(event.getId()) });

                // Delete event
                db.delete("event", "id = ?", new String[] { Integer.toString(event.getId()) });
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }
}
