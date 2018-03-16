package me.ofmc.capstone.capstone_s18;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by brandon on 3/5/18.
 */

public class CameraActivity extends Fragment {
    private File photoFile;

    public static CameraActivity newInstance(File photoFile) {
        CameraActivity fragment = new CameraActivity();
        fragment.setURI(photoFile);
        return fragment;
    }

    private void setURI(File photoFile){
        this.photoFile = photoFile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_capture, container, false);
        ImageView mImageView = view.findViewById(R.id.imgDisplayImage);
        Bitmap imageBitmap =  BitmapFactory.decodeFile(photoFile.getPath());
        mImageView.setImageBitmap(imageBitmap);
        return view;
    }

}