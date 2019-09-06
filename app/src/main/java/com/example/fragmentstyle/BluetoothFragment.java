package com.example.fragmentstyle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class BluetoothFragment extends Fragment {

    //  Logging
    private final String TAG = "BT_CONNECT_FRAG:";
    private String MY_TAG = " Shawn_Log: BluetoothFragment: ";

    //  Bluetooth
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mDiscoveredDevices;
    private String mConnectedDeviceName;

    //  Unconnected Layout
    private LinearLayout mUnconnectedLayout;
    private Button mScanButton;
    private ListView mPairedDeviceList;
    private ListView mDiscoveredDeviceList;
    private ArrayAdapter<String> mPairedDeviceListAdapter;
    private ArrayAdapter<String> mDiscoveredDeviceListAdapter;

    //  Connected Layout
    private LinearLayout mConnectedLayout;
    private TextView mConnectedDeviceText;
    private Button mDisconnectButton;
    private EditText mSendBluetoothMessage;
    private Button mSendBluetoothMessageButton;
    private ListView mBluetoothMessages;
    private ArrayAdapter<String> mBluetoothMessagesListAdapter;

    //  Bluetooth Service
    public static BluetoothService bluetoothService;

    public BluetoothFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Get Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //  Create new Set of BluetoothDevices
        mDiscoveredDevices = new HashSet<>();

        //  Create intent filter and register broadcast receiver
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver, mIntentFilter);

        //  Get instance of BluetoothService
        bluetoothService = BluetoothService.getInstance();

        //  Register handler callback to handle BluetoothService messages
        bluetoothService.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //  Stop Bluetooth discovery
        mBluetoothAdapter.cancelDiscovery();

        //  Unregister receivers
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        //  Instantiate layouts
        mUnconnectedLayout = view.findViewById(R.id.bluetooth_unconnected_container);
        mConnectedLayout = view.findViewById(R.id.bluetooth_connected_container);

        //  Instantiate unconnected layout
        mScanButton = view.findViewById(R.id.bluetooth_connect_button);
        ListView mPairedDeviceList = view.findViewById(R.id.bluetooth_paired_device_list);
        mPairedDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mPairedDeviceList.setAdapter(mPairedDeviceListAdapter);
        mDiscoveredDeviceList = view.findViewById(R.id.bluetooth_discovered_device_list);
        mDiscoveredDeviceListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mDiscoveredDeviceList.setAdapter(mDiscoveredDeviceListAdapter);

        //  Instantiate connected layout
        mDisconnectButton = view.findViewById(R.id.bluetooth_disconnect_button);
        mConnectedDeviceText = view.findViewById(R.id.bluetooth_connected_device);
        mBluetoothMessages = view.findViewById(R.id.bluetooth_messages);
        mBluetoothMessagesListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        mBluetoothMessages.setAdapter(mBluetoothMessagesListAdapter);
        mSendBluetoothMessage = view.findViewById(R.id.bluetooth_message);
        mSendBluetoothMessageButton = view.findViewById(R.id.send_bluetooth_message_button);

        //  Get paired devices and display on list
        final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice pairedDevice : pairedDevices) {
                mPairedDeviceListAdapter.add(pairedDevice.getName());
            }
        }

        //  If paired device is clicked, connect
        mPairedDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenDeviceName = ((TextView) view).getText().toString();
                for (BluetoothDevice pairedDevice : pairedDevices) {
                    if (pairedDevice.getName().equalsIgnoreCase(chosenDeviceName)) {
                        connect(pairedDevice);
                        break;
                    }
                }
            }
        });

        //  Scan button action
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscoveredDeviceListAdapter.clear();
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();
            }
        });

        //  If discovered device is clicked, connect
        mDiscoveredDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String chosenDeviceName = ((TextView) view).getText().toString();
                for (BluetoothDevice device : mDiscoveredDevices) {
                    if (device.getName().equalsIgnoreCase(chosenDeviceName)) {
                        mDiscoveredDeviceListAdapter.clear();
                        mDiscoveredDevices.clear();
                        connect(device);
                        break;
                    }
                }
            }
        });

        //  Disconnect button action
        mDisconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothService.disconnect();
            }
        });

        //  Send Message button action
        mSendBluetoothMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mSendBluetoothMessage.getText().toString().trim();
                if (message.length() != 0) {
                    mSendBluetoothMessage.setText("");
                    bluetoothService.sendMessageToRemoteDevice(message);
                }
            }
        });

        //  If already connected to a device, restore connected view
        if (bluetoothService.getState() == BluetoothService.State.CONNECTED) {
            setConnectedState(bluetoothService.getConnectedDeviceName());
        }

        return view;
    }

    /**
     * Handle Bluetooth broadcast events
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //  Bluetooth device discovery started
                        mScanButton.setText(R.string.bluetooth_discovering);
                        mScanButton.setEnabled(false);
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //  Bluetooth device discovery completed
                        mScanButton.setText(R.string.bluetooth_scan);
                        mScanButton.setEnabled(true);
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        //  Bluetooth device discovered, get information from Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String deviceName = device.getName();
                        mDiscoveredDeviceListAdapter.add(deviceName);
                        mDiscoveredDeviceListAdapter.notifyDataSetChanged();
                        mDiscoveredDevices.add(device);
                        break;
                }
            }
        }
    };

    /**
     * Handle messages from BluetoothService
     */
    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                Log.d(MY_TAG, "BluetoothFragment Handler");
                Log.d(MY_TAG, "bluetoothServiceMessageHandler: message.what: "+message.what);
                //Message_Read = 0
                //MESSAGE_WRITE = 1
                //BT_CONNECTED = 100
                //BT_DISCONNECTING = 101
                switch (message.what) {
                    case Constants.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        mBluetoothMessagesListAdapter.add(mConnectedDeviceName + ": " + receivedMessage);
                        return false;
                    case Constants.MESSAGE_WRITE:
                        //  Writing message to remote device
                        String sendingMessage = message.obj.toString();
                        mBluetoothMessagesListAdapter.add("ARC: " + sendingMessage);
                        return false;
                    case Constants.BT_CONNECTED:
                        //  Successfully connected to remote device
                        String deviceName = message.obj.toString();
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        setConnectedState(deviceName);
                        //  Switch to Arena
                        MainActivity.addFragment(MainActivity.ARENA_TAG);
                        return false;
                    case Constants.BT_DISCONNECTING:
                    case Constants.BT_CONNECTION_LOST:
                        //  Connection to remote device lost
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        setDisconnectedState();
                        return false;
                    case Constants.BT_ERROR_OCCURRED:
                        //  An error occured during connection
                        Log.e(TAG, "BT_ERROR_OCCURRED: A Bluetooth error occurred");
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        setDisconnectedState();
                        return false;
                }
            } catch (Throwable t) {
                Log.e(TAG,null, t);
            }

            return false;
        }
    };

    /**
     * Connect to remote Bluetooth device
     * @param device Remote Bluetooth device
     */
    private void connect(BluetoothDevice device) {
        bluetoothService.connect(device);
    }

    /**
     * Set conected state, changing UI
     * @param deviceName Name of the remote device
     */
    private void setConnectedState(String deviceName) {
        mConnectedDeviceName = deviceName;
        mConnectedDeviceText.setText(deviceName);
        mUnconnectedLayout.setVisibility(View.GONE);
        mConnectedLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Set disconnected state, changing UI and Bluetooth Service
     */
    private void setDisconnectedState() {
        bluetoothService.disconnect();
        bluetoothService.listen();
        mConnectedDeviceName = "";
        mConnectedDeviceText.setText("");
        mBluetoothMessagesListAdapter.clear();
        mUnconnectedLayout.setVisibility(View.VISIBLE);
        mConnectedLayout.setVisibility(View.GONE);
    }
}