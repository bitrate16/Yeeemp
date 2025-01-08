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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.QueueListItemBinding;

class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {
    public static final String TAG = QueueRecyclerViewAdapter.class.getSimpleName();

    private Queue[] queues;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = (
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.queue_list_item, viewGroup, false)
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.getBinding().queueListItemTitle.setText(queues[position].getName());

        viewHolder.getBinding().queueListItemItem.setOnLongClickListener((View view) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), viewHolder.getBinding().queueListItemItem);
            popupMenu.getMenuInflater().inflate(R.menu.queue_list_item_action_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener((MenuItem menuItem) -> {
                if (menuItem.getItemId() == R.id.queue_list_item_action_menu_delete) {
                    new AlertDialog.Builder(view.getContext()).setTitle("Delete queue").setMessage(
                        "Are you sure you want to delete " + queues[position].getName() + "?").setPositiveButton(
                        android.R.string.yes,
                        (dialog, which) -> {
                            Wrapper.getQueueMaker().delete(
                                queues[position]);

                            reloadItems();
                        }
                    ).setNegativeButton(android.R.string.no, null).setIcon(android.R.drawable.ic_dialog_alert).show();
                } else if (menuItem.getItemId() == R.id.queue_list_item_action_menu_rename) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("New name");

                    final EditText input = new EditText(view.getContext());
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    input.setText(queues[position].getName());
                    builder.setView(input);

                    builder.setPositiveButton("OK", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        queues[position].setName(name);

                        reloadItems();
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

                    builder.show();
                }
                return true;
            });

            popupMenu.show();

            return true;
        });
        viewHolder.getBinding().queueListItemItem.setOnClickListener((View view) -> {
            Utils.hapticTick(view);

            Bundle extra = new Bundle();
            extra.putInt("queue_id", queues[position].getId());

            Intent intent = new Intent(view.getContext(), EventListActivity.class);
            intent.putExtras(extra);

            view.getContext().startActivity(intent);
        });

        viewHolder.getBinding().queueListItemStats.setText(Integer.toString(queues[position].getEventCount()));

        viewHolder.getBinding().queueListItemPlus.setOnClickListener((View view) -> {
            Utils.hapticTick(view);

            Bundle extra = new Bundle();
            extra.putInt("queue_id", this.queues[position].getId());

            Intent intent = new Intent(view.getContext(), EventEditActivity.class);
            intent.putExtras(extra);

            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return this.queues.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private QueueListItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = QueueListItemBinding.bind(itemView);
        }

        public QueueListItemBinding getBinding() {
            return this.binding;
        }
    }

    public void reloadItems() {
        this.queues = Wrapper.getQueueMaker().list();
        this.notifyDataSetChanged();
    }
}
