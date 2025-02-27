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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.QueueOrder;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.ActivityQueueListBinding;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.impl.DataUtils;
import art.pegasko.yeeemp.impl.Init;

public class QueueListActivity extends AppCompatActivity {
    public static final String TAG = QueueListActivity.class.getSimpleName();

    private static final int REQUEST_CODE_CREATE_FILE = 37;
    private static final int REQUEST_CODE_OPEN_FILE = 19;

    private static String PREFS_UI_QUEUE_ORDER = "ui-queue-order";
    private static String PREFS_UI_QUEUE_ORDER_DEFAULT = "id";

    private AppBarConfiguration appBarConfiguration;
    private ActivityQueueListBinding binding;
    private RecyclerView queueList;
    private QueueRecyclerViewAdapter queueListAdapter;

    private void updateList() {
        runOnUiThread(() -> {
            queueListAdapter.reloadItems();
        });
    }

    private CharSequence getQueueOrderDisplayString(@Nullable QueueOrder.Order order) {
        if (order == null) {
            throw new RuntimeException("Order is null");
        }

        switch (order) {
            case ID:
                return "created";
            case NAME:
                return "name";
            default:
                throw new RuntimeException("Not implemented for " + order);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init.initDB(getApplicationContext());

        binding = ActivityQueueListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        /* Toolbar menu */
        binding.toolbar.inflateMenu(R.menu.queue_list_toolbar_menu);

        // TODO: Better import / export / delete logic
        binding.toolbar.setOnMenuItemClickListener((MenuItem item) -> {
            if (item.getItemId() == R.id.queue_list_toolbar_menu_order) {
                /* Get some options */
                SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
                QueueOrder.Order order = QueueOrder.orderFromString(prefs.getString(PREFS_UI_QUEUE_ORDER, PREFS_UI_QUEUE_ORDER_DEFAULT));

                /* Construct options */
                QueueOrder.Order[] values = QueueOrder.Order.class.getEnumConstants();
                if (values == null) {
                    // How did we get here?
                    throw new RuntimeException("QueueOrder.Order.class.getEnumConstants() is Empty");
                }
                CharSequence[] choices = new CharSequence[values.length];
                int currentSelection = -1;

                for (int i = 0; i < values.length; ++i) {
                    choices[i] = this.getQueueOrderDisplayString(values[i]);
                    if (values[i] == order) {
                        currentSelection = i;
                    }
                }

                /* Build dialog */
                AlertDialog.Builder builder = new AlertDialog.Builder(QueueListActivity.this);
                builder.setTitle("Order by ..");

                builder.setSingleChoiceItems(
                    choices,
                    currentSelection,
                    (dialog, which) -> {
                        QueueOrder.Order newOrder = values[which];

                        // Save option
                        prefs.edit().putString(PREFS_UI_QUEUE_ORDER, QueueOrder.orderToString(newOrder)).apply();

                        // Apply
                        QueueListActivity.this.invalidateOptionsMenu();
                        queueListAdapter.setOrder(newOrder);
                        runOnUiThread(() -> {
                            queueListAdapter.reloadItems();
                        });

                        // Close
                        dialog.dismiss();
                    }
                );

                builder.show();

                return true;
            } if (item.getItemId() == R.id.queue_list_toolbar_menu_export) {
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_TITLE, "export_" + DataUtils.formatTs(System.currentTimeMillis()) + ".db");
                startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);

                return true;
            } else if (item.getItemId() == R.id.queue_list_toolbar_menu_import) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);

                return true;
            } else if (item.getItemId() == R.id.queue_list_toolbar_menu_delete) {

                new AlertDialog
                    .Builder(QueueListActivity.this)
                    .setTitle("Confirm action")
                    .setMessage("Delete everything?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (DialogInterface dialog, int id) -> {
                        try {
                            Log.i(TAG, "Close before delete");
                            DataUtils.closeDatabase();

                            Log.i(TAG, "Deleting database");
                            DataUtils.deleteDatabase(getApplicationContext());

                            Log.i(TAG, "Reloading database");
                            Init.reinitDB(getApplicationContext());
                            updateList();
                        } catch (Exception e) {
                            Log.e(TAG, "Delete failed");
                            Log.wtf(TAG, e);

                            new AlertDialog
                                .Builder(QueueListActivity.this)
                                .setTitle("Delete failed")
                                .setMessage(e.getMessage())
                                .setCancelable(true)
                                .setNegativeButton("OK", (DialogInterface dialog2, int id2) -> { dialog2.cancel(); })
                                .show();
                        }
                    })
                    .setNegativeButton("Cancel", (DialogInterface dialog, int id) -> { dialog.cancel(); })
                    .show();

