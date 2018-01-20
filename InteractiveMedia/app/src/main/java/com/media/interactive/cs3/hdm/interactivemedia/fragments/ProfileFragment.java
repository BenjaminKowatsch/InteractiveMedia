package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.media.interactive.cs3.hdm.interactivemedia.R;


public class ProfileFragment extends Fragment implements IMyFragment {
    private static final String TAG = ProfileFragment.class.getSimpleName();
    public ProfileFragment() {
        // Required empty public constructor
    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View.OnClickListener getOnFabClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "Hello from " + TAG);
            }
        };
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
