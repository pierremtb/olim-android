/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.pierrejacquier.olim.adapters;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.helpers.Graphics;

import java.util.List;

import im.delight.android.ddp.MeteorSingleton;

public class SwipeableTaskAdapter
        extends RecyclerView.Adapter<SwipeableTaskAdapter.TaskViewHolder>
        implements SwipeableItemAdapter<SwipeableTaskAdapter.TaskViewHolder> {
    private static final String TAG = "MySwipeableItemAdapter";

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    private List<Task> tasks;
    private EventListener mEventListener;
    private View.OnClickListener mItemViewOnClickListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;

    public int hintColor;
    public IconicsDrawable tagIcon;

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemPinned(int position);

        void onItemViewClicked(View v, boolean pinned);
    }

    public static class TaskViewHolder extends AbstractSwipeableItemViewHolder {
        public FrameLayout taskContainer;
        public TextView taskPrimaryText;
        public TextView taskSecondaryText;
        public ImageButton taskTag;

        public TaskViewHolder(View v) {
            super(v);
            taskContainer = (FrameLayout) v.findViewById(R.id.taskContainer);
            taskPrimaryText = (TextView) v.findViewById(R.id.taskPrimaryText);
            taskSecondaryText = (TextView) v.findViewById(R.id.taskSecondaryText);
            taskTag = (ImageButton) v.findViewById(R.id.taskTag);
        }

        @Override
        public View getSwipeableContainerView() {
            return taskContainer;
        }
    }

    public SwipeableTaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
        mItemViewOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemViewClick(v);
            }
        };
        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeableViewContainerClick(v);
            }
        };

        // SwipeableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(v, true); // true --- pinned
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false);  // false --- not pinned
        }
    }

    @Override
    public long getItemId(int position) {
        //return tasks.get(position).getId();
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        //return tasks.get(position).getViewType();
        return 0;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        hintColor = parent.getContext().getResources().getColor(R.color.colorHintText);
        tagIcon = new IconicsDrawable(parent.getContext()).sizeDp(20).color(Color.WHITE);
        final View v = inflater.inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final Task task = tasks.get(position);

        // set listeners
        // (if the item is *pinned*, click event comes to the itemView)
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        // (if the item is *not pinned*, click event comes to the taskContainer)
        holder.taskContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);

        // set text
        holder.taskPrimaryText.setText(task.getTitle());
        holder.taskSecondaryText.setText(task.getDueDate().toLocaleString());
        if (task.getTag() != null) {
            Tag tag = new Tag(MeteorSingleton.getInstance()
                                .getDatabase()
                                .getCollection("Tags")
                                .getDocument(task.getTag()));
            if (!task.isDone()) {
                if (tag.getColor() != null) {
                    holder.taskTag.setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
                }
            } else if (tag.getColor() != null) {
                tagIcon.color(Color.parseColor(tag.getColor()));
            } else {
                tagIcon.color(hintColor);
            }
            if (tag.getIcon() != null) {
                String iconName;
                try {
                    tagIcon.icon(GoogleMaterial.Icon.valueOf("gmd_" + tag.getIcon()));
                } catch (Exception e ) {
                    tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
                }
            } else {
                tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            }
        } else {
            tagIcon.icon(GoogleMaterial.Icon.gmd_label_outline);
            if (task.isDone()) {
                tagIcon.color(hintColor);
            } else {
                holder.taskTag.setBackgroundDrawable(
                        Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
                );
            }
        }
        if (task.isDone()) {
            holder.taskTag.setAlpha(Float.valueOf("0.6"));
            holder.taskPrimaryText.setTextColor(hintColor);
        }

        holder.taskTag.setImageDrawable(tagIcon);

        // set background resource (target view ID: container)
        final int swipeState = holder.getSwipeStateFlags();

        if ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0) {
            int bgResId;

            if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.taskContainer.setBackgroundResource(bgResId);
        }

        // set swiping properties
        holder.setSwipeItemHorizontalSlideAmount(
                /*task.isPinned() ? Swipeable.OUTSIDE_OF_THE_WINDOW_LEFT :*/ 0);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public int onGetSwipeReactionType(TaskViewHolder holder, int position, int x, int y) {
        return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
    }

    @Override
    public void onSetSwipeBackground(TaskViewHolder holder, int position, int type) {
        int bgRes = 0;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            case Swipeable.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_left;
                break;
            case Swipeable.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_right;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }

    @Override
    public SwipeResultAction onSwipeItem(TaskViewHolder holder, final int position, int result) {
        Log.d(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");

        switch (result) {
            // swipe right
            case Swipeable.RESULT_SWIPED_RIGHT:
                if (/*tasks.get(position).isPinned()*/false) {
                    // pinned --- back to default position
                    return new UnpinResultAction(this, position);
                } else {
                    // not pinned --- remove
                    return new SwipeRightResultAction(this, position);
                }
                // swipe left -- pin
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position);
            // other --- do nothing
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            Task task = mAdapter.tasks.get(mPosition);

            if (/*!task.isPinned()*/ true) {
                //task.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
                mSetPinned = true;
            }
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mSetPinned && mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemPinned(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the referencess
        }
    }

    private static class SwipeRightResultAction extends SwipeResultActionRemoveItem {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;

        SwipeRightResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            //mAdapter.tasks.remove(mPosition);
            mAdapter.notifyItemRemoved(mPosition);
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemRemoved(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        private SwipeableTaskAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(SwipeableTaskAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            Task task = mAdapter.tasks.get(mPosition);
            if (/*task.isPinned()*/ true) {
                //task.setPinned(false);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }
}
