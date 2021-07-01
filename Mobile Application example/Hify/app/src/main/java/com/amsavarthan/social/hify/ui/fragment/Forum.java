package com.amsavarthan.social.hify.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.amsavarthan.social.hify.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class Forum extends Fragment implements BottomNavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemReselectedListener {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_forum, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BottomNavigationView bottomNavigationView=view.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnNavigationItemReselectedListener(this);

        loadfragment(new AllQuestions());

    }

    public void loadfragment(androidx.fragment.app.Fragment fragment) {
        ((AppCompatActivity)getActivity()).getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_global:
                loadfragment(new AllQuestions());
                break;

            case R.id.action_my:
                loadfragment(new MyQuestions());
                break;

        }
        return true;
    }

    @Override
    public void onNavigationItemReselected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.action_global:
                break;

            case R.id.action_my:
                break;

        }
    }
}
