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

package art.pegasko.yeeemp.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TimeUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.MultiAutoCompleteTextView;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.TagStat;
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
    private TagStat[] tagStat;

    /**
     * Store values for edited or new event
     */
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
                for (Tag tag : this.event.getTags()) {
                    this.eventContainer.tags.add(tag.getName());
                }
            }
        }

        binding = ActivityEventEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(queue.getName() + " / " + (event == null ? "Create" : "Edit") + " Event");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (this.eventContainer.timestamp != 0)
            binding.eventEditContent.eventEditTimestamp.setText(Utils.formatTs(this.eventContainer.timestamp));
        if (this.eventContainer.comment != null)
            binding.eventEditContent.eventEditComment.setText(this.eventContainer.comment);

        /* Comment Listeners */

        // Request focus on click
        this.binding.eventEditContent.eventEditContainerComment.setOnClickListener((View view) -> {
            EventEditActivity.this.binding.eventEditContent.eventEditComment.requestFocus();
            EventEditActivity.this.binding.eventEditContent.eventEditComment.setSelection(EventEditActivity.this.binding.eventEditContent.eventEditComment.getText().length());
            InputMethodManager imm = (InputMethodManager) EventEditActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(EventEditActivity.this.binding.eventEditContent.eventEditComment, InputMethodManager.SHOW_IMPLICIT);
        });

        /* Timestamp Listeners */

        binding.eventEditContent.eventEditContainerTimestamp.setOnClickListener((View view) -> {
            Date date;
            if (this.eventContainer.timestamp != 0) date = new Date(this.eventContainer.timestamp);
            else date = new Date();

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                view.getContext(),
                (DatePicker datePicker, int year, int month, int day) -> {
                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                        view.getContext(),
                        (TimePicker timePicker, int hour, int minute) -> {
                            Date newDate = new Date();
                            newDate.setYear(
                                year - 1900);
                            newDate.setMonth(
                                month);
                            newDate.setDate(
                                day);
                            newDate.setHours(
                                hour);
                            newDate.setMinutes(
                                minute);
                            newDate.setSeconds(
                                0);

                            this.eventContainer.timestamp = newDate.getTime();
                            binding.eventEditContent.eventEditTimestamp.setText(
                                Utils.formatTs(
                                    this.eventContainer.timestamp));
                        },
                        date.getHours(),
                        date.getMinutes(),
                        true
                    );
                    timePickerDialog.show();
                },
                date.getYear() + 1900,
                date.getMonth(),
                date.getDate()
            );
            datePickerDialog.show();
        });

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Save Event", Snackbar.LENGTH_LONG).setAnchorView(R.id.fab).setAction(
                "Action",
                null
            ).show();

            return true;
        });
        binding.fab.setOnClickListener(view -> {
            Utils.hapticTick(view);

            // Finalize values
            this.eventContainer.comment = this.binding.eventEditContent.eventEditComment.getText().toString().trim();

            String[] tags = EventEditActivity.this.binding.eventEditContent.eventEditTags.getText().toString().split(",");
            tags = Utils.orderedDeduplicateIgnoreCaseAndTrim(tags);
            this.eventContainer.tags.clear();
            for (String tag : tags) {
                this.eventContainer.tags.add(tag);
            }

            // Fill event
            boolean hasEvent = this.event != null;
            if (this.event == null) this.event = Wrapper.getEventMaker().create();

            this.event.setTimestamp(this.eventContainer.timestamp);
            this.event.removeTags();
            for (String tag : this.eventContainer.tags) {
                this.event.addTag(Wrapper.getTagMaker().getOrCreateInQueue(this.queue, tag));
            }
            this.event.setComment(this.eventContainer.comment);

            if (!hasEvent) {
                this.queue.addEvent(event);
            }

            finish();
        });

        /* Tags list + input */
        this.tagStat = this.queue.getGlobalTags();

        ArrayAdapter<TagStat> adapter = new EventEditTagsAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            0,
            new ArrayList<>(Arrays.asList(this.tagStat))
        );
        this.binding.eventEditContent.eventEditTags.setAdapter(adapter);
        this.binding.eventEditContent.eventEditTags.setThreshold(1);
        this.binding.eventEditContent.eventEditTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        this.binding.eventEditContent.eventEditTags.setOnItemClickListener((parent, view, position, id) -> {
            String[] tags = EventEditActivity.this.binding.eventEditContent.eventEditTags.getText().toString().split(",");
            tags = Utils.orderedDeduplicateIgnoreCaseAndTrim(tags);

            EventEditActivity.this.binding.eventEditContent.eventEditTags.setText(String.join(", ", tags) + ", ");
            EventEditActivity.this.binding.eventEditContent.eventEditTags.setSelection(EventEditActivity.this.binding.eventEditContent.eventEditTags.getText().length());
        });

        // Request focus on click
        this.binding.eventEditContent.eventEditContainerTags.setOnClickListener((View view) -> {
            this.binding.eventEditContent.eventEditTags.requestFocus();
            this.binding.eventEditContent.eventEditTags.setSelection(EventEditActivity.this.binding.eventEditContent.eventEditTags.getText().length());
            InputMethodManager imm = (InputMethodManager) EventEditActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(EventEditActivity.this.binding.eventEditContent.eventEditTags, InputMethodManager.SHOW_IMPLICIT);
        });

        // Fill
        this.binding.eventEditContent.eventEditTags.setText(String.join(", ", this.eventContainer.tags));
        this.binding.eventEditContent.eventEditTags.setSelection(EventEditActivity.this.binding.eventEditContent.eventEditTags.getText().length());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}