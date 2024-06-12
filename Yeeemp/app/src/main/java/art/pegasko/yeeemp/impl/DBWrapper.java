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
