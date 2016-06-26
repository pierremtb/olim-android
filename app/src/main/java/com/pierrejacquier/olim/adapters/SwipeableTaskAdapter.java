package com.pierrejacquier.olim.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.data.Tag;
import com.pierrejacquier.olim.data.Task;
import com.pierrejacquier.olim.databinding.ItemTaskBinding;
import com.pierrejacquier.olim.helpers.Graphics;

import java.util.List;

public class SwipeableTaskAdapter
        extends RecyclerView.Adapter<SwipeableTaskAdapter.TaskViewHolder>
        implements SwipeableItemAdapter<SwipeableTaskAdapter.TaskViewHolder> {
    private static final String TAG = "MySwipeableItemAdapter";

    // TODO: create a latency compensation system (avoid updating the whole RVs)
    private interface Swipeable extends SwipeableItemConstants {}

    private List<Task> tasks;
    private EventListener mEventListener;
    private View.OnClickListener mItemViewOnClickListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;

    public int hintColor;
    public IconicsDrawable tagIcon;

    public interface EventListener {
        void onItemRemoved(int position);

        void onItemPinned(int position);

        void onItemViewClicked(View v, boolean pinned, int position);
    }

    public static class TaskViewHolder extends AbstractSwipeableItemViewHolder {
        private ItemTaskBinding binding;

        public TaskViewHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ItemTaskBinding getBinding() {
            return binding;
        }

        @Override
        public View getSwipeableContainerView() {
            return binding.taskContainer;
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
        setHasStableIds(true);
    }

    private void onItemViewClick(View v) {
        if (mEventListener != null) {
            View itemView = RecyclerViewAdapterUtils.getParentViewHolderItemView(v);
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(itemView);
            int position = recyclerView.getChildAdapterPosition(itemView);
            mEventListener.onItemViewClicked(v, true, position); // true --- pinned
        }
    }

    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            View itemView = RecyclerViewAdapterUtils.getParentViewHolderItemView(v);
            RecyclerView recyclerView = RecyclerViewAdapterUtils.getParentRecyclerView(itemView);
            int position = recyclerView.getChildAdapterPosition(itemView);
            mEventListener.onItemViewClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v), false, position);  // false --- not pinned
        }
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
        // TODO: investigate the icons/colors bug when swiping
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

        holder.getBinding().setTask(task);
        holder.getBinding().executePendingBindings();

        holder.getBinding().taskLayout.setOnClickListener(mItemViewOnClickListener);
        holder.itemView.setOnClickListener(mItemViewOnClickListener);
        holder.getBinding().taskContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);

        if (task.getTag() != null) {
            Tag tag = task.getTag();
            if (!task.isDone()) {
                if (tag.getColor() != null) {
                    holder.getBinding().taskIconButton
                            .setBackgroundDrawable(Graphics.createRoundDrawable(tag.getColor()));
                }
            } else if (tag.getColor() != null) {
                tagIcon.color(Color.parseColor(tag.getColor()));
            } else {
                tagIcon.color(hintColor);
            }
            if (tag.getIcon() != null) {
                try {
                    tagIcon.icon(tag.getIconicsName());
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
                holder.getBinding().taskIconButton.setBackgroundDrawable(
                        Graphics.createRoundDrawable(Graphics.intColorToHex(hintColor))
                );
            }
        }
        if (task.isDone()) {
            holder.getBinding().taskIconButton.setAlpha(Float.valueOf("0.6"));
            holder.getBinding().taskPrimaryText.setTextColor(hintColor);
        }

        holder.getBinding().taskIconButton.setImageDrawable(tagIcon);

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

            holder.getBinding().taskContainer.setBackgroundResource(bgResId);
        }

        holder.setSwipeItemHorizontalSlideAmount(0);
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
        switch (result) {
            case Swipeable.RESULT_SWIPED_RIGHT:
                return new SwipeRightResultAction(this, position);
            case Swipeable.RESULT_SWIPED_LEFT:
                return new SwipeLeftResultAction(this, position);
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

//            mAdapter.notifyItemChanged(mPosition);
//            mSetPinned = true;
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

//            mAdapter.tasks.remove(mPosition);
//            mAdapter.notifyItemRemoved(mPosition);
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

//            Task task = mAdapter.tasks.get(mPosition);
//            task.setPinned(false);
//            mAdapter.notifyItemChanged(mPosition);
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            mAdapter = null;
        }
    }
}
