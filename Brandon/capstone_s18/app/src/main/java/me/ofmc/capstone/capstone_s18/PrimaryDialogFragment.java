package me.ofmc.capstone.capstone_s18;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by brandon on 3/17/18.
 */

public class PrimaryDialogFragment  extends DialogFragment {
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
                                break;
                            case 1:
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
