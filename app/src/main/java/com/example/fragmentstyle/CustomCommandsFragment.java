package com.example.fragmentstyle;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fragmentstyle.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomCommandsFragment extends Fragment {

    //  Logging
    private static final String TAG = "CUSTOM_COMMANDS_FRAG";

    public enum STATUS{CALIBRATING, MOVEMENT, FASTEST_PATH, EXPLORATION, ERROR, DEFAULT};

    STATUS myStatus = STATUS.DEFAULT;

    //  Dialog variables
    private final static String DIALOG_TAG = "RECONFIGURE";

    //  Member variables
    private static final BluetoothService bs = BluetoothService.getInstance();

    //  Layout members
    private Button customCommand1Button;
    private Button customCommand2Button;

    public CustomCommandsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_custom_commands, container, false);

        //  Get buttons
        customCommand1Button = view.findViewById(R.id.custom_command_1_button);
        customCommand2Button = view.findViewById(R.id.custom_command_2_button);
        Button customCommand1ReconfigureButton = view.findViewById(R.id.custom_command_1_reconfigure_button);
        Button customCommand2ReconfigureButton = view.findViewById(R.id.custom_command_2_reconfigure_button);
        Button changeButton = view.findViewById(R.id.changeStatus);
        final TextView status = view.findViewById(R.id.status);

        //  Set button values
        setButtonValues();

        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (myStatus){
                    case DEFAULT:
                        status.setText("default status");
                        status.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        myStatus = STATUS.CALIBRATING;
                        break;
                    case CALIBRATING:
                        status.setText("calibrating status");
                        status.setBackgroundColor(Color.parseColor("#F3FF00"));
                        myStatus = STATUS.MOVEMENT;
                        break;
                    case MOVEMENT:
                        status.setText("movement status");
                        status.setBackgroundColor(Color.parseColor("#CE33FF"));
                        myStatus = STATUS.FASTEST_PATH;
                        break;
                    case FASTEST_PATH:
                        status.setText("fastest path status");
                        status.setBackgroundColor(Color.parseColor("#33FCFF"));
                        myStatus = STATUS.EXPLORATION;
                        break;
                    case EXPLORATION:
                        status.setText("exploring status");
                        status.setBackgroundColor(Color.parseColor("#FFBB33"));
                        myStatus = STATUS.ERROR;
                        break;
                    case ERROR:
                        status.setText("error status");
                        status.setBackgroundColor(Color.parseColor("#FF3333"));
                        myStatus = STATUS.DEFAULT;
                        break;
                    default:
                        myStatus = STATUS.DEFAULT;
                }
            }
        });

        //  Send custom command F1, if connected to remote device
        customCommand1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Get configured command
                String command = Preferences.readPreference(getContext(), R.string.custom_command_1_key, R.string.custom_command_1_default_value);
                if (bs.getState() == BluetoothService.State.CONNECTED) {
                    bs.sendMessageToRemoteDevice(command);
                } else {
                    Toast.makeText(getContext(), "Not connected to any remote device, unable to send command", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //  Send custom command F2, if connected to remote device
        customCommand2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Get configured command
                String command = Preferences.readPreference(getContext(), R.string.custom_command_2_key, R.string.custom_command_2_default_value);
                if (bs.getState() == BluetoothService.State.CONNECTED) {
                    bs.sendMessageToRemoteDevice(command);
                } else {
                    Toast.makeText(getContext(), "Not connected to any remote device, unable to send command", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //  Reconfigure custom command F1, using ReconfigureDialog
        customCommand1ReconfigureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Create new Dialog
                ReconfigureDialogFragment dialog = new ReconfigureDialogFragment();
                //  Passing arguments to Dialog
                Bundle args = new Bundle();
                args.putInt("res", R.string.custom_command_1_key);
                args.putInt("defaultRes", R.string.custom_command_1_default_value);
                //  Show dialog
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), DIALOG_TAG);
            }
        });

        //  Reconfigure custom command F2, using ReconfigureDialog
        customCommand2ReconfigureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //  Create new Dialog
                ReconfigureDialogFragment dialog = new ReconfigureDialogFragment();
                //  Passing arguments to Dialog
                Bundle args = new Bundle();
                args.putInt("res", R.string.custom_command_2_key);
                args.putInt("defaultRes", R.string.custom_command_2_default_value);
                //  Show dialog
                dialog.setArguments(args);
                dialog.show(getFragmentManager(), DIALOG_TAG);
            }
        });

        return view;
    }

    /**
     * Reloads values on Buttons after reconfiguring
     */
    public void setButtonValues() {
        String customCommand1Value = Preferences.readPreference(getContext(), R.string.custom_command_1_key, R.string.custom_command_1_default_value);
        String customCommand2Value = Preferences.readPreference(getContext(), R.string.custom_command_2_key, R.string.custom_command_2_default_value);
        customCommand1Button.setText(getString(R.string.custom_command_1_button_title, customCommand1Value));
        customCommand2Button.setText(getString(R.string.custom_command_2_button_title, customCommand2Value));
    }
}
