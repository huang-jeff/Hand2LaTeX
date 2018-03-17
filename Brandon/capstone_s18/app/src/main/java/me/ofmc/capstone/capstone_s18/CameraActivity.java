package me.ofmc.capstone.capstone_s18;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by brandon on 3/5/18.
 */

public class CameraActivity extends Fragment {
    private Uri photoFile;
    private View view;
    private Bitmap thumbImage;
    static final int THUMBSIZE = 256;

    public static CameraActivity newInstance(Uri photoFile) {
        CameraActivity fragment = new CameraActivity();
        fragment.setURI(photoFile);
        return fragment;
    }

    private void setURI(Uri photoFile){
        this.photoFile = photoFile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_capture, container, false);
        try {
        ImageView mImageView = view.findViewById(R.id.imgDisplayImage);
        final InputStream imageStream = getActivity().getContentResolver().openInputStream(photoFile);
        Bitmap imageBitmap =  BitmapFactory.decodeStream(imageStream);
        thumbImage = ThumbnailUtils.extractThumbnail(imageBitmap, THUMBSIZE, THUMBSIZE);
        mImageView.setImageBitmap(imageBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
        Button button =  view.findViewById(R.id.processPic);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String docName = ((EditText)view.findViewById(R.id.fileName)).getText().toString();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                Bundle args = new Bundle();
                ProcessFragment newFragment = ProcessFragment.newInstance(photoFile, docName, thumbImage);
                newFragment.setArguments(args);
                transaction.replace(R.id.fragment_container, newFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
            }
        });


        return view;
    }


}