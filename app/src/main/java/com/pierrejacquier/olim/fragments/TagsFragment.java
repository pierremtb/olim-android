package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierrejacquier.olim.Olim;
import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.activities.TagActivity;
import com.pierrejacquier.olim.adapters.TagsAdapter;
import com.pierrejacquier.olim.data.Tag;

public class TagsFragment extends Fragment implements View.OnClickListener {

    Olim app;
    OnFragmentInteractionListener Main;

    public TagsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
        app.getCurrentUser().getTags();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myView = inflater.inflate(R.layout.fragment_tags, container, false);
        FloatingActionButton fab = (FloatingActionButton) myView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TagActivity.class);
                i.putExtra("id", -1);
                startActivity(i);
            }
        });
        return myView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView tagsRecyclerView = (RecyclerView) view.findViewById(R.id.tagsRecyclerView);
        tagsRecyclerView.setAdapter(new TagsAdapter(app.getCurrentUser().getTags()));
        tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            Main = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
    }

    public interface OnFragmentInteractionListener {
        void toast(String str);
    }

    // Affichage
}
