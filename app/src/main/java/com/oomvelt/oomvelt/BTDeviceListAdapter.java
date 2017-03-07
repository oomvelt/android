package com.oomvelt.oomvelt;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BTDeviceListAdapter extends RecyclerView.Adapter<BTDeviceListAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> devices;
    private ItemCallback itemCallback;

    public BTDeviceListAdapter(ArrayList<BluetoothDevice> devices) {
        this.devices = devices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);

        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        holder.name.setText(deviceName != null ? deviceName : deviceAddress);
        holder.address.setText(deviceName == null ? "" : deviceAddress);

        holder.itemView.setTag(device);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    void setCallback(ItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

    interface ItemCallback {
        void onItemClicked(BluetoothDevice device);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView address;
        BTDeviceListAdapter adapter;

        public ViewHolder(View itemView, BTDeviceListAdapter adapter) {
            super(itemView);

            this.adapter = adapter;

            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(android.R.id.text1);
            address = (TextView) itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(View v) {
            Log.v("CLICK", "Click");
            BluetoothDevice device = devices.get(getAdapterPosition());
            adapter.itemCallback.onItemClicked(device);
        }
    }
}