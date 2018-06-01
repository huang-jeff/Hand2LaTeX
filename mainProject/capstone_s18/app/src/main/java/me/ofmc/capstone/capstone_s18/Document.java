package me.ofmc.capstone.capstone_s18;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.File;

/**
 * Created by brandon on 3/16/18.
 */

public class Document {
    public String name;
    public String dateModified;
    public long dateMills;
    public Uri thumbUrl;
    public Uri path;
    public JSONArray files;

    public Document(String name, String dateModified, String thumbImage, JSONArray files, long dateMills){
        this.name = name;
        this.dateModified = dateModified;
        try {
            Uri tmpURI = Uri.parse(thumbImage);
            this.thumbUrl = Uri.withAppendedPath(tmpURI, "thumb.jpg");
            this.path = Uri.parse(thumbImage);
            this.files = files;
            this.dateMills = dateMills;
        } catch(NullPointerException e){
            e.printStackTrace();
        }
    }
}
