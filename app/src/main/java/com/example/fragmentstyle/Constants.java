package com.example.fragmentstyle;

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
    String ROBOT_COMMAND_PREFIX_ARDUINO = "A+";
    String ROBOT_COMMAND_FORWARD = ROBOT_COMMAND_PREFIX_ARDUINO + "W1";
    String ROBOT_COMMAND_BACKWARD = ROBOT_COMMAND_PREFIX_ARDUINO + "S";
    String ROBOT_COMMAND_ROTATE_LEFT = ROBOT_COMMAND_PREFIX_ARDUINO + "A";
    String ROBOT_COMMAND_ROTATE_RIGHT = ROBOT_COMMAND_PREFIX_ARDUINO + "D";
    String ROBOT_COMMAND_BEGIN_EXPLORATION = ROBOT_COMMAND_PREFIX + "explore";
    String ROBOT_COMMAND_BEGIN_FASTEST = ROBOT_COMMAND_PREFIX + "fastest";
    String ROBOT_COMMAND_COORDINATES_START = ROBOT_COMMAND_PREFIX + "coords_start";
    String ROBOT_COMMAND_COORDINATES_WAYPOINT = ROBOT_COMMAND_PREFIX + "";

    /**
     * JSON Objects from Pi
     */

    /**
     * Arena Action constants
     */
    int ARENA_NONE = 100;
    int ARENA_PLACING_ROBOT = 101;
    int ARENA_PLACING_WAYPOINT = 102;
    int ARENA_PLACING_DESTINATION = 103;
    int ARENA_PLACING_OBSTACLE = 104;

}
