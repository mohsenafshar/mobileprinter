package com.simonguest.utill;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.simonguest.BTPhotoTransfer.MainActivity;
import com.simonguest.BTPhotoTransfer.R;
import com.simonguest.BTPhotoTransfer.TestSdk;

import java.util.ArrayList;

public class MyDialogFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private Connection connection;

    private ListView listView1;
    private ListView listView2;
    private ArrayList<String> devicesAddress;
    private ArrayList<String> discoveredDevices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_fragment, null, false);
        listView1 = view.findViewById(R.id.list_paired_devices);
        listView2 = view.findViewById(R.id.list_new_devices);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Bundle b = getArguments();
        devicesAddress = b.getStringArrayList("devices");
        //devicesName = b.getStringArrayList("devices_name");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, devicesAddress);

        listView1.setAdapter(adapter1);
        listView1.setOnItemClickListener(this);


        discoveredDevices = new ArrayList<>();
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, discoveredDevices);
        listView2.setAdapter(adapter2);
        listView2.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        Toast.makeText(getActivity(), devicesAddress.get(position), Toast.LENGTH_SHORT).show();
        String address = devicesAddress.get(position).split("\\r\\n|\\n|\\r")[0];
        MainActivity.TARGET_ADDRESS = address;
        Toast.makeText(getActivity(), address, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), TestSdk.class);
        intent.putExtra("ADDRESS", address);
        getActivity().startActivity(intent);
        //connection.startConnection(devicesAddress.get(position));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        connection = (Connection) activity;
    }

    public interface Connection{
        void startConnection(String deviceName);
    }

    public ArrayList<String> getDiscoveredDevices() {
        return discoveredDevices;
    }

    public void setDiscoveredDevices(ArrayList<String> discoveredDevices) {
        this.discoveredDevices = discoveredDevices;
    }


}
