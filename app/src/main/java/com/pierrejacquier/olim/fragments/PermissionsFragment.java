package com.pierrejacquier.olim.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pierrejacquier.olim.R;
import com.pierrejacquier.olim.databinding.FragmentPermissionsBinding;

public class PermissionsFragment extends Fragment {

    private FragmentPermissionsBinding binding;

    public PermissionsFragment() {}

    public static PermissionsFragment newInstance() {
        return new PermissionsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_permissions, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
