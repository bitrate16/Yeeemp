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

import androidx.annotation.NonNull;

import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import kotlin.NotImplementedError;

public class QueueImpl implements Queue {
    public static final String TAG = QueueImpl.class.getSimpleName();

    private final SQLiteDatabase db;
    private final int id;

    protected QueueImpl(SQLiteDatabase db, int id) {
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
                "queue",
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

    @Override
    public void setName(String name) {
        synchronized (this.db) {
            ContentValues cv = new ContentValues();
            cv.put("name", name);
            db.update(
                "queue",
                cv,
                "id = ?",
                new String[] { Integer.toString(this.getId()) }
            );
        }
    }

    @Override
    public Event[] getEvents() {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "queue_event",
                new String[] { "event_id" },
                "queue_id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                "event_id desc"
            );

            if (cursor == null) {
                return new Event[0];
            }

            Event[] events = new Event[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                events[index++] = new EventImpl(
                    this.db,
                    cursor.getInt(0)
                );
            }

            return events;
        }
    }

    @Override
    public int getEventCount() {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "queue_event",
                new String[] { "count(*)" },
                "queue_id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            if (!Utils.findResult(cursor))
                return 0;

            return cursor.getInt(0);
        }
    }

    /** !synchronized */
    protected boolean hasEvent(Event event) {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "queue_event",
                new String[] { "1" },
                "queue_id = ? AND event_id = ?",
                new String[] { Integer.toString(this.getId()), Integer.toString(event.getId()) },
                null,
                null,
                null
            );

            return Utils.findResult(cursor);
        }
    }

    @Override
    public void addEvent(Event event) {
        synchronized (this.db) {
            if (event == null)
                return;

            if (this.hasEvent(event))
                return;

            try {
                ContentValues cv = new ContentValues();
                cv.put("queue_id", this.getId());
                cv.put("event_id", event.getId());
                db.insertOrThrow("queue_event", null, cv);
            } catch (SQLiteException e) {
                Log.w(TAG, e);
            }
        }
    }

    @Override
    public void removeEvent(Event event) {
        synchronized (this.db) {
            if (event == null)
                return;

            if (this.hasEvent(event))
                return;

            try {
                db.delete(
                    "queue_event",
                    "queue_id = ? AND event_id = ?",
                    new String[] { Integer.toString(this.getId()), Integer.toString(event.getId()) }
                );
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }

    @Override
    public Tag[] getGlobalTags() {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "tag",
                new String[] { "id" },
                "queue_id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            if (cursor == null) {
                return new Tag[0];
            }

            Tag[] tags = new Tag[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                tags[index++] = new TagImpl(
                    this.db,
                    cursor.getInt(0)
                );
            }

            return tags;
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue{id=");
        sb.append(this.getId());
        sb.append(",name=");
        sb.append(this.getName());
        sb.append("}");
        return sb.toString();
    }
}
