package me.ofmc.capstone.capstone_s18;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.JsonWriter;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by brandon on 3/17/18.
 */

public class HoldDialogFragment extends DialogFragment {

    private Document doc;
    DocAdapter adapter;
    private String m_Text = "";

    public static HoldDialogFragment newInstance(Document doc, DocAdapter adapter){
        HoldDialogFragment fragment = new HoldDialogFragment();
        fragment.setDoc(doc, adapter);
        return fragment;

    }

    private void setDoc(Document doc, DocAdapter adapter){
        this.doc = doc;
        this.adapter = adapter;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    private void writeJson(){
        File jsonFile = new File(doc.path.getPath() + File.separator+ "data.json");
        System.out.println("FILE: " + jsonFile.toString());
        try {
            jsonFile.getParentFile().mkdirs();
            jsonFile.createNewFile();
            OutputStream fo = new FileOutputStream(jsonFile);
            writeJsonStream(fo, doc.path.toString());
            fo.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
    }


    public void writeJsonStream(OutputStream out, String dir) throws IOException, JSONException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("name").value(m_Text);
        writer.name("dir").value(dir);
        writer.name("date").value(doc.dateMills);
        writer.name("files");
        writer.beginArray();
        JSONArray files = doc.files;
        for(int i = 0; i < files.length(); i++){
            writer.value(files.get(i).toString());
        }
        writer.endArray();
        writer.endObject();
        writer.close();
    }

    private void getRename(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Rename Document");

        // Set up the input
        final EditText input = new EditText(this.getContext());
        input.setHint(doc.name);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                writeJson();
                doc.name = m_Text;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] mTestArray = getResources().getStringArray(R.array.hold_options_array);
        builder.setTitle(R.string.hold_title)
                .setItems(R.array.hold_options_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Toast.makeText(getContext(), "Coming soon...", Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                deleteRecursive(new File(doc.path.getPath()));
                                adapter.remove(doc);
                                Toast.makeText(getContext(), "Document Deleted", Toast.LENGTH_LONG).show();
                                break;
                            case 2:
                                Toast.makeText(getContext(), "Coming soon...", Toast.LENGTH_LONG).show();
                                break;
                            case 3:
                                getRename();
                                break;
                            default:
                                Toast.makeText(getContext(), mTestArray[which], Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
        return builder.create();
    }
}
