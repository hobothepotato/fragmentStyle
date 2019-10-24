package com.example.fragmentstyle;

import java.util.HashMap;
import java.util.Map;

public interface Constants {

    /**
     * BluetoothSevice Message Types
     */
    int MESSAGE_READ = 0;
    int MESSAGE_WRITE = 1;
    int MESSAGE_TOAST = 2;

    /**
     * Bluetooth Status Types
     */
    int BT_CONNECTED = 100;
    int BT_DISCONNECTING = 101;
    int BT_CONNECTION_LOST = 102;
    int BT_ERROR_OCCURRED = 404;

    /**
     * Arena Properties
     */
    int ARENA_ROBOT_SIZE_COLUMN = 3;
    int ARENA_ROBOT_SIZE_ROW = 3;
    int ARENA_COLUMN_COUNT = 15;
    int ARENA_ROW_COUNT = 20;

    //  Robot commands
    long ARENA_UPDATE_RATE = 1000;
    String ROBOT_COMMAND_PREFIX = "P+";
    String ROBOT_COMMAND_PREFIX_ARDUINO = "O";
    String ROBOT_COMMAND_FORWARD = ROBOT_COMMAND_PREFIX_ARDUINO + "W1";
    String ROBOT_COMMAND_BACKWARD = ROBOT_COMMAND_PREFIX_ARDUINO + "S";
    String ROBOT_COMMAND_ROTATE_LEFT = ROBOT_COMMAND_PREFIX_ARDUINO + "A";
    String ROBOT_COMMAND_ROTATE_RIGHT = ROBOT_COMMAND_PREFIX_ARDUINO + "D";
    String ROBOT_COMMAND_BEGIN_EXPLORATION = "Aexs";
    String ROBOT_COMMAND_BEGIN_FASTEST = "Afps";
    String ROBOT_COMMAND_COORDINATES_START = ROBOT_COMMAND_PREFIX + "coords_start";
    String ROBOT_COMMAND_COORDINATES_WAYPOINT = "O" + "";
    String IMAGE_KEY = "IMAGE_CONTAINER";
    enum STATUS{CALIBRATING, MOVEMENT, IDLE, FASTEST_PATH, EXPLORATION, ERROR, CUSTOM};
//    Map<Integer, String> myMap = new HashMap<Integer, String>() {{
//        put(1, "W_A_U");
//        put(2, "R_A_D");
//        put(3, "G_A_R");
//        put(4, "B_A_L");
//        put(5, "Y_S");
//        put(6, "B_N_1");
//        put(7, "G_N_2");
//        put(8, "R_N_3");
//        put(9, "W_N_4");
//        put(10, "Y_N_5");
//        put(11, "R_L_A");
//        put(12, "G_L_B");
//        put(13, "W_L_C");
//        put(14, "B_L_D");
//        put(15, "Y_L_E");
//    }};

    /**
     * Arena Action constants
     */
    int ARENA_NONE = 100;
    int ARENA_PLACING_ROBOT = 101;
    int ARENA_PLACING_WAYPOINT = 102;
    int ARENA_PLACING_DESTINATION = 103;
    int ARENA_PLACING_OBSTACLE = 104;

}
