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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.EventOrder;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.ActivityEventListBinding;
import art.pegasko.yeeemp.impl.Init;

public class EventListActivity extends AppCompatActivity {
    public static final String TAG = EventListActivity.class.getSimpleName();

    private static final String PREFS_UI_EVENT_ORDER = "ui-event-order";
    private static final String PREFS_UI_EVENT_ORDER_DEFAULT = "id_desc";

    private ActivityEventListBinding binding;
    private RecyclerView eventList;
    private EventRecyclerViewAdapter eventListAdapter;
    private Queue queue;

    private void updateList() {
        runOnUiThread(() -> {
            eventListAdapter.reloadItems();
        });
    }

    private CharSequence getEventOrderDisplayString(@Nullable EventOrder.Order order) {
        if (order == null) {
            throw new RuntimeException("Order is null");
        }

        switch (order) {
            case ID_ASC:
                return "created ascending";
            case ID_DESC:
                return "created descending";
            case TIMESTAMP_ASC:
                return "date ascending";
            case TIMESTAMP_DESC:
                return "date descending";
            default:
                throw new RuntimeException("Not implemented for " + order);
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

        /* Bindings */
        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + " / " + queue.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* Menu handlers */
        binding.toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == R.id.event_list_toolbar_menu_order) {
                /* Get some options */
                SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
                EventOrder.Order order = EventOrder.orderFromString(prefs.getString(PREFS_UI_EVENT_ORDER, PREFS_UI_EVENT_ORDER_DEFAULT));

                /* Construct options */
                EventOrder.Order[] values = EventOrder.Order.class.getEnumConstants();
                if (values == null) {
                    // How did we get here?
                    throw new RuntimeException("EventOrder.Order.class.getEnumConstants() is Empty");
                }
                CharSequence[] choices = new CharSequence[values.length];
                int currentSelection = -1;

                for (int i = 0; i < values.length; ++i) {
                    choices[i] = this.getEventOrderDisplayString(values[i]);
                    if (values[i] == order) {
                        currentSelection = i;
                    }
                }

                /* Build dialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(EventListActivity.this);
                builder.setTitle("Order by ..");

                builder.setSingleChoiceItems(
                    choices,
                    currentSelection,
                    (dialog, which) -> {
                        EventOrder.Order newOrder = values[which];

                        // Save option
                        prefs.edit().putString(PREFS_UI_EVENT_ORDER, EventOrder.orderToString(newOrder)).apply();

                        // Apply
                        EventListActivity.this.invalidateOptionsMenu();
                        eventListAdapter.setOrder(newOrder);
                        runOnUiThread(() -> {
                            eventListAdapter.reloadItems();
                        });

                        // Close dialog
                        dialog.dismiss();
                    }
                );

                builder.show();

                return true;
            }

            return false;
        });

        /* Get some options */
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
        EventOrder.Order order = EventOrder.orderFromString(prefs.getString(PREFS_UI_EVENT_ORDER, PREFS_UI_EVENT_ORDER_DEFAULT));

        /* Queue list */
        eventListAdapter = new EventRecyclerViewAdapter(queue);
        eventListAdapter.setOrder(order);
        eventList = findViewById(R.id.content_event_list__list);
        eventList.setLayoutManager(new LinearLayoutManager(this));
        eventList.setAdapter(eventListAdapter);

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Create Event", Snackbar.LENGTH_LONG).setAnchorView(R.id.fab).setAction(
                "Action",
                null
            ).show();

            return true;
        });
        binding.fab.setOnClickListener(view -> {
            Utils.hapticTick(view);

            Bundle extra = new Bundle();
            extra.putInt("queue_id", this.queue.getId());

            Intent intent = new Intent(view.getContext(), EventEditActivity.class);
            intent.putExtras(extra);

            view.getContext().startActivity(intent);
        });

        /* Fill lists */
        updateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.event_list_toolbar_menu, menu);

        /* Get some options */
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
        EventOrder.Order order = EventOrder.orderFromString(prefs.getString(PREFS_UI_EVENT_ORDER, PREFS_UI_EVENT_ORDER_DEFAULT));

        // Set item text based on sort mode
        MenuItem item = menu.findItem(R.id.event_list_toolbar_menu_order);
        item.setTitle(Objects.requireNonNull(item.getTitle()).toString() + ": " + getEventOrderDisplayString(order).toString());

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Ekchualle we returned from somewhere */
        updateList();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
