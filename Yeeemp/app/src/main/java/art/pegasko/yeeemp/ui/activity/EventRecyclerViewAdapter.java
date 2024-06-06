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
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Event;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.databinding.EventListItemBinding;
import art.pegasko.yeeemp.databinding.QueueListItemBinding;

class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder> {
    public static final String TAG = EventRecyclerViewAdapter.class.getSimpleName();

    private final Queue queue;
    private Event[] events;

    public EventRecyclerViewAdapter(Queue queue) {
        super();
        this.queue = queue;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = (
            LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.event_list_item, viewGroup, false)
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.getBinding().eventListItemTimestamp.setText(Utils.formatTs(events[position].getTimestamp()));

        viewHolder.getBinding().eventListItemItem.setOnLongClickListener((View view) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), viewHolder.getBinding().eventListItemItem);
            popupMenu.getMenuInflater().inflate(R.menu.event_list_item_action_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener((MenuItem menuItem) -> {
                if (menuItem.getItemId() == R.id.event_list_item_action_menu_delete) {
                    new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete event")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            Wrapper.getEventMaker().delete(events[position]);

                            reloadItems();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                }
                return true;
            });

            popupMenu.show();

            return true;
        });

//        viewHolder.queueStats.setText(Integer.toString(events[position].getEventCount()));
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
        this.events = this.queue.getEvents();
        this.notifyDataSetChanged();
    }
}
