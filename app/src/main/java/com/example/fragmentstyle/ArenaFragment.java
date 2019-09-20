package com.example.fragmentstyle;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.fragmentstyle.Constants;
import com.example.fragmentstyle.MainActivity;
import com.example.fragmentstyle.R;
import com.example.fragmentstyle.BluetoothService;
import com.example.fragmentstyle.Preferences;

import static com.example.fragmentstyle.Constants.ARENA_COLUMN_COUNT;
import static com.example.fragmentstyle.Constants.ARENA_NONE;
import static com.example.fragmentstyle.Constants.ARENA_PLACING_ROBOT;
import static com.example.fragmentstyle.Constants.ARENA_PLACING_WAYPOINT;
import static com.example.fragmentstyle.Constants.ARENA_ROW_COUNT;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_BEGIN_EXPLORATION;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_BEGIN_FASTEST;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_COORDINATES_START;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_COORDINATES_WAYPOINT;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_FORWARD;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_ROTATE_LEFT;
import static com.example.fragmentstyle.Constants.ROBOT_COMMAND_ROTATE_RIGHT;
import static com.example.fragmentstyle.Constants.STATUS;

public class ArenaFragment extends Fragment {

    private final String TAG = "ARENA_FRAG:";
    private String MY_TAG = " Shawn_Log: ArenaFragment: ";

    //  Bluetooth Service
    private static final BluetoothService bs = BluetoothService.getInstance();

    //  Arena view
    private ArenaView arenaView;
    private Button manualBtn, updateBtn;

    //  Arena Frame
    private RelativeLayout arenaFrame;

    //  Arena Information
    private TextView robotStatusText;

    //  Member objects
    Button placeWaypointButton;
    Button exploreButton;
    Button fastestPathButton;
    Button gridRobotBtn;
    Button gridWaypointBtn;
    Switch bluetoothStatusSwitch;

    //  Dialog
    AlertDialog.Builder builder;
    AlertDialog dialog;

    //  Arena state
    State state = State.NONE;
    enum State {
        NONE, EXPLORING, FASTEST
    }

    public ArenaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  Register handler callback to handle BluetoothService messages
        bs.registerNewHandlerCallback(bluetoothServiceMessageHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_arena, container, false);

        //  Robot Controls
        ImageButton forwardButton = view.findViewById(R.id.robot_controls_forward);
//        ImageButton backwardButton = view.findViewById(R.id.robot_controls_backward);
        ImageButton rotateLeftButton = view.findViewById(R.id.robot_controls_rotate_left);
        ImageButton rotateRightButton = view.findViewById(R.id.robot_controls_rotate_right);

        //  Robot Controls: OnClickListeners
        forwardButton.setOnClickListener(forwardButtonClickListener);
//        backwardButton.setOnClickListener(backwardButtonClickListener);
        rotateLeftButton.setOnClickListener(rotateLeftButtonClickListener);
        rotateRightButton.setOnClickListener(rotateRightButtonClickListener);

        //  Arena Controls
        placeWaypointButton = view.findViewById(R.id.arena_place_waypoint_button);
        exploreButton = view.findViewById(R.id.arena_explore);
        fastestPathButton = view.findViewById(R.id.arena_fastest);

        //  Robot Controls: OnClickListeners
        placeWaypointButton.setOnClickListener(placeWaypointButtonClickListener);
        exploreButton.setOnClickListener(exploreButtonOnClickListener);
        fastestPathButton.setOnClickListener(fastestPathButtonOnClickListener);

        //  Map Descriptor
        Button mapDescriptorButton = view.findViewById(R.id.arena_map_descriptor);
        mapDescriptorButton.setOnClickListener(mapDescriptorButtonClickListener);

        //  Arena clear/load
        manualBtn = view.findViewById(R.id.manualBtn);
        updateBtn = view.findViewById(R.id.updateBtn);
        manualBtn.setOnClickListener(manualButtonClickListener);
        updateBtn.setOnClickListener(updateButtonClickListener);

