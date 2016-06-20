package com.pierrejacquier.olim.helpers;

import android.util.Log;
import android.view.View;

import com.pierrejacquier.olim.adapters.SwipeableTaskAdapter;
import com.pierrejacquier.olim.data.Task;
import java.util.List;

public class TaskEventsListener implements SwipeableTaskAdapter.EventListener {

    private List<Task> tasks;

    public TaskEventsListener(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void onItemRemoved(int position) {
        Task task = tasks.get(position);
        //task.toggleDoneServer();
    }

    @Override
    public void onItemPinned(int position) {
        Task task = tasks.get(position);
        //task.postponeToNextDayServer();
    }

    @Override
    public void onItemViewClicked(View v, boolean pinned) {
        Log.d("Clicekd", v.toString());
    }
}