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
    public Event createInQueue(Queue queue) {
        synchronized (this.db) {
            try {
                ContentValues cv = new ContentValues();
                cv.put("queue_id", queue.getId());
                long rowId = db.insertOrThrow("event", null, cv);
                return new EventImpl(
                    this.db,
                    queue,
                    (int) rowId
                );
            } catch (SQLiteException e) {
                Log.w(TAG, e);
            }

            return null;
        }
    }
}