        //  Bluetooth Status
        bluetoothStatusSwitch = view.findViewById(R.id.bluetooth_status);
        if (bs.getState() == BluetoothService.State.CONNECTED) {
            bluetoothStatusSwitch.setChecked(true);
        } else {
            bluetoothStatusSwitch.setChecked(false);
        }

        //  Create Arena
        arenaFrame = view.findViewById(R.id.arena_frame);

        //  Resize Arena frame to display arena properly
        ViewGroup.LayoutParams layoutParams = arenaFrame.getLayoutParams();

        //  Adjust arena width and height to display cells
        layoutParams.width = layoutParams.width - (layoutParams.width % ARENA_COLUMN_COUNT) + 1;
        layoutParams.height = layoutParams.height - (layoutParams.height % ARENA_ROW_COUNT) + 1;
        arenaFrame.setLayoutParams(layoutParams);

        //  Add ArenaView to frame
        arenaView = new ArenaView(getContext());
        arenaView.setFragment(this);
        // Set a way to touch grid to set things.
        arenaFrame.addView(arenaView);

        //  Btn to set actions on Grid.
        gridRobotBtn = view.findViewById(R.id.robotGridBtn);
        gridRobotBtn.setOnClickListener(gridRobotBtnOnClickListener);
        gridWaypointBtn = view.findViewById(R.id.waypointGridBtn);
        gridWaypointBtn.setOnClickListener(gridWaypointBtnOnClickListener);

