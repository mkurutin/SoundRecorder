package com.danielkim.soundrecorder.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.danielkim.soundrecorder.R;
import com.danielkim.soundrecorder.adapters.AudiobookPickerAdapter;

public class AudiobookPickerFragment extends Fragment{
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "AudiobookPickerFragment";

    private int position;
    private AudiobookPickerAdapter audiobookPickerAdapter;

    public static AudiobookPickerFragment newInstance(int position) {
        AudiobookPickerFragment f = new AudiobookPickerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audiobook_viewer, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        audiobookPickerAdapter = new AudiobookPickerAdapter(getActivity(), linearLayoutManager);
        recyclerView.setAdapter(audiobookPickerAdapter);

        return view;
    }
}




