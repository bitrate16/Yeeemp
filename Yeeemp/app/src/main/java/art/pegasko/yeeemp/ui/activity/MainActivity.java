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

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.ActivityMainBinding;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.impl.EventImpl;
import art.pegasko.yeeemp.impl.Init;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private RecyclerView queueList;
    private QueueRecyclerViewAdapter queueListAdapter;

    private void updateList() {
        runOnUiThread(() -> {
            queueListAdapter.reloadItems();
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Init.initDB(getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        queueListAdapter = new QueueRecyclerViewAdapter();
        queueList = findViewById(R.id.content_main__queue_list);
        queueList.setLayoutManager(new LinearLayoutManager(this));
        queueList.setAdapter(queueListAdapter);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        /* FAB Listeners */
        binding.fab.setOnLongClickListener((View view) -> {
            Snackbar.make(view, "Create Queue", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show();

            return true;
        });
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Queue q = Wrapper.getQueueMaker().create();
                q.setName("New Queue");
                Log.w(TAG, "Create: " + q.toString());

                updateList();
            }
        });

        /* Fill lists */
        updateList();
    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}