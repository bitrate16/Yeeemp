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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import art.pegasko.yeeemp.base.EventMaker;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.base.QueueMaker;
import art.pegasko.yeeemp.base.TagMaker;

public class DBWrapper extends Wrapper {
    public static final String TAG = DBWrapper.class.getSimpleName();
    public static final boolean DEBUG = false;

    public DBWrapper(Context context) {
        this.db = openDB(context, DB_PATH);
        this.queueMaker = new QueueMakerImpl(this.db);
        this.eventMaker = new EventMakerImpl(this.db);
        this.tagMaker = new TagMakerImpl(this.db);
    }

    // Fields
    SQLiteDatabase db;
    QueueMaker queueMaker;
    EventMaker eventMaker;
    TagMaker tagMaker;

    public QueueMaker queueMaker() {
        return this.queueMaker;
    }

    public EventMaker eventMaker() {
        return this.eventMaker;
    }

    public TagMaker tagMaker() {
        return this.tagMaker;
    }

    /**
     * SQLite initializer script
     */
    // @formatter:off
    private static final String[] INIT_SQL = new String[] {
        "CREATE TABLE IF NOT EXISTS tag (" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    queue_id INTEGER," +
        "    name TEXT" +
        ");",

        "CREATE INDEX IF NOT EXISTS tag__queue_id ON tag(queue_id);",

        "CREATE INDEX IF NOT EXISTS tag__name ON tag(name);",

        "CREATE TABLE IF NOT EXISTS event (" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    timestamp INTEGER," +
        "    comment TEXT" +
        ");",

        "CREATE TABLE IF NOT EXISTS queue (" +
        "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    name TEXT" +
        ");",

        "CREATE TABLE IF NOT EXISTS event_tag (" +
        "    event_id INTEGER," +
        "    tag_id INTEGER" +
        ");",

        "CREATE INDEX IF NOT EXISTS event_tag__event_id_tag_id ON event_tag(event_id, tag_id);",

        "CREATE INDEX IF NOT EXISTS event_tag__event_id ON event_tag(event_id);",

        "CREATE INDEX IF NOT EXISTS event_tag__tag_id ON event_tag(tag_id);",

        "CREATE TABLE IF NOT EXISTS queue_event (" +
        "    queue_id INTEGER," +
        "    event_id INTEGER" +
        ");",

        "CREATE INDEX IF NOT EXISTS queue_event__event_id_tag_id ON queue_event(queue_id, event_id);",

        "CREATE INDEX IF NOT EXISTS queue_event__event_id ON queue_event(queue_id);",

        "CREATE INDEX IF NOT EXISTS queue_event__tag_id ON queue_event(event_id);"
    };

    private static String DB_PATH = "database.db";

    /**
     * Initialize database object
     *
     * @param db
     */
    private static void initDB(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Creating database");
            for (String query: INIT_SQL) {
                Log.d(TAG, query);
                db.execSQL(query);
            }
            Log.d(TAG, "Database created");
        } catch (SQLiteException e) {
            Log.wtf(TAG, e);
        }
    }

    /**
     * Get internal database path
     */
    static File getDBPath(Context context) {
        return new File(context.getFilesDir(), DB_PATH);
    }

    /**
     * @return opened and initialized database
     */
    private static SQLiteDatabase openDB(Context context, String dbPath) {
        if (DBWrapper.DEBUG) {
            try {
                new File(context.getFilesDir(), dbPath).delete();
            } catch (Exception e) {
                Log.wtf(TAG, e);
            }
        }

        File path = getDBPath(context);
        SQLiteDatabase db;
        if (!path.exists()) {
            db = SQLiteDatabase.openDatabase(path.getPath(), null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.CREATE_IF_NECESSARY);
        } else {
            db = SQLiteDatabase.openDatabase(path.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        }

        initDB(db);
        return db;
    }
}
