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

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;

import art.pegasko.yeeemp.base.Tag;
import art.pegasko.yeeemp.base.TagStat;

public class EventEditTagsAdapter extends ArrayAdapter<TagStat> {
    public static final String TAG = EventEditTagsAdapter.class.getSimpleName();

    private ArrayList<TagStat> tags; // used by parent as container for filtered elements, changes as suer types
    private ArrayList<TagStat> tagsAll; // used as container with backup of all unfiltered tags, does not change
    private LayoutInflater inflater;
    private final int viewResourceId;
    private final int viewFieldId;

    public EventEditTagsAdapter(Context context, int viewResourceId, int viewFieldId, ArrayList<TagStat> tags) {
        super(context, viewResourceId, tags);
        this.tags = tags;
        this.tagsAll = (ArrayList<TagStat>) tags.clone();
        this.viewResourceId = viewResourceId;
        this.viewFieldId = viewFieldId;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View tagView, ViewGroup parent) {
        if (tagView == null)
            tagView = inflater.inflate(this.viewResourceId, parent, false);

        TextView text;
        try {
            if (this.viewFieldId == 0) {
                text = (TextView) tagView;
            } else {
                text = tagView.findViewById(this.viewFieldId);
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

        return tagView;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    private Filter nameFilter = new Filter() {
        // TODO: Stop using mutable global
        // Reusable filter result
        private ArrayList<TagStat> tagsSuggestions = new ArrayList<TagStat>();
        private String prevConstraint = null;

        @Override
        public String convertResultToString(Object resultValue) {
            return ((TagStat) (resultValue)).tag.getName();
        }

        /**
         * Check if constraint is reducing previous.
         *
         * Constraint (C) reduces previous (P) if `len(C) >= len(P)` and `C.startsWith(P)`.
         *
         * This is means that user has typed some text to the end of input field and matching items can be filtered
         * from previous filter result.
         * In the opposite case, the set of matching items grows and required full rebuild from scratch.
         */
        private boolean checkIfPossibleReduceToConstraint(String prevConstraint, String constraint) {
            if (prevConstraint == null)
                return false;

            return (constraint.length() >= prevConstraint.length()) && (constraint.startsWith(prevConstraint));
        }

        private String makeConstraintFromUserInput(CharSequence userInput) {
            return userInput.toString().toLowerCase().trim();
        }

        private boolean hasChangedConstraint(String prevConstraint, String newConstraint) {
            if (prevConstraint == null || newConstraint == null)
                return true;

            return !prevConstraint.equals(newConstraint);
        }

        private void reduceTagsSuggestions(TagMatcher matcher) {
            for (int index = 0; index < tagsSuggestions.size(); ) {
                if (!matcher.isMatch(tagsSuggestions.get(index).tag))
                    tagsSuggestions.remove(index);
                else
                    index += 1;
            }
        }

        private void filterTagsSuggestions(TagMatcher matcher) {
            tagsSuggestions.clear();
            for (TagStat tag : tagsAll) {
                if (matcher.isMatch(tag.tag)) {
                    tagsSuggestions.add(tag);
                }
            }
        }

        private void prepareTagsSuggestions(CharSequence constraint) {
            String newConstraint = makeConstraintFromUserInput(constraint);
            TagMatcher matcher = new TagMatcher(newConstraint);

            // TODO: Use better search strategy and optimize search in non-reducing mode
            if (hasChangedConstraint(prevConstraint, newConstraint)) {
                if (checkIfPossibleReduceToConstraint(prevConstraint, newConstraint))
                    reduceTagsSuggestions(matcher);
                else
                    filterTagsSuggestions(matcher);

                prevConstraint = newConstraint;
            }
        }

        private void resetTagsSuggestions() {
            prevConstraint = null;
            tagsSuggestions.clear();
        }

        private FilterResults makeFilterResults() {
            FilterResults filterResults = new FilterResults();
            // TODO: Spank me for using global mutable instance for returning immutable result (causes concurrent modification when publishResults())
            filterResults.values = tagsSuggestions;
            filterResults.count = tagsSuggestions.size();
            return filterResults;
        }

        private FilterResults makeEmptyFilterResults() {
            return new FilterResults();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            synchronized (this) {
                if (constraint != null) {
                    prepareTagsSuggestions(constraint);
                    return makeFilterResults();
                } else {
                    resetTagsSuggestions();
                    return makeEmptyFilterResults();
                }
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            synchronized (this) {
                if (results == null)
                    return;

                if (results.values == null) {
                    clear();
                    notifyDataSetChanged();
                    return;
                }

                // TODO: Do not republish results if no data changed
                ArrayList<TagStat> filteredList = (ArrayList<TagStat>) results.values;
                clear();
                for (TagStat c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }

        /**
         * Matcher to filter tags based on user-input constraint
         */
        class TagMatcher {
            private final String[] constraintParts;

            public TagMatcher(String constraint) {
                this.constraintParts = constraint.split(" +");
            }

            public boolean isMatch(Tag tag) {
                String tagName = tag.getName();
                int lastIndex = 0;
                for (String seq : constraintParts) {
                    if (lastIndex >= tagName.length())
                        return false;

                    int newIndex = tagName.indexOf(seq, lastIndex);
                    if (newIndex == -1)
                        return false;

                    lastIndex = newIndex + seq.length();
                }

                return true;
            }
        }
    };
}