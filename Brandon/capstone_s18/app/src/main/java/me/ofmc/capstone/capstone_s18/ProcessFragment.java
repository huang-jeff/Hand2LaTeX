package me.ofmc.capstone.capstone_s18;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    private static Object monitor = new Object();
    private static boolean fileProcessed = false;
    private static boolean exception = false;
    private Thread thread;
    private Uri photoFile;
    private String docName;
    private Bitmap thumbImage;
    private Timer timer;
    private String folderName;

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

    private void saveFiles(String tex, byte[] pdf){
        File pdfFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "tex.pdf");
        File texFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "result.tex");
        try {
            OutputStream fo1 = new FileOutputStream(pdfFile);
            OutputStream fo2 = new FileOutputStream(texFile);
            fo1.write(pdf);
            fo2.write(tex.getBytes());
        } catch (IOException e) {
            writeConsole(e.toString());
        }
    }

    public void sendPost() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    synchronized(monitor) {
                        try {
                            if(exception){
                                writeConsole("WAITING");
                                monitor.wait();
                                if(exception){
                                    break;
                                }
                            }
                            fileProcessed = false;
                            showRetryButton(false);
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                            String ip = sharedPref.getString("server_preference", null);
                            String port = sharedPref.getString("port_preference", null);
                            Boolean addToGallery = sharedPref.getBoolean("gallery_preference", false);
                            URL url = new URL("http://" + ip + ":" + port + "/upload");
                            progressBar.setProgress(10);
                            setProcStatus(10 + "/" + progressBar.getMax());
                            writeConsole("URL set as: " + url.toString());
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            conn.setInstanceFollowRedirects(false);
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setRequestProperty("charset", "utf-8");
                            // Add your data
                            JSONObject jsonParam = new JSONObject();
                            progressBar.setProgress(20);
                            setProcStatus(20 + "/" + progressBar.getMax());
                            writeConsole("Loading image");
                            final InputStream imageStream = getActivity().getContentResolver().openInputStream(photoFile);
                            byte[] bytes = IOUtils.toByteArray(imageStream);
                            String image64 = Base64.encodeToString(bytes, Base64.DEFAULT);
                            jsonParam.put("photo", image64);
                            progressBar.setProgress(40);
                            setProcStatus(40 + "/" + progressBar.getMax());
                            writeConsole("Done loading image");
                            String jsonString = jsonParam.toString();
                            progressBar.setProgress(50);
                            setProcStatus(50 + "/" + progressBar.getMax());
                            writeConsole("Done creating json");
                            writeConsole("Uploading Data");
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                            os.writeBytes(jsonParam.toString());
                            progressBar.setProgress(70);
                            setProcStatus(70 + "/" + progressBar.getMax());
                            writeConsole("Wrote data stream");
                            os.flush();
                            os.close();

                            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                            String response = conn.getResponseMessage();
                            Log.i("MSG", response);
                            BufferedReader br;
                            writeConsole("HTTP CODE: " + conn.getResponseCode());
                            if (200 <= conn.getResponseCode() && conn.getResponseCode() <= 299) {
                                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            } else {
                                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                            }
                            String message = org.apache.commons.io.IOUtils.toString(br);
                            JSONObject json = new JSONObject(message);
                            String latex = json.getString("latex");
                            byte[] tex_pdf = Base64.decode(json.getString("pdf"), Base64.DEFAULT);
                            progressBar.setProgress(90);
                            setProcStatus(90 + "/" + progressBar.getMax());
                            writeConsole("Saving files");
                            saveFiles(latex, tex_pdf);
                            progressBar.setProgress(100);
                            setProcStatus(100 + "/" + progressBar.getMax());
                            writeConsole("Done saving");
                            if(addToGallery){
                                System.out.println("Adding to gallery");
                                galleryAddPic();
                            }
                            conn.disconnect();
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            Bundle args = new Bundle();
                            MainFragment newFragment = new MainFragment();
                            newFragment.setArguments(args);
                            transaction.replace(R.id.fragment_container, newFragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                            System.out.println("FRAGMENT CHANGED???");
                            fileProcessed = true;
                            break;
                        }  catch (InterruptedException e) {
                            System.out.println("INTERRUPT");
                            exception = false;
                            return;
                        } catch (Exception e) {
                            exception = true;
                            e.printStackTrace();
                            writeConsole(e.toString());
                            showRetryButton(true);
                        }
                    }
                }
            }
        });

        Button retryButton = view.findViewById(R.id.retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeConsole("trying to wake" + exception);
                exception = false;
                synchronized (monitor) {
                    monitor.notifyAll();
                }
            }
        });

        thread.start();
    }

    protected  void showRetryButton(final boolean show){
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Button retryButton = view.findViewById(R.id.retry);
                if(show){
                    retryButton.setVisibility(View.VISIBLE);
                } else{
                    retryButton.setVisibility(View.GONE);
                }
            }
        });
    }

    protected void writeConsole(final String textLine){
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView console =  view.findViewById(R.id.console);
                final ScrollView textContainer=  view.findViewById(R.id.consoleContainer);

                if(currentText == null){
                    currentText = textLine;
                } else{
                    currentText += "\n" + textLine;
                }
                console.setText(currentText);
                textContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    protected void setProcStatus(final String status){
        if(getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final TextView procStatus = view.findViewById(R.id.procStatus);
                procStatus.setText(status);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Processing");
        view = inflater.inflate(R.layout.frag_process, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        folderName = getFileName(photoFile, getActivity()).split(Pattern.quote(new String(".")))[0] + "-" + System.currentTimeMillis();

        File thumbDir = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "thumb.jpg");
        File jsonFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName + File.separator + "data.json");
        writeJson(folderName, jsonFile, thumbDir);
        writeThumb(folderName, thumbDir);
        writeConsole("Initializing...");

        sendPost();
//        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//        Bundle args = new Bundle();
//        MainFragment newFragment = new MainFragment();
//        newFragment.setArguments(args);
//        transaction.replace(R.id.fragment_container, newFragment);
//        transaction.addToBackStack(null);
//        transaction.commit();
//        System.out.println("FRAGMENT CHANGED???");
//        timer = new Timer(false);
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        progressStatus += 10;
//                        if(currentText == null){
//                            currentText = Long.toString( System.currentTimeMillis());
//                        } else{
//                            currentText += "\n" + System.currentTimeMillis();
//                        }
//
//
//                        myTextView.setText(currentText);
//                        progressStatus += 10;
//                        if(progressStatus > 100){
//                            progressStatus = 0;
//                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                            Bundle args = new Bundle();
//                            MainFragment newFragment = new MainFragment();
//                            newFragment.setArguments(args);
//                            transaction.replace(R.id.fragment_container, newFragment);
//                            transaction.addToBackStack(null);
//                            transaction.commit();
//                            System.out.println("FRAGMENT CHANGED???");
//                        }
//                        handler.post(new Runnable() {
//                            public void run() {
//                                progressBar.setProgress(progressStatus);
//                                procStatus.setText(progressStatus+"/"+progressBar.getMax());
//                            }
//                        });
//                        textContainer.fullScroll(View.FOCUS_DOWN);
//                    }
//                });
//            }
//        };
//        timer.scheduleAtFixedRate(timerTask, 1000, 1000); // 1000 = 1 second.


        return view;
    }

    public void onStop() {
        if(!fileProcessed){
            thread.interrupt();
            File folder = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), File.separator + folderName);
            HoldDialogFragment.deleteRecursive(folder);
        }
        super.onStop();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(photoFile);
        getActivity().sendBroadcast(mediaScanIntent);
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
        writer.value(getFileName(photoFile, getActivity()));
        writer.endArray();
        writer.endObject();
        writer.close();
    }


    public static String getFileName(Uri uri, Activity activity) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
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