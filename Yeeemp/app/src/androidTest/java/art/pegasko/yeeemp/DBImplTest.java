package art.pegasko.yeeemp;

import android.database.sqlite.SQLiteDatabase;
import android.provider.CalendarContract;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.impl.QueueMakerImpl;
import art.pegasko.yeeemp.impl.TagMakerImpl;

@RunWith(AndroidJUnit4.class)
public class DBImplTest {

    private SQLiteDatabase db;

    @Before
    public void setUp() {
        db = SQLiteDatabase.create(null);
//        db = SQLiteDatabase.openDatabase("test.db", SQLiteDatabase.OpenParams);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testQueueMakerImpl() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        QueueMakerImpl queueMaker = new QueueMakerImpl(db);

        Queue q = queueMaker.create();

        // check initial ID
        assert q.getId() == 0;

        // check null name
        assert q.getName() == null;

        // check no events
        Event[] events = q.getEvents();
        assert events != null;
        assert events.length == 0;

        // check no tags
        Tag[] tags = q.getGlobalTags();
        assert tags != null;
        assert tags.length == 0;

        // Check name set
        q.setName("aboba");
        assert q.getName() == "aboba";
    }

    @Test
    public void testTagMakerImpl() {
    }
}
