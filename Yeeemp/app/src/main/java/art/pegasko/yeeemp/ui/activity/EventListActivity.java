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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.ActivityEventListBinding;
import art.pegasko.yeeemp.impl.Init;

public class EventListActivity extends AppCompatActivity {
    public static final String TAG = EventListActivity.class.getSimpleName();

    private ActivityEventListBinding binding;
    private RecyclerView eventList;
    private EventRecyclerViewAdapter eventListAdapter;
    private Queue queue;

    private void updateList() {
        runOnUiThread(() -> {
            eventListAdapter.reloadItems();
        });
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

        binding = ActivityEventListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + " / " + queue.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        /* Queue list */
        eventListAdapter = new EventRecyclerViewAdapter(queue);
        eventList = findViewById(R.id.content_event_list__list);
        eventList.setLayoutManager(new LinearLayoutManager(this));
        eventList.setAdapter(eventListAdapter);

//        /* Swipe delete */
//        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                Snackbar.make(viewHolder.itemView, "undo delete event", 5000)
//                    .setAnchorView(R.id.fab)
//                    .setAction("Action", (View view) -> {
//
//                    }).show();
//
//                ScheduledFuture<?> future = eventListAdapter.scheduleDeleteAt(viewHolder.getAdapterPosition());
//            }
//        };

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Create Event", Snackbar.LENGTH_LONG).setAnchorView(R.id.fab).setAction("Action",
                                                                                                        null
            ).show();

            return true;
        });
        binding.fab.setOnClickListener(view -> {
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
