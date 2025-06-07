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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.Executors;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.EventOrder;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.EventListItemBinding;

class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {
    public static final String TAG = EventRecyclerViewAdapter.class.getSimpleName();

    private final Queue queue;
    private Event[] events;

    private EventOrder.Order order;

    public EventRecyclerViewAdapter(Queue queue) {
        super();
        this.queue = queue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = (
            LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_list_item, viewGroup, false)
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.getBinding().eventListItemTags.removeAllViews();
        Tag[] tags = this.events[position].getTags();
        for (Tag tag : tags) {
            TextView tagView = (TextView) (
                LayoutInflater.from(viewHolder.getBinding().getRoot().getContext()).inflate(
                    R.layout.event_list_item_tag,
                    null,
                    false
                )
            );

            tagView.setText(tag.getName());
            viewHolder.getBinding().eventListItemTags.addView(tagView);
        }

        viewHolder.getBinding().eventListItemTimestamp.setText(Utils.formatTs(events[position].getTimestamp()));

        String comment = events[position].getComment();
        if (comment != null && !comment.isEmpty()) {
            viewHolder.getBinding().eventListItemComment.setVisibility(View.VISIBLE);
            viewHolder.getBinding().eventListItemComment.setText(comment);
        } else {
            viewHolder.getBinding().eventListItemComment.setVisibility(View.GONE);
            viewHolder.getBinding().eventListItemComment.setText("");
        }

        viewHolder.getBinding().eventListItemItem.setOnLongClickListener((View view) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), viewHolder.getBinding().eventListItemItem);
            popupMenu.getMenuInflater().inflate(R.menu.event_list_item_action_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener((MenuItem menuItem) -> {
                if (menuItem.getItemId() == R.id.event_list_item_action_menu_delete) {
                    new AlertDialog.Builder(view.getContext()).setTitle("Delete event").setMessage(
                        "Are you sure you want to delete this event?").setPositiveButton(
                        android.R.string.yes,
                        (dialog, which) -> {
                            Wrapper.getEventMaker().delete(
                                events[position]);

                            reloadItems();
                        }
                    ).setNegativeButton(
                        android.R.string.no,
                        null
                    ).setIcon(android.R.drawable.ic_dialog_alert).show();
                }
                return true;
            });

            popupMenu.show();

            return true;
        });
        viewHolder.getBinding().eventListItemItem.setOnClickListener((View view) -> {
            Utils.hapticTick(view);

            Bundle extra = new Bundle();
            extra.putInt("event_id", this.events[position].getId());
            extra.putInt("queue_id", this.queue.getId());

            Intent intent = new Intent(view.getContext(), EventEditActivity.class);
            intent.putExtras(extra);

            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return this.events.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private EventListItemBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = EventListItemBinding.bind(itemView);
        }

        public EventListItemBinding getBinding() {
            return this.binding;
        }
    }

    public void reloadItems() {
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            this.events = this.queue.getEvents(this.order);
            handler.post(() -> {
                this.notifyDataSetChanged();
            });
        });
    }

    public void setOrder(EventOrder.Order order) {
        this.order = order;
    }
}