        //  Arena Status Text
        robotStatusText = view.findViewById(R.id.arena_robot_status);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        arenaFrame.removeAllViews();
    }

    /**
     * Send message to remote device notifying them of robot initial location
     * @param position (x, y) coordinates of robot's initial location
     * @param width Width of cell
     * @param height Height of cell
     * @param rotation Rotation of robot
     */
    public void sendStartingPosition(Point position, int width, int height, float rotation) {
        bs.sendMessageToRemoteDevice(ROBOT_COMMAND_COORDINATES_START + ":" + (position.x / width) + "," + (ARENA_ROW_COUNT - (position.y / height)) + "," + (long) rotation);
    }

    /**
     * Loads a saved Arena from SharedPreferences on load
     */
    public void loadSavedArena() {
        String savedRobotPosition = Preferences.readPreference(getContext(), R.string.arena_robot_position);
        String savedP1Descriptor = Preferences.readPreference(getContext(), R.string.arena_p1_descriptor);
        String savedP2Descriptor = Preferences.readPreference(getContext(), R.string.arena_p2_descriptor);
        String savedWaypoint = Preferences.readPreference(getContext(), R.string.arena_waypoint);
        Log.d(MY_TAG, "loadSavedArena: savedWaypoint: "+savedWaypoint);

        //  Load saved robot position and direction
        if (savedRobotPosition.trim().length() != 0) {
            String[] robotPos = savedRobotPosition.split(",");
            int x = Integer.parseInt(robotPos[0]);
            int y = Integer.parseInt(robotPos[1]);
            float rot = Float.parseFloat(robotPos[2]);
            arenaView.moveRobot(x, y, rot);
        }

        //  Load P1 descriptor and update map
        if (savedP1Descriptor.trim().length() != 0) {
            arenaView.updateMapP1(savedP1Descriptor);
        }

        //  Load P2 descriptor and update map
        if (savedP2Descriptor.trim().length() != 0) {
            arenaView.updateMapP2(savedP2Descriptor);
        }

        //  Load way point and update map
        if (savedWaypoint.trim().length() != 0) {
            String[] wpPos = savedWaypoint.split(",");
            int wpX = Integer.parseInt(wpPos[0]);
            int wpY = Integer.parseInt(wpPos[1]);
            arenaView.setWaypoint(wpX, wpY);
        }
    }

    /**
     * Move robot in Arena given X, Y, direction
     * @param robotMidX X value of robot middle
     * @param robotMidY Y value of robot middle
     * @param robotDir Cardinal direction that robot is facing
     */
    private void moveRobot(String robotMidX, String robotMidY, String robotDir) {
        if (robotMidX.trim().length() != 0 && robotMidY.trim().length() != 0) {
            //  Update robot values (robotMid, robotDir)
            int x = Integer.parseInt(robotMidX);
            int y = Integer.parseInt(robotMidY);

            //  Convert robotDir to degree values
            int rot = 0;
            switch (robotDir) {
                case "north":
                    rot = 0;
                    break;
                case "south":
                    rot = 180;
                    break;
                case "east":
                    rot = 90;
                    break;
                case "west":
                    rot = 270;
                    break;
            }

            //  Finally, move robot to received location and save into SharedPreferences for future use
            arenaView.moveRobot(x, y, rot);
            Preferences.savePreference(getContext(), R.string.arena_robot_position, (robotMidX + "," + robotMidY + "," + rot));
        }
    }

    /**
     * After receiving message from Raspberry Pi, process message to perform actions in ArenaView
     */
    private void processMessage(String message) {
        //  Variables
        String robotMidX = "";
        String robotMidY = "";
        String robotDir = "";
        Pattern pattern;
        Matcher matcher;
        String[] contents;
        Log.d(MY_TAG, "process Message: state: "+state);

        if (message.startsWith("/s")){
            String newmessage;
            newmessage = message.substring(2);
            Log.d(TAG, "processMessage: "+newmessage);
            setStatus(STATUS.CUSTOM, newmessage);
        }

        else if (state == State.EXPLORING) {
            // SET TO CHECK IF /m for map descriptor. instead of state exploring
            /**
             * Messages arriving have to be deconstructed and processed accordingly.
             *  Messages come in the following format:
             *      paddedP1, paddedP2, x, y, Direction (i.e. NORTH, SOUTH, EAST, WEST)
             *  Example:
             *      ffff...ffff,0000...800,1,14,NORTH
             */
            Log.d(MY_TAG, "Process Message (EXPLORE) message: "+message);
            //  Regex expression to match correct messages
            pattern = Pattern.compile("[0-9a-fA-F]+,[0-9a-fA-F]+,[0-9]+,[0-9]+,(?:(?:north|south)(?:[ ](?:east|west))?|east|west)", Pattern.CASE_INSENSITIVE);
            //  Matcher that performs matching of regex to message
            matcher = pattern.matcher(message);
            /*
            *   If the correct message is found at the start of the message,
            *   return the correct message and perform processing,
            *   while dropping any concatenated messages, if any.
            *
            *   Here, we drop concatenated messages as less information
            *   is lost. A single buffer is likely to only concatenate
            *   two to three steps, and is an acceptable loss.
            * */
            while(matcher.find()) {
                Log.d(MY_TAG, "IN WHILE LOOP");
                message = matcher.group();
                //  Get contents of message
                contents = message.split(",");
                String paddedP1 = contents[0].trim();
                String paddedP2 = contents[1].trim();
                robotMidX = contents[2].trim();
                robotMidY = contents[3].trim();
                robotDir = contents[4].trim().toLowerCase();
                Log.d(MY_TAG, "paddedP1: "+paddedP1+", paddedP2: "+paddedP2+", robotMidX: "+robotMidX+", robotMidY: "+robotMidY+", robotDir: "+robotDir);

                //  Update map with P1 descriptor and save into SharedPreferences for future use
                arenaView.updateMapP1(paddedP1);
                Preferences.savePreference(getContext(), R.string.arena_p1_descriptor, paddedP1);

                //  Update map with P2 descriptor and sae into SharedPreferences for future use
                arenaView.updateMapP2(paddedP2);
                Preferences.savePreference(getContext(), R.string.arena_p2_descriptor, paddedP2);

                //  Move robot
                moveRobot(robotMidX, robotMidY, robotDir);
            }
        } else if (state == State.FASTEST) {
            Log.d(MY_TAG, "Process Message (FASTEST) message: "+message);
            /**
             * Messages arriving have to be deconstructed and processed accordingly
             *  Messages come in the following format:
             *      x, y, Direction (i.e. NORTH, SOUTH, EAST, WEST)
             *  Example:
             *      1,1,NORTH
             */
            //  Regex expression to match correct messages
            pattern = Pattern.compile("[0-9]+,[0-9]+,(?:(?:north|south)(?:[ ](?:east|west))?|east|west)", Pattern.CASE_INSENSITIVE);
            //  Matcher that performs matching of regex to message
            matcher = pattern.matcher(message);
            /*
            *   If correct messages are found, perform processing for each message.
            *
            *   Here, we perform a more diligent search for correct messages as
            *   more information can be lost due to a smaller message size, if
            *   we were to drop all messages that did not arrive first.
            * */
            while (matcher.find()) {
                message = matcher.group();

                //  Get contents of message
                contents = message.split(",");
                robotMidX = contents[0].trim();
                robotMidY = contents[1].trim();
                robotDir = contents[2].trim().toLowerCase();

                //  Move robot
                moveRobot(robotMidX, robotMidY, robotDir);
            }
        }
    }

    private View.OnClickListener forwardButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (bs.getState() == BluetoothService.State.CONNECTED) {
                arenaView.setArenaAction(ARENA_NONE);
                bs.sendMessageToRemoteDevice(ROBOT_COMMAND_FORWARD);
            }
        }
    };

