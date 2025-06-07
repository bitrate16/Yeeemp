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
import art.pegasko.yeeemp.base.EventOrder;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.TagStat;

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

            return Utils.getStringAndClose(cursor, null);
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
    public Event[] getEvents(EventOrder.Order order) {
        synchronized (this.db) {
            Cursor cursor;

            // Requires JOIN
            if ((order == EventOrder.Order.TIMESTAMP_ASC) || (order == EventOrder.Order.TIMESTAMP_DESC)) {
                String query = (
                    "select\n" +
                    "    queue_event.event_id\n" +
                    "from\n" +
                    "    queue_event\n" +
                    "left join\n" +
                    "    event\n" +
                    "on\n" +
                    "    queue_event.event_id = event.id\n" +
                    "where\n" +
                    "    queue_event.queue_id = ?\n" +
                    "order by\n" +
                    "    timestamp "
                );

                if (order == EventOrder.Order.TIMESTAMP_ASC) {
                    query += "asc";
                } else {
                    query += "desc";
                }
                cursor = db.rawQuery(
                    query,
                    new String[] { Integer.toString(this.getId()) }
                );
            } else {
                cursor = db.query(
                    "queue_event",
                    new String[] { "event_id" },
                    "queue_id = ?",
                    new String[] { Integer.toString(this.getId()) },
                    null,
                    null,
                    ( order == EventOrder.Order.ID_ASC ?
                        "event_id asc" :
                    ( order == EventOrder.Order.ID_DESC ?
                        "event_id desc" :
                        null
                    ))
                );
            }

            if (cursor == null) {
                return new Event[0];
            }

            Event[] events = new Event[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                events[index++] = new EventImpl(this.db, cursor.getInt(0));
            }
            cursor.close();

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

            return Utils.getIntAndClose(cursor, 0);
        }
    }

    /**
     * !synchronized
     */
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

            return Utils.findResultAndClose(cursor);
        }
    }

    @Override
    public void addEvent(Event event) {
        synchronized (this.db) {
            if (event == null) return;

            if (this.hasEvent(event)) return;

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
            if (event == null) return;

            if (this.hasEvent(event)) return;

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
    public TagStat[] getGlobalTags() {
        synchronized (this.db) {
            Cursor cursor = db.rawQuery(
                "select" +
                "    tag_id,\n" +
                "    count(*) as cnt\n" +
                "from (\n" +
                "    select\n" +
                "        event_id,\n" +
                "        tag_id\n" +
                "    from (\n" +
                "        select\n" +
                "            event_tag.event_id as event_id,\n" +
                "            event_tag.tag_id as tag_id\n" +
                "        from (\n" +
                "            select\n" +
                "                event_id\n" +
                "            from\n" +
                "                queue_event\n" +
                "            where\n" +
                "                queue_id = ?\n" +
                "        ) as queue_event_temp\n" +
                "        inner join\n" +
                "            event_tag\n" +
                "        on\n" +
                "            (event_tag.event_id = queue_event_temp.event_id)\n" +
                "    )\n" +
                "    group by\n" +
                "        event_id,\n" +
                "        tag_id\n" +
                ")\n" +
                "group by\n" +
                "    tag_id\n" +
                "order by\n" +
                "    cnt desc",
                new String[] { Integer.toString(this.getId()) }
            );

            if (cursor == null) {
                return new TagStat[0];
            }

            TagStat[] tags = new TagStat[cursor.getCount()];

            int index = 0;
            while (cursor.moveToNext()) {
                TagStat tagStat = new TagStat();
                tags[index++] = tagStat;

                tagStat.tag = new TagImpl(this.db, cursor.getInt(0));
                tagStat.count = cursor.getInt(1);
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
