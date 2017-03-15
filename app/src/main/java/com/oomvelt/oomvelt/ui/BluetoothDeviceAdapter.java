package com.oomvelt.oomvelt.ui;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {
    private ArrayList<BluetoothDevice> mDevices;
    private ItemCallback mItemCallback;

    public BluetoothDeviceAdapter(ArrayList<BluetoothDevice> devices) {
        mDevices = devices;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);

        return new ViewHolder(v, this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        holder.name.setText(deviceName != null ? deviceName : deviceAddress);
        holder.address.setText(deviceName == null ? "" : deviceAddress);

        holder.itemView.setTag(device);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void setCallback(ItemCallback itemCallback) {
        mItemCallback = itemCallback;
    }

    public interface ItemCallback {
        void onItemClicked(BluetoothDevice device);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView address;
        BluetoothDeviceAdapter adapter;

        ViewHolder(View itemView, BluetoothDeviceAdapter adapter) {
            super(itemView);

            this.adapter = adapter;

            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(android.R.id.text1);
            address = (TextView) itemView.findViewById(android.R.id.text2);
        }

        @Override
        public void onClick(View v) {
            BluetoothDevice device = mDevices.get(getAdapterPosition());
            adapter.mItemCallback.onItemClicked(device);
        }
    }
}