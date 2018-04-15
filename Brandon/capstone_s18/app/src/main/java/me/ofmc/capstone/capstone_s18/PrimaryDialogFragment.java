package me.ofmc.capstone.capstone_s18;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;

/**
 * Created by brandon on 3/17/18.
 */

public class PrimaryDialogFragment  extends DialogFragment {

    private Document doc;
    DocAdapter adapter;


    public static PrimaryDialogFragment newInstance(Document doc, DocAdapter adapter){
        PrimaryDialogFragment fragment = new PrimaryDialogFragment();
        fragment.setDoc(doc, adapter);
        return fragment;

    }

    private void setDoc(Document doc, DocAdapter adapter){
        this.doc = doc;
        this.adapter = adapter;
    }

    private void openFile(String fileName, String type){
        Uri pdfPath = Uri.withAppendedPath(doc.path, fileName);
        Intent target = new Intent(Intent.ACTION_VIEW);
        target.setDataAndType(pdfPath,type);
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No application found to open PDF files", Toast.LENGTH_LONG).show();
        } catch(Exception e){
            Toast.makeText(getContext(), "Error loading pdf", Toast.LENGTH_LONG).show();
        }
    }




    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] mTestArray = getResources().getStringArray(R.array.primary_options_array);
        builder.setTitle(R.string.primary_title)
                .setItems(R.array.primary_options_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        switch (which) {
                            case 0:
                                Toast.makeText(getContext(), mTestArray[which], Toast.LENGTH_LONG).show();
                                openFile("tex.pdf", "application/pdf");
                                break;
                            case 1:
                                openFile("result.tex", "text/plain");
                                Toast.makeText(getContext(), mTestArray[which], Toast.LENGTH_LONG).show();
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
