package com.montefiore.gaulthiergain.distributedcache;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.montefiore.gaulthiergain.adhoclibrary.appframework.ListenerApp;

import java.util.List;

class MyListAdaper extends ArrayAdapter<String> {
    private int layout;
    private List<String> mObjects;
    private List<String> devices;

    public MyListAdaper(Context context, int resource, List<String> objects, List<String> devices) {
        super(context, resource, objects);
        this.mObjects = objects;
        this.layout = resource;
        this.devices = devices;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder mainViewholder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(layout, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.thumbnail = (ImageView) convertView.findViewById(R.id.list_item_thumbnail);
            viewHolder.title = (TextView) convertView.findViewById(R.id.list_item_text);
            viewHolder.device = convertView.findViewById(R.id.list_item_device);
            convertView.setTag(viewHolder);
        }
        mainViewholder = (ViewHolder) convertView.getTag();
        try {
            mainViewholder.title.setText(getItem(position));
            mainViewholder.device.setText(devices.get(position));
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return convertView;
    }


    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView device;
    }

}
