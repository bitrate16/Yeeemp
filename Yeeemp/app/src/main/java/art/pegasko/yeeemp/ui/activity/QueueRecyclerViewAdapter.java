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
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.QueueOrder;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.QueueListItemBinding;

class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {
    public static final String TAG = QueueRecyclerViewAdapter.class.getSimpleName();

    private Queue[] queues;

    private QueueOrder.Order order;

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
        this.queues = Wrapper.getQueueMaker().list(this.order);
        this.notifyDataSetChanged();
    }

    public void setOrder(QueueOrder.Order order) {
        this.order = order;
    }
}
