package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import com.pierrejacquier.olim.databinding.FragmentTagsBinding;
import com.pierrejacquier.olim.helpers.ItemClickSupport;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TagsFragment extends Fragment implements View.OnClickListener {

    Olim app;
    OnFragmentInteractionListener Main;
    FragmentTagsBinding binding;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), TagActivity.class);
                i.putExtra("id", -1);
                startActivityForResult(i, 0);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.tagsRecyclerView.setAdapter(new TagsAdapter(app.getCurrentUser().getTags()));
        binding.tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false));
        ItemClickSupport.addTo(binding.tagsRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Intent intent = new Intent(getActivity(), TagActivity.class);
                Bundle b = new Bundle();
                b.putLong("id", app.getCurrentUser().getTags().get(position).getId());
                intent.putExtras(b);
                startActivityForResult(intent, 0);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.tagsRecyclerView.setAdapter(new TagsAdapter(app.getCurrentUser().getTags()));
        binding.tagsRecyclerView.invalidate();
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

}
