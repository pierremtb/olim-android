/*
 * Copyright (C) 2015 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pierrejacquier.olim.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.helpers.ItemTouchHelperAdapter;
import com.pierrejacquier.olim.helpers.ItemTouchHelperViewHolder;
import com.pierrejacquier.olim.helpers.OnStartDragListener;

/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private ArrayList<Task> tasks = new ArrayList<>();

    private final OnStartDragListener mDragStartListener;

    public TasksAdapter(OnStartDragListener dragStartListener, ArrayList<Task> tasks) {
        mDragStartListener = dragStartListener;
        this.tasks = tasks;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskPrimaryText.setText(task.getTitle());
        holder.taskSecondaryText.setText(task.getDueDate().toLocaleString());
    }

    @Override
    public void onItemDismiss(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(tasks, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView taskPrimaryText;
        public final TextView taskSecondaryText;
        public final ImageButton taskTag;

        public ItemViewHolder(View itemView) {
            super(itemView);
            taskPrimaryText = (TextView) itemView.findViewById(R.id.taskPrimaryText);
            taskSecondaryText = (TextView) itemView.findViewById(R.id.taskSecondaryText);
            taskTag = (ImageButton) itemView.findViewById(R.id.taskTag);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
