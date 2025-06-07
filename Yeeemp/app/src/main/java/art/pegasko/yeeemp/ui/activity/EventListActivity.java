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
