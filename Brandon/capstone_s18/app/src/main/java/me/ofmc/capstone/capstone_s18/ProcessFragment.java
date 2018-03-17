package me.ofmc.capstone.capstone_s18;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.JsonWriter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 * Created by brandon on 3/16/18.
 */

public class ProcessFragment extends Fragment {

    private String currentText;
    private View view;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Uri photoFile;
    private String docName;
    private Bitmap thumbImage;
    private Timer timer;

    public static ProcessFragment newInstance(Uri photoFile, String docName, Bitmap thumbImage ) {
        ProcessFragment fragment = new ProcessFragment();
        fragment.setPhotoURI(photoFile, docName,thumbImage);
        return fragment;
    }

    private void setPhotoURI(Uri photoFile, String docName, Bitmap thumbImage ){
        this.docName = docName;
        this.photoFile = photoFile;
        this.thumbImage = thumbImage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.frag_process, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        String folderName = getFileName(photoFile).split(Pattern.quote(new String(".")))[0] + "-" + System.currentTimeMillis();
        File thumbDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "thumb.jpg");
        File jsonFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "data.json");
        writeJson(folderName, jsonFile, thumbDir);
        writeThumb(folderName, thumbDir);
        final Handler handler = new Handler();
        timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressStatus += 10;
                        if(currentText == null){
                            currentText = Long.toString( System.currentTimeMillis());
                        } else{
                            currentText += "\n" + System.currentTimeMillis();
                        }

                        final TextView myTextView =  view.findViewById(R.id.console);
                        final ScrollView textContainer=  view.findViewById(R.id.consoleContainer);
                        final TextView procStatus = view.findViewById(R.id.procStatus);
                        myTextView.setText(currentText);
                        progressStatus += 10;
                        if(progressStatus > 100){
                            progressStatus = 0;
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            Bundle args = new Bundle();
                            MainFragment newFragment = new MainFragment();
                            newFragment.setArguments(args);
                            transaction.replace(R.id.fragment_container, newFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            System.out.println("FRAGMENT CHANGED???");
                        }
                        handler.post(new Runnable() {
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                procStatus.setText(progressStatus+"/"+progressBar.getMax());
                            }
                        });
                        textContainer.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 1000, 1000); // 1000 = 1 second.


        return view;
    }

    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    private void writeThumb(String folderName, File thumbDir){

        try {
            thumbDir.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(thumbDir);
            thumbImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
        System.out.println("FOLDER NAME: " + folderName);
    }

    private void writeJson(String folderName, File jsonFile, File thumbDir){

        System.out.println("FILE: " + jsonFile.toString());
        try {
            jsonFile.getParentFile().mkdirs();
            jsonFile.createNewFile();
            OutputStream fo = new FileOutputStream(jsonFile);
            writeJsonStream(fo, thumbDir.getParentFile().toURI().toString());
            fo.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
        System.out.println("FOLDER NAME: " + folderName);
    }

    public void writeJsonStream(OutputStream out, String dir) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("name").value(docName);
        writer.name("dir").value(dir);
        writer.name("date").value(System.currentTimeMillis());
        writer.name("files");
        writer.beginArray();
        writer.value(getFileName(photoFile));
        writer.endArray();
        writer.endObject();
        writer.close();
    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
