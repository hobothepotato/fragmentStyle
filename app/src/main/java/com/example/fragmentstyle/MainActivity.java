package com.example.fragmentstyle;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.fragmentstyle.ArenaFragment;
import com.example.fragmentstyle.WaypointCoordsDialogFragment;
import com.example.fragmentstyle.BluetoothFragment;
import com.example.fragmentstyle.BluetoothService;
import com.example.fragmentstyle.CustomCommandsFragment;
import com.example.fragmentstyle.ReconfigureDialogFragment;

import static android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED;

public class MainActivity extends AppCompatActivity implements ReconfigureDialogFragment.ReconfigureDialogListener, WaypointCoordsDialogFragment.WaypointCoordsDialogListener{
    //Logging Tag
    private static final String TAG = "Main";

    //Fragment variables
    private static FragmentManager fm;

    //Fragment tags
    public static final String BLUETOOTH_TAG = "BT";
    public static final String ARENA_TAG = "A";
    private static final String CUSTOM_COMMANDS_TAG = "CC";

    //bluetooth objects
    private BluetoothService bluetoothService = BluetoothService.getInstance();
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Fragments
        fm = getSupportFragmentManager();

        //Attaching layout to toolbar object
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        //Bluetooth setup
        onBluetooth();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver, intentFilter);

        //  Listen for Bluetooth connections
        if (mBluetoothAdapter.isEnabled() && bluetoothService.getState() == BluetoothService.State.NONE && bluetoothService.getState() != BluetoothService.State.LISTEN) {
            bluetoothService.disconnect();
        }

        //  Show initial Fragment
        addFragment(BLUETOOTH_TAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //  Unregister Bluetooth events
        unregisterReceiver(mBroadcastReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //  Inflate the menu, adding items to the action bar if present
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_bluetooth_settings:
                //  Switch to Bluetooth fragment
                addFragment(BLUETOOTH_TAG);
                return true;
            case R.id.action_arena:
                //  Switch to Arena fragment
                addFragment(ARENA_TAG);
                return true;
            case R.id.action_custom_commands:
                //  Switch to Custom Command fragment
                addFragment(CUSTOM_COMMANDS_TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void addFragment(String tag) {
        try {
            //  Begin fragment transaction
            FragmentTransaction ft = fm.beginTransaction();
            //  Find tagged fragment
            Fragment taggedFrag = fm.findFragmentByTag(tag);
            //  If tagged fragment exists, use it, else, create a new Fragment
            if (taggedFrag != null) {
                ft.replace(R.id.fragment_container, taggedFrag);
            } else {
                switch (tag) {
                    case ARENA_TAG:
                        ft.replace(R.id.fragment_container, new ArenaFragment(), tag);
                        break;
                    case BLUETOOTH_TAG:
                        ft.replace(R.id.fragment_container, new BluetoothFragment(), tag);
                        break;
                    case CUSTOM_COMMANDS_TAG:
                        ft.replace(R.id.fragment_container, new CustomCommandsFragment(), tag);
                        break;
                }
            }
            ft.commit();
        } catch (Exception e) {
            Log.e(TAG, "addFragment() failed", e);
        }
    }

    /**
     * Handles CustomCommandsFragment's ReconfigureDialogFragment positive button click event
     * @param dialog ReconfigureDialogFragment created by CustomCommandsFragment
     * @param res Resource ID of Preference key updated
     */
    public void onDialogPositiveClick(DialogFragment dialog, int res) {
        //  If CustomCommandsFragment exists, find it
        CustomCommandsFragment ccFragment = (CustomCommandsFragment) fm.findFragmentByTag(CUSTOM_COMMANDS_TAG);
        //  If CustomCommandsFragment exists and is visible, update view
        if (ccFragment != null && ccFragment.isVisible()) {
            ccFragment.setButtonValues();
        }
        //  Notify user of change
        Toast.makeText(this, "Successfully changed custom command", Toast.LENGTH_SHORT).show();
    }

    public void onSuccessfullyAddedWaypoint(int x, int y) {
        //  If ArenaFragment exists, find it
        ArenaFragment arenaFragment = (ArenaFragment) fm.findFragmentByTag(ARENA_TAG);
        //  If ArenaFragment exists and is visible, update arena
        if (arenaFragment != null && arenaFragment.isVisible()) {
            arenaFragment.setWaypoint(x, y);
        }
    }

    /**
     * Listen for Bluetooth events
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_STATE_CHANGED:
                        //  Bluetooth state changed, if Bluetooth is off, nag until it is on
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        if (state == BluetoothAdapter.STATE_OFF) {
                            onBluetooth();
                        }
                        break;
                }
            }
        }
    };

    /**
     * If Bluetooth is not enabled, prompt user to enable Bluetooth
     */
    private void onBluetooth() {
        if (!mBluetoothAdapter.isEnabled()) {
            int ENABLE_BT = 1;
            Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetoothIntent, ENABLE_BT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //  If user does not turn on Bluetooth, prompt user to turn on Bluetooth
        if (resultCode == RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
            onBluetooth();
        } else {
            bluetoothService.disconnect();
        }
    }
}
