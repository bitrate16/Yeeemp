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
import art.pegasko.yeeemp.base.Tag;

public class EventImpl implements Event {
    public static final String TAG = EventImpl.class.getSimpleName();

    private final SQLiteDatabase db;
    private final int id;

    protected EventImpl(SQLiteDatabase db, int id) {
        this.db = db;
        this.id = id;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public long getTimestamp() {
        synchronized (this.db) {
            Cursor cursor = db.query("event", new String[] { "timestamp" }, "id = ?",
                                     new String[] { Integer.toString(this.getId()) }, null, null, null
            );

            if (Utils.findResult(cursor)) {
                return cursor.getLong(0);
            }

            return 0;
        }
    }

    @Override
    public void setTimestamp(long timestamp) {
        synchronized (this.db) {
            ContentValues cv = new ContentValues();
            cv.put("timestamp", timestamp);
            db.update("event", cv, "id = ?", new String[] { Integer.toString(this.getId()) });
        }
    }

    @Override
    public String getComment() {
        synchronized (this.db) {
            Cursor cursor = db.query("event", new String[] { "comment" }, "id = ?",
                                     new String[] { Integer.toString(this.getId()) }, null, null, null
            );

            if (Utils.findResult(cursor)) {
                return cursor.getString(0);
            }

            return null;
        }
    }

    @Override
    public void setComment(String comment) {
        synchronized (this.db) {
            ContentValues cv = new ContentValues();
            cv.put("comment", comment);
            db.update("event", cv, "id = ?", new String[] { Integer.toString(this.getId()) });
        }
    }

    /**
     * !synchronized
     */
    protected boolean hasTag(Tag tag) {
        synchronized (this.db) {
            Cursor cursor = db.query("event_tag", new String[] { "1" }, "event_id = ? AND tag_id = ?", new String[] {
                Integer.toString(this.getId()), Integer.toString(tag.getId())
            }, null, null, null);

            return Utils.findResult(cursor);
        }
    }

    @Override
    public void addTag(Tag tag) {
        synchronized (this.db) {
            if (tag == null) return;

            if (this.hasTag(tag)) return;

            try {
                ContentValues cv = new ContentValues();
                cv.put("event_id", this.getId());
                cv.put("tag_id", tag.getId());
                db.insertOrThrow("event_tag", null, cv);
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }

    @Override
    public void removeTag(Tag tag) {
        synchronized (this.db) {
            if (tag == null) return;

            if (!this.hasTag(tag)) return;

            try {
                db.delete("event_tag", "event_id = ? AND tag_id = ?", new String[] {
                    Integer.toString(this.getId()), Integer.toString(tag.getId())
                });
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }

    @Override
    public void removeTags() {
        synchronized (this.db) {
            try {
                db.delete("event_tag", "event_id = ?", new String[] { Integer.toString(this.getId()) });
            } catch (SQLiteException e) {
                Log.wtf(TAG, e);
            }
        }
    }

    @Override
    public Tag[] getTags() {
        synchronized (this.db) {
            Cursor cursor = db.query("event_tag", new String[] { "tag_id" }, "event_id = ?",
                                     new String[] { Integer.toString(this.getId()) }, null, null, "tag_id desc"
            );

            if (cursor == null) {
                return new Tag[0];
            }

            Tag[] tags = new Tag[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                tags[index++] = new TagImpl(this.db, cursor.getInt(0));
            }

            return tags;
        }
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Event{id=");
        sb.append(this.getId());
        sb.append(",timestamp=");
        sb.append(this.getTimestamp());
        sb.append(",comment=");
        sb.append(this.getComment());
        sb.append("}");
        return sb.toString();
    }
}
