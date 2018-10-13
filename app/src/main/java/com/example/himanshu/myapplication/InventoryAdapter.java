package com.example.himanshu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends ArrayAdapter<Inventory> {

    public InventoryAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    void setData(ArrayList<Inventory> inventories){
        addAll(inventories);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Inventory inventory = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_cell, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.imageView);
            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.textView1);
            viewHolder.catTextview = (TextView) convertView.findViewById(R.id.textView2);
            viewHolder.amount = (TextView) convertView.findViewById(R.id.textView3);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.nameTextView.setText(inventory.getItem_name());
        viewHolder.catTextview.setText(inventory.getItem_cat());
        viewHolder.amount.setText(inventory.getAmount());
        viewHolder.setImageWithUrl(inventory.getImage_url());

        return convertView;
    }


    private static class ViewHolder implements DownloadFileTask.ImageDownloadCallback{
        ImageView imageView;
        TextView nameTextView;
        TextView catTextview;
        TextView amount;

        public void setImageWithUrl(String url) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("himanshu/inventory-app").build();
            DbxClientV2 client = new DbxClientV2(config, MainActivity.ACCESS_TOKEN);

            DownloadFileTask downloadFileTask = new DownloadFileTask(imageView.getContext(), client, this);
            downloadFileTask.execute(url);
        }

        @Override
        public void onDownloadComplete(File result) {
            if (imageView!=null)
                if(result.exists())
                {
                    Bitmap myBitmap = BitmapFactory.decodeFile(result.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }
        }

        @Override
        public void onError(Exception e) {

        }
    }
}
