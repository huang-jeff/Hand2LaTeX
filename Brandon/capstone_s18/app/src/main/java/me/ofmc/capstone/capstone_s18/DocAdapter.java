package me.ofmc.capstone.capstone_s18;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by brandon on 3/16/18.
 */

public class DocAdapter extends ArrayAdapter<Document> {
    public DocAdapter(Context context, ArrayList<Document> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Document doc = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.doc_item, parent, false);
        }
        // Lookup view for data population
        TextView docName = convertView.findViewById(R.id.doc_name);
        TextView docDate = convertView.findViewById(R.id.doc_date);
        ImageView thumbPrev = convertView.findViewById(R.id.doc_list_thumbnail);
        // Populate the data into the template view using the data object
        docName.setText(doc.name);
        docDate.setText(doc.dateModified);
        try {
            System.out.println("THUMB URL: " + doc.thumbUrl);
            InputStream imageStream = this.getContext().getContentResolver().openInputStream(doc.thumbUrl);
            Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
            thumbPrev.setImageBitmap(imageBitmap);
        } catch(FileNotFoundException | NullPointerException e){
            e.printStackTrace();
            Toast.makeText(this.getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
        }
        // Return the completed view to render on screen
        return convertView;
    }
}
