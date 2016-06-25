package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.util.List;

public class TagsFragment extends Fragment implements View.OnClickListener {

    private Olim app;
    private OnFragmentInteractionListener Main;
    private FragmentTagsBinding binding;
    private List<Tag> tags;

    public TagsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Olim) getActivity().getApplicationContext();
        tags = app.getCurrentUser().getTags();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tags, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTagActivity(-1);
            }
        });

        setTags();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tagsRecyclerView.setAdapter(new TagsAdapter(tags));
        binding.tagsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),  LinearLayoutManager.VERTICAL, false));
        ItemClickSupport.addTo(binding.tagsRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                launchTagActivity(app.getCurrentUser().getTags().get(position).getId());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Main.updateUserTags();
        setTags();
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
        List<Tag> updateUserTags();

        List<Tag> getTags();
    }

    /**
     * Navigation
     */

    private void launchTagActivity(long id) {
        Intent intent = new Intent(getActivity(), TagActivity.class);
        Bundle b = new Bundle();
        b.putLong("id", id);
        intent.putExtras(b);
        startActivityForResult(intent, 0);
    }

    /**
     * Layout
     */

    private void setTags() {
        app.getCurrentUser().setTags(Main.getTags());
        tags = app.getCurrentUser().getTags();
        if (tags.size() > 0) {
            binding.tagsCard.setVisibility(View.VISIBLE);
            binding.noTagsLayout.setVisibility(View.GONE);
        } else {
            binding.tagsCard.setVisibility(View.GONE);
            binding.noTagsLayout.setVisibility(View.VISIBLE);
        }
        binding.tagsRecyclerView.setAdapter(new TagsAdapter(tags));
        binding.tagsRecyclerView.invalidate();
    }
}