                return true;
            }
            return false;
        });

        /* Get some options */
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
        QueueOrder.Order order = QueueOrder.orderFromString(prefs.getString(PREFS_UI_QUEUE_ORDER, PREFS_UI_QUEUE_ORDER_DEFAULT));

        /* Queue list */
        queueListAdapter = new QueueRecyclerViewAdapter();
        queueListAdapter.setOrder(order);
        queueList = findViewById(R.id.content_queue_list__list);
        queueList.setLayoutManager(new LinearLayoutManager(this));
        queueList.setAdapter(queueListAdapter);

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Create Queue", Snackbar.LENGTH_LONG).setAnchorView(R.id.fab).setAction(
                "Action",
                null
            ).show();

            return true;
        });
        binding.fab.setOnClickListener(view -> {
            Utils.hapticTick(view);

            Queue q = Wrapper.getQueueMaker().create();
            q.setName("New Queue");

            updateList();
        });

        /* Fill lists */
        updateList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == REQUEST_CODE_CREATE_FILE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.i(TAG, "Exporting file to " + uri.toString());

                try {
                    DataUtils.exportDatabase(getApplicationContext(), uri);
                    Toast.makeText(QueueListActivity.this, "Exported database", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.wtf(TAG, e);

                    new AlertDialog
                        .Builder(QueueListActivity.this)
                        .setTitle("Export failed")
                        .setMessage(e.getMessage())
                        .setCancelable(true)
                        .setNegativeButton("OK", (DialogInterface dialog, int id) -> { dialog.cancel(); })
                        .show();
                }
            }
        } else if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                final Uri uri = resultData.getData();

                new AlertDialog
                    .Builder(QueueListActivity.this)
                    .setTitle("Confirm action")
                    .setMessage("Import file as database? This will overwrite local database with external file without checking")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (DialogInterface dialog, int id) -> {
                        Log.i(TAG, "Importing file from " + uri.toString());

                        try {
                            Log.i(TAG, "Close before restore");
                            DataUtils.closeDatabase();

                            Log.i(TAG, "Backup before restore");
                            DataUtils.backupDatabase(getApplicationContext());

                            Log.i(TAG, "Importing database");
                            DataUtils.importDatabase(getApplicationContext(), uri);
                            Toast.makeText(QueueListActivity.this, "Imported database", Toast.LENGTH_SHORT).show();

                            Log.i(TAG, "Reloading database");
                            Init.reinitDB(getApplicationContext());
                            updateList();

                            Log.i(TAG, "Delete backup");
                            DataUtils.deleteBackupDatabase(getApplicationContext());
                        } catch (Exception e) {
                            Log.e(TAG, "Import failed");
                            Log.wtf(TAG, e);

                            new AlertDialog
                                .Builder(QueueListActivity.this)
                                .setTitle("Import failed")
                                .setMessage(e.getMessage())
                                .setCancelable(true)
                                .setNegativeButton("OK", (DialogInterface dialog2, int id2) -> { dialog2.cancel(); })
                                .show();

                            try {
                                Log.i(TAG, "Trying to restore backup");
                                DataUtils.restoreDatabase(getApplicationContext());

                                Log.i(TAG, "Reloading database");
                                Init.reinitDB(getApplicationContext());
                                updateList();

                                Log.i(TAG, "Delete backup");
                                DataUtils.deleteBackupDatabase(getApplicationContext());

                                new AlertDialog
                                    .Builder(QueueListActivity.this)
                                    .setTitle("Info")
                                    .setMessage("Restored backup")
                                    .setCancelable(true)
                                    .setNegativeButton("OK", (DialogInterface dialog2, int id2) -> { dialog2.cancel(); })
                                    .show();

                            } catch (Exception e2) {
                                Log.e(TAG, "Restore backup failed");
                                Log.wtf(TAG, e2);

                                new AlertDialog
                                    .Builder(QueueListActivity.this)
                                    .setTitle("Restore backup failed")
                                    .setMessage(e2.getMessage())
                                    .setCancelable(true)
                                    .setNegativeButton("OK", (DialogInterface dialog2, int id2) -> { dialog2.cancel(); })
                                    .show();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", (DialogInterface dialog, int id) -> { dialog.cancel(); })
                    .show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.queue_list_toolbar_menu, menu);

        /* Get some options */
        SharedPreferences prefs = this.getApplicationContext().getSharedPreferences(Common.PREFS_NAME, MODE_PRIVATE);
        QueueOrder.Order order = QueueOrder.orderFromString(prefs.getString(PREFS_UI_QUEUE_ORDER, PREFS_UI_QUEUE_ORDER_DEFAULT));

        // Set item text based on sort mode
        MenuItem item = menu.findItem(R.id.queue_list_toolbar_menu_order);
        item.setTitle(Objects.requireNonNull(item.getTitle()).toString() + ": " + getQueueOrderDisplayString(order).toString());


        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* Ekchualle we returned from somewhere */
        updateList();
    }
}