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
                "queue_id = ? AND name = ?",
                new String[] { Integer.toString(queue.getId()), name },
                null,
                null,
                null
            );

            int id = Utils.getIntAndClose(cursor, -1);
            if (id != -1) {
                return new TagImpl(
                    db,
                    id
                );
            }
        } catch (SQLiteException e) {
            Log.wtf(TAG, e);
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
            Log.wtf(TAG, e);
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
