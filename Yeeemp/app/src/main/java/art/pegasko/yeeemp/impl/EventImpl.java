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

import androidx.annotation.NonNull;

import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Tag;

public class EventImpl implements Event {
    public static final String TAG = EventImpl.class.getSimpleName();

    private final SQLiteDatabase db;
    private final int id;
    private Tag[] cachedTags;

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
            Cursor cursor = db.query(
                "event",
                new String[] { "timestamp" },
                "id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            return Utils.getLongAndClose(cursor, 0);
        }
    }

    @Override
    public void setTimestamp(long timestamp) {
        synchronized (this.db) {
            ContentValues cv = new ContentValues();
            cv.put("timestamp", timestamp);

            db.update(
                "event",
                cv,
                "id = ?",
                new String[] { Integer.toString(this.getId()) }
            );
        }
    }

    @Override
    public String getComment() {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "event",
                new String[] { "comment" },
                "id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                null
            );

            return Utils.getStringAndClose(cursor, null);
        }
    }

    @Override
    public void setComment(String comment) {
        synchronized (this.db) {
            ContentValues cv = new ContentValues();
            cv.put("comment", comment);

            db.update(
                "event",
                cv,
                "id = ?",
                new String[] { Integer.toString(this.getId()) }
            );
        }
    }

    /**
     * !synchronized
     */
    protected boolean hasTag(Tag tag) {
        synchronized (this.db) {
            Cursor cursor = db.query(
                "event_tag",
                new String[] { "1" },
                "event_id = ? AND tag_id = ?",
                new String[] {
                    Integer.toString(this.getId()), Integer.toString(tag.getId())
                },
                null,
                null,
                null
            );

            return Utils.findResultAndClose(cursor);
        }
    }

    @Override
    public void addTag(Tag tag) {
        synchronized (this.db) {
            this.cachedTags = null;

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
            this.cachedTags = null;

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
            this.cachedTags = null;

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
            if (this.cachedTags != null)
                return this.cachedTags;

            Cursor cursor = db.query(
                "event_tag",
                new String[] { "tag_id" },
                "event_id = ?",
                new String[] { Integer.toString(this.getId()) },
                null,
                null,
                "tag_id desc"
            );

            if (cursor == null) {
                return new Tag[0];
            }

            Tag[] tags = new Tag[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                tags[index++] = new TagImpl(this.db, cursor.getInt(0));
            }
            cursor.close();

            return this.cachedTags = tags;
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
