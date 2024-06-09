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

 package art.pegasko.yeeemp.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TimeUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.databinding.ActivityEventEditBinding;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.impl.Init;

public class EventEditActivity extends AppCompatActivity {
    public static final String TAG = EventEditActivity.class.getSimpleName();

    private Queue queue;
    private Event event;
    private EventContainer eventContainer;
    private ActivityEventEditBinding binding;

    /** Store values for edited or new event */
    private static class EventContainer {
        public String comment;
        public long timestamp;
        public ArrayList<String> tags;

        public EventContainer() {
            this.timestamp = System.currentTimeMillis();
            this.tags = new ArrayList<String>();
            this.comment = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init.initDB(getApplicationContext());

        // Get Queue ID from Intent
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e(TAG, "Missing Intent arguments (queue_id)");
            finish();
            return;
        }

        int queue_id = extras.getInt("queue_id", -1);
        if (queue_id == -1) {
            Log.e(TAG, "Missing Intent arguments (queue_id)");
            finish();
            return;
        }

        queue = Wrapper.getQueueMaker().getById(queue_id);
        if (queue == null) {
            Log.e(TAG, "Missing Intent arguments (queue_id)");
            finish();
            return;
        }

        // New edit container
        this.eventContainer = new EventContainer();

        // Get Event ID from Intent (optional)
        extras = getIntent().getExtras();
        if (extras != null) {
            int event_id = extras.getInt("event_id", -1);
            if (event_id != -1) {
                this.event = Wrapper.getEventMaker().getById(event_id);
                this.eventContainer.timestamp = this.event.getTimestamp();
                this.eventContainer.comment = this.event.getComment();
                for (Tag tag: this.event.getTags()) {
                    this.eventContainer.tags.add(tag.getName());
                }
            }
        }

        binding = ActivityEventEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + " / " + (event == null ? "Create" : "Edit") + " Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (this.eventContainer.timestamp != 0)
            binding.eventEditContent.eventEditTimestamp.setText(Utils.formatTs(this.eventContainer.timestamp));
        if (this.eventContainer.comment != null)
            binding.eventEditContent.eventEditComment.setText(this.eventContainer.comment);

        binding.eventEditContent.eventEditContainerTimestamp.setOnClickListener((View view) -> {
            Date date;
            if (this.eventContainer.timestamp != 0)
                date = new Date(this.eventContainer.timestamp);
            else
                date = new Date();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                view.getContext(),
                (DatePicker datePicker, int year, int month, int day) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        view.getContext(),
                        (TimePicker timePicker, int hour, int minute) -> {
                            Date newDate = new Date();
                            newDate.setYear(year - 1900);
                            newDate.setMonth(month);
                            newDate.setDate(day);
                            newDate.setHours(hour);
                            newDate.setMinutes(minute);
                            newDate.setSeconds(0);

                            this.eventContainer.timestamp = newDate.getTime();
                            binding.eventEditContent.eventEditTimestamp.setText(Utils.formatTs(this.eventContainer.timestamp));
                        },
                        date.getHours(), date.getMinutes(), true
                    );
                    timePickerDialog.show();
                }, date.getYear() + 1900, date.getMonth(), date.getDate()
            );
            datePickerDialog.show();
        });

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Save Event", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show();

            return true;
        });
        binding.fab.setOnClickListener(view -> {
            // Finalize values
            this.eventContainer.comment = this.binding.eventEditContent.eventEditComment.getText().toString().trim();

            // Fill event
            if (this.event == null)
                this.event = Wrapper.getEventMaker().create();

            this.event.setTimestamp(this.eventContainer.timestamp);
            this.event.removeTags();
            for (String tag: this.eventContainer.tags) {
                this.event.addTag(Wrapper.getTagMaker().getOrCreateInQueue(this.queue, tag));
            }
            this.event.setComment(this.eventContainer.comment);

            finish();
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}