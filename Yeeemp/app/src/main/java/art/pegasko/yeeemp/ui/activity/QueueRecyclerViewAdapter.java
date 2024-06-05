package art.pegasko.yeeemp.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import art.pegasko.yeeemp.R;
import art.pegasko.yeeemp.base.Queue;
import art.pegasko.yeeemp.base.QueueMaker;
import art.pegasko.yeeemp.base.Wrapper;
import art.pegasko.yeeemp.impl.EventImpl;

public class QueueRecyclerViewAdapter extends RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {
    public static final String TAG = QueueRecyclerViewAdapter.class.getSimpleName();

    private Queue[] queues;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = (
            LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.queue_list_item, viewGroup, false)
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.queueName.setText(queues[position].getName());
        viewHolder.queueItem.setOnLongClickListener((View view) -> {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), viewHolder.queueItem);
            popupMenu.getMenuInflater().inflate(R.menu.queue_list_item_action_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener((MenuItem menuItem) -> {
                if (menuItem.getItemId() == R.id.queue_list_item_action_menu_delete) {
                    new AlertDialog.Builder(view.getContext())
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this queue?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
//                                Wrapper.getQueueMaker().delete
                                Log.e(TAG, "Not implemented action");
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                } else if (menuItem.getItemId() == R.id.queue_list_item_action_menu_rename) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Title");

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
    }

    @Override
    public int getItemCount() {
        return this.queues.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView queueName;
        private View queueItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            queueItem = itemView.findViewById(R.id.queue_list_item__queue_item);
            queueName = (TextView) itemView.findViewById(R.id.queue_list_item__queue_title);
        }

        public TextView getQueueName() {
            return queueName;
        }
    }

    public void reloadItems() {
        this.queues = Wrapper.getQueueMaker().list();
        this.notifyDataSetChanged();
    }
}
