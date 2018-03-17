package me.ofmc.capstone.capstone_s18;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by brandon on 3/5/18.
 */

public class MainFragment extends Fragment {

    private FloatingActionButton fab, fab2;
    FloatingActionMenu materialDesignFAM;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static int RESULT_LOAD_IMAGE = 2;
    private boolean photo_result = false;
    private File photoFile =null;
    private Uri photoURI = null;
    private ListView mListView;
    private ArrayList<JSONObject> docList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("My Documents");
        View view = inflater.inflate(R.layout.frag_main, container, false);
        fab = view.findViewById(R.id.fab_menu_capture);
        fab2 = view.findViewById(R.id.fab_menu_photo);
        materialDesignFAM = view.findViewById(R.id.material_design_android_floating_action_menu);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDesignFAM.toggle(true);
                dispatchTakePictureIntent();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                materialDesignFAM.toggle(true);
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        mListView = view.findViewById(R.id.doc_list_view);
        docList = new ArrayList<>();
        walk(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS));
        if(docList != null) {
            System.out.println("LEN: " + docList.size());
            String[] listItems = new String[docList.size()];
            ArrayList<Document> arrayOfDocs = new ArrayList<>();
            DocAdapter adapter = new DocAdapter(this.getContext(), arrayOfDocs);
            for(int i = 0; i < docList.size(); i++){
                String title = null;
                String date = null;
                String dir = null;
                try {
                    title = docList.get(i).getString("name");
                    date = docList.get(i).getString("date");
                    dir = docList.get(i).getString("dir");
                    System.out.println("DIR????: " + dir);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(date));
                    SimpleDateFormat format1 = new SimpleDateFormat("MM/dd/yy HH:mm");
                    date = format1.format(cal.getTime());

                } catch(JSONException e){
                    e.printStackTrace();
                    Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }
                Document newDoc = new Document(title, date, dir);
                adapter.add(newDoc);
            }
            mListView.setAdapter(adapter);
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                               int pos, long id) {
                    HoldDialogFragment holdDiag = new HoldDialogFragment();
                    holdDiag.show(getActivity().getFragmentManager(), "hold");
                    return true;
                }
            });
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PrimaryDialogFragment primDialog = new PrimaryDialogFragment();
                    primDialog.show(getActivity().getFragmentManager(), "primary");
                }
            });
        } else{
            System.out.println("DOCLIST IS NULL???");
        }

        // Inflate the layout for this fragment
        return view;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(this.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(this.getContext());
                }
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this.getContext(),
                        "me.ofmc.capstone.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
//            Bitmap imageBitmap =  BitmapFactory.decodeFile(photoFile.getPath());
//            mImageView.setImageBitmap(imageBitmap);
            photo_result = true;
        } else if( requestCode == REQUEST_TAKE_PHOTO && resultCode != RESULT_OK){
            System.out.println("CANCELLED?????");
            photoFile.delete();
        }    else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            photoURI = data.getData();
            photo_result = true;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(photo_result)
        {
            photo_result=false;
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            Bundle args = new Bundle();
            CameraActivity newFragment = CameraActivity.newInstance(photoURI);
            newFragment.setArguments(args);
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);
            // Commit the transaction
            transaction.commit();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public JSONObject getFileContents(final File file) throws IOException {
        final InputStream inputStream = new FileInputStream(file);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        final StringBuilder stringBuilder = new StringBuilder();

        boolean done = false;

        while (!done) {
            final String line = reader.readLine();
            done = (line == null);

            if (line != null) {
                stringBuilder.append(line);
            }
        }

        reader.close();
        inputStream.close();
        JSONObject doc = null;
        try {
            doc = new JSONObject(stringBuilder.toString());
        } catch(JSONException e) {
            e.printStackTrace();
            Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
        return doc;
    }

    public void walk(File root) {

        File[] list = root.listFiles();
        for (File f : list) {
            if (f.isDirectory()) {
                Log.d("", "Dir: " + f.getAbsoluteFile());
                walk(f);
            }
            else {
                String filename = f.getName();
                if(filename.equals("data.json")){
                    try{
                        docList.add(getFileContents(f));
                    } catch(IOException e){
                        e.printStackTrace();
                        Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                    Log.d("", "File: " + f.getAbsoluteFile());
                }
            }
        }
    }
}


