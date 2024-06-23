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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import art.pegasko.yeeemp.base.TagStat;

public class EventEditTagsAdapter extends ArrayAdapter<TagStat> {
    public static final String TAG = EventEditTagsAdapter.class.getSimpleName();

    private ArrayList<TagStat> tags;
    private LayoutInflater inflater;
    private final int viewResourceId;
    private final int viewFieldId;

    @SuppressWarnings("unchecked")
    public EventEditTagsAdapter(Context context, int viewResourceId, int viewFieldId, ArrayList<TagStat> tags) {
        super(context, viewResourceId, tags);
        this.tags = tags;
        this.viewResourceId = viewResourceId;
        this.viewFieldId = viewFieldId;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) convertView = inflater.inflate(this.viewResourceId, parent, false);

        TextView text;
        try {
            if (this.viewFieldId == 0) {
                text = (TextView) convertView;
            } else {
                text = convertView.findViewById(this.viewFieldId);
            }

            if (text == null) {
                Log.e(TAG, "Failed attach text to view");
                throw new RuntimeException("Failed attach text to view");
            }
        } catch (ClassCastException e) {
            Log.e(TAG, "You must supply a resource ID for a TextView");
            throw new RuntimeException("You must supply a resource ID for a TextView");
        }

        text.setText(tags.get(position).tag.getName() + " (" + tags.get(position).count + ")");

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    private Filter nameFilter = new Filter() {
        // TODO: Stop using mutable global
        // Reusable filter result
        private ArrayList<TagStat> tagsSuggestions = new ArrayList<TagStat>();

        public String convertResultToString(Object resultValue) {
            String str = ((TagStat) (resultValue)).tag.getName();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            synchronized (tagsSuggestions) {
                if (constraint != null) {
                    tagsSuggestions.clear();
                    for (TagStat tag : tags) {
                        if (tag.tag.getName().contains(constraint.toString().toLowerCase())) {
                            tagsSuggestions.add(tag);
                        }
                    }
                    FilterResults filterResults = new FilterResults();
                    // TODO: Spank me for using global mutable instance for returning immutable result (causes concurrent modification when publishResults())
                    filterResults.values = tagsSuggestions;
                    filterResults.count = tagsSuggestions.size();
                    return filterResults;
                } else {
                    return new FilterResults();
                }
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values == null)
                return;

            synchronized (results.values) {
                ArrayList<TagStat> filteredList = (ArrayList<TagStat>) results.values;
                if (results.count > 0) {
                    clear();
                    for (TagStat c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        }
    };
}