//    private View.OnClickListener backwardButtonClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (bs.getState() == BluetoothService.State.CONNECTED) {
//                arenaView.setArenaAction(ARENA_NONE);
//                bs.sendMessageToRemoteDevice(ROBOT_COMMAND_BACKWARD);
//            }
//        }
//    };

    private View.OnClickListener rotateLeftButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (bs.getState() == BluetoothService.State.CONNECTED) {
                arenaView.setArenaAction(ARENA_NONE);
                bs.sendMessageToRemoteDevice(ROBOT_COMMAND_ROTATE_LEFT);
            }
        }
    };

    private View.OnClickListener rotateRightButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (bs.getState() == BluetoothService.State.CONNECTED) {
                arenaView.setArenaAction(ARENA_NONE);
                bs.sendMessageToRemoteDevice(ROBOT_COMMAND_ROTATE_RIGHT);
            }
        }
    };

    private View.OnClickListener placeWaypointButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //  Reset status text
            setStatus(STATUS.CUSTOM, String.valueOf(R.string.place_waypoint));
            //robotStatusText.setText(R.string.place_waypoint);
            //  Create new Dialog
            WaypointCoordsDialogFragment dialog = new WaypointCoordsDialogFragment();
            //  Show dialog
            dialog.show(getFragmentManager(), "MAP_DESC");
        }
    };

    private View.OnClickListener gridRobotBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //robotStatusText.setText("Setting Robot on Grid");
            setStatus(STATUS.CUSTOM, "Setting Robot on Grid");
            arenaView.setArenaAction(ARENA_PLACING_ROBOT);
        }
    };

    private View.OnClickListener gridWaypointBtnOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            //robotStatusText.setText("Setting Waypoint on Grid");
            setStatus(STATUS.CUSTOM, "Setting waypoint on Grid");
            arenaView.setArenaAction(ARENA_PLACING_WAYPOINT);
        }
    };

    public void setWaypoint(int x, int y) {
        arenaView.setWaypoint(x, y);
    }

    private View.OnClickListener exploreButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            arenaView.setArenaAction(ARENA_NONE);

            //  Get way point information
            final String waypointMsg = arenaView.getWaypointInfo();

            //  Check if waypoint is placed
            if (waypointMsg.trim().length() != 0) {
                //  Get way point information for display
                String[] wp = waypointMsg.split(",");
                //  Build alert dialog
                builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Start Exploration");
                builder.setMessage("Do you really want to start exploration?\n\nWaypoint is at (" + wp[0] + "," + wp[1] + ")");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (bs.getState() == BluetoothService.State.CONNECTED) {
                            //  Clear Arena
                            arenaView.clearArena();
                            //  Place robot at default position
                           // arenaView.moveRobot(1, 1, 180);
                            //Preferences.savePreference(getContext(), R.string.arena_robot_position, "1,1,180.0");
                            //  Send way point coordinates
                            Log.d(MY_TAG, "explore on click listener: waypoint: "+ROBOT_COMMAND_COORDINATES_WAYPOINT+""+waypointMsg);
                            bs.sendMessageToRemoteDevice(ROBOT_COMMAND_COORDINATES_WAYPOINT + "" + waypointMsg);
                            //  Save way point coordinates
                            Preferences.savePreference(getContext(), R.string.arena_waypoint, waypointMsg);
                            //  Send explore keyword
                            bs.sendMessageToRemoteDevice(ROBOT_COMMAND_BEGIN_EXPLORATION);
                            //  Set state
                            state = State.EXPLORING;
                            //robotStatusText.setText(R.string.robot_status_exploring);
                            setStatus(STATUS.EXPLORATION, "Exploring");
                        } else {
                            Toast.makeText(getContext(), "Bluetooth is not connected", Toast.LENGTH_LONG).show();
                            //  Switch back to Bluetooth Fragment
                            MainActivity.addFragment(MainActivity.BLUETOOTH_TAG);
                        }
                    }});
                builder.setNegativeButton(android.R.string.no, null);

                dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(getContext(), "Waypoint is not placed", Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener fastestPathButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            arenaView.setArenaAction(ARENA_NONE);

            //  Get way point information
            final String waypointMsg = arenaView.getWaypointInfo();

            if (waypointMsg.trim().length() != 0) {
                //  Build alert dialog
                builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Start Fastest Path");
                builder.setMessage("Do you really want to start fastest path?");
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //  Send command to remote device to initiate fastest path
                        if (bs.getState() == BluetoothService.State.CONNECTED) {
                            //  Place robot at default position
//                            arenaView.moveRobot(1, 1, 180);
//                            Preferences.savePreference(getContext(), R.string.arena_robot_position, "1,1,180.0");
                            //  Send command to remote device
                            bs.sendMessageToRemoteDevice(ROBOT_COMMAND_BEGIN_FASTEST);
                            //  Set state
                            state = State.FASTEST;
                            //robotStatusText.setText(R.string.robot_status_fastest_path);
                            setStatus(STATUS.FASTEST_PATH, "Fastest Path");
                        } else {
                            Toast.makeText(getContext(), "Bluetooth is not connected", Toast.LENGTH_LONG).show();
                            //  Switch back to Bluetooth Fragment
                            MainActivity.addFragment(MainActivity.BLUETOOTH_TAG);
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.no, null);

                dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(getContext(), "Waypoint is not placed", Toast.LENGTH_LONG).show();
            }
        }
    };

    private View.OnClickListener mapDescriptorButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //  Reset status text
            //robotStatusText.setText(R.string.robot_status_unknown);
            setStatus(STATUS.CUSTOM, String.valueOf(R.string.robot_status_unknown));
            //  Create new Dialog
            MapDescriptorDialogFragment dialog = new MapDescriptorDialogFragment();
            //  Passing arguments to Dialog
            Bundle args = new Bundle();
            args.putString("p1", Preferences.readPreference(getContext(), R.string.arena_p1_descriptor));
            args.putString("p2", Preferences.readPreference(getContext(), R.string.arena_p2_descriptor));
            //  Show dialog
            dialog.setArguments(args);
            dialog.show(getFragmentManager(), "MAP_DESC");
        }
    };

    private View.OnClickListener manualButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //  Clear arena
            Log.d(MY_TAG, "manualButtonClickListener arenaVIew.getManualArena(): "+arenaView.getManualArena());
            if(arenaView.getManualArena() == 0){
                arenaView.setManualArena(1);
                bs.sendMessageToRemoteDevice("ManualMode");
                //robotStatusText.setText("In Manual Receiving Mode");
                setStatus(STATUS.CUSTOM, "In Manual Receiving Mode");
                manualBtn.setText("Automatic");
            }else{
                bs.sendMessageToRemoteDevice("ManualMode");
                //robotStatusText.setText("In Automatic Receiving Mode");
                setStatus(STATUS.CUSTOM, "In Automatic Receiving Mode");
                arenaView.setManualArena(0);
                manualBtn.setText("Manual");
            }
        }
    };

    private View.OnClickListener updateButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(arenaView.getManualArena() == 1) bs.sendMessageToRemoteDevice("Update");
            else  Toast.makeText(getContext(), "Must set to Manual before update is available.", Toast.LENGTH_SHORT).show();
        }
    };

    private void setStatus(STATUS type, String message){
        switch (type){
            case IDLE:
                //do something
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                robotStatusText.setText("Idling.....");
                break;
            case ERROR:
                //do something
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
            case CUSTOM:
                //do something
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
            case MOVEMENT:
                //do sometthing
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
            case CALIBRATING:
                //do something
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
            case EXPLORATION:
                //do something
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
            case FASTEST_PATH:
                //do seomthing
                robotStatusText.setBackgroundColor(Color.parseColor("#FF1A1A"));
                break;
        }
        robotStatusText.setText(message);
    }

    /**
     * Handle messages from BluetoothService
     */
    private final Handler.Callback bluetoothServiceMessageHandler = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            try {
                Log.d(MY_TAG, "ArenaFragment Handler");
                Log.d(MY_TAG, "BluetoothServiceMessageHandler: message.what: "+message.what);
                // 0 = Message_read
                // 1 = message_write
                // 1 = message_write
                // 100 = BT_Connected
                switch (message.what) {
                    case Constants.MESSAGE_READ:
                        //  Reading message from remote device
                        String receivedMessage = message.obj.toString();
                        //  Handle message from Pi
                        Log.d(MY_TAG, "BluetoothServiceMessageHandler: receivedMessage: "+receivedMessage);
                        processMessage(receivedMessage);
                        break;
                    case Constants.MESSAGE_WRITE:
                        Log.d(MY_TAG, "BluetoothServiceMessageHandler: MESSAGE_WRITE: message string: "+message.obj.toString());
                        //  Writing message to remote device
                        break;
                    case Constants.BT_CONNECTED:
                        //  Successfully connected to remote device
                        String deviceName = message.obj.toString();
                        Toast.makeText(getContext(), "Connected to remote device: " + deviceName, Toast.LENGTH_SHORT).show();
                        bluetoothStatusSwitch.setChecked(true);
                        break;
                    case Constants.BT_DISCONNECTING:
                    case Constants.BT_CONNECTION_LOST:
                        //  Connection to remote device lost
                        bs.disconnect();
                        Toast.makeText(getContext(), "Connection to remote device lost", Toast.LENGTH_SHORT).show();
                        //  Switch back to Bluetooth Fragment
                        MainActivity.addFragment(MainActivity.BLUETOOTH_TAG);
                        bluetoothStatusSwitch.setChecked(false);
                        break;
                    case Constants.BT_ERROR_OCCURRED:
                        //  An error occured during connection
                        bs.disconnect();
                        Toast.makeText(getContext(), "A Bluetooth error occurred", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (Throwable t) {
                Log.e(TAG,null, t);
            }
            return false;
        }
    };
}