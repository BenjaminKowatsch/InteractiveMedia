package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;


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
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);
        final ImageView profileImageView = (ImageView) view.findViewById(R.id.profile_image_view);
        final TextView profileUsername = (TextView) view.findViewById(R.id.profile_username);
        final TextView profileEmail = (TextView) view.findViewById(R.id.profile_email);
        final User user = Login.getInstance().getUser();
        final String imageUrl = user.getImageUrl();

        if (imageUrl != null) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();
            GlideUrl glideUrl = null;
            if (imageUrl != null) {
                if (imageUrl.startsWith(getResources().getString(R.string.web_service_url))) {
                    builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
                }
                glideUrl = new GlideUrl(imageUrl, builder.build());
                Glide.with(this).load(glideUrl)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .fallback(R.drawable.anonymoususer)
                    .placeholder(R.drawable.anonymoususer)
                    .into(profileImageView);
            }
        } else {
            Glide.with(this)
                .load(R.drawable.anonymoususer)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(profileImageView);
        }
        profileUsername.setText(user.getUsername());
        profileEmail.setText(user.getEmail());

        // Inflate the layout for this fragment
        return view;
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

}
