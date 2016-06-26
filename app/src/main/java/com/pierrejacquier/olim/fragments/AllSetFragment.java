package com.pierrejacquier.olim.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.databinding.FragmentAllSetBinding;

public class AllSetFragment extends Fragment {

    private FragmentAllSetBinding binding;

    public AllSetFragment() {
    }

    public static AllSetFragment newInstance() {
       return new AllSetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_set, container, false);
        return binding.getRoot();
    }
}
