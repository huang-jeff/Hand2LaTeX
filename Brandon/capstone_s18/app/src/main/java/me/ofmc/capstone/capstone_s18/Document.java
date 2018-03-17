package me.ofmc.capstone.capstone_s18;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

/**
 * Created by brandon on 3/16/18.
 */

public class Document {
    public String name;
    public String dateModified;
    public Uri thumbUrl;

    public Document(String name, String dateModified, String thumbImage){
        this.name = name;
        this.dateModified = dateModified;
        try {
            Uri tmpURI = Uri.parse(thumbImage);
            this.thumbUrl = Uri.withAppendedPath(tmpURI, "thumb.jpg");
        } catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
