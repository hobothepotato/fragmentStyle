package com.example.fragmentstyle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import static com.example.fragmentstyle.Constants.ARENA_COLUMN_COUNT;
import static com.example.fragmentstyle.Constants.ARENA_NONE;
import static com.example.fragmentstyle.Constants.ARENA_PLACING_ROBOT;
import static com.example.fragmentstyle.Constants.ARENA_PLACING_WAYPOINT;
import static com.example.fragmentstyle.Constants.ARENA_ROBOT_SIZE_COLUMN;
import static com.example.fragmentstyle.Constants.ARENA_ROBOT_SIZE_ROW;
import static com.example.fragmentstyle.Constants.ARENA_ROW_COUNT;
import static com.example.fragmentstyle.Cell.Type2.EMPTY;
import static com.example.fragmentstyle.Cell.Type2.OBSTACLE;
import static com.example.fragmentstyle.Cell.Type4.IMAGE;


public class ArenaView extends SurfaceView implements SurfaceHolder.Callback {

    private final String TAG = "ARENA_VIEW";
    private final String MY_TAG = "Shawn_Log: ArenaView:";
    private MainThread thread;

    //  Parent
    ArenaFragment mFragment;

    //  Arena
    int MIN_HEIGHT = 0;
    int MAX_HEIGHT;
    int MIN_WIDTH = 0;
    int MAX_WIDTH;
    int imageX = 50, imageY = 50;
    int imageID =-1;// Set image out of range so that it doesnt place anything on the map.

    //  Robot
    private static Robot robot;

    //  Robot width and height
    public static int ROBOT_WIDTH;
    public static int ROBOT_HEIGHT;

    //  When clicking on the grid, we place it in the center of a cell.
    //  However, since we want to render the robot in line with the
    //  grid and not in the center of the cell, an offset is needed
    //  during rendering.
    public static int ROBOT_WIDTH_OFFSET;
    public static int ROBOT_HEIGHT_OFFSET;

    //  Waypoint
    private static Cell wp;

    //  Arena Cells
    private int CELL_HEIGHT;
    private int CELL_WIDTH;
    private Cell[][] cells;

    //  Current Action
    private int arenaAction = ARENA_NONE;
    private int manualState = 0;

    //  Arena objects
    private StringBuilder sb;

    private int robotX = 1, robotY = 1, robotRotation = 0;

    public ArenaView(Context context) {
        super(context);

        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        //  Structural change made to surface (format/size), update image on surface
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        //  Restart MainThread
        thread = new MainThread(getHolder(), this);

        //  Get dimensions of Arena
        MIN_HEIGHT = 0;
        MAX_HEIGHT = getHolder().getSurfaceFrame().height();
        MIN_WIDTH = 0;
        MAX_WIDTH = getHolder().getSurfaceFrame().width();

        //  Cell dimensions
        CELL_HEIGHT = (MAX_HEIGHT - MIN_HEIGHT) / ARENA_ROW_COUNT;
        CELL_WIDTH = (MAX_WIDTH - MIN_WIDTH) / ARENA_COLUMN_COUNT;

        //  Grid cells
        cells = new Cell[ARENA_ROW_COUNT][ARENA_COLUMN_COUNT];
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                int x = (CELL_WIDTH / 2) + (j * CELL_WIDTH);
                int y = (CELL_HEIGHT / 2) + (i * CELL_HEIGHT);
                cells[i][j] = new Cell(x, y, CELL_HEIGHT, CELL_WIDTH, i, j);
            }
        }

        //  Get robot dimensions based on Cells
        ROBOT_WIDTH = CELL_WIDTH * ARENA_ROBOT_SIZE_COLUMN;
        ROBOT_HEIGHT = CELL_HEIGHT * ARENA_ROBOT_SIZE_ROW;
        ROBOT_WIDTH_OFFSET = ROBOT_WIDTH / 2;
        ROBOT_HEIGHT_OFFSET = ROBOT_HEIGHT / 2;

        //  Start render thread
        thread.setRunning(true);
        thread.start();

        //  Load previously saved arena
        mFragment.loadSavedArena();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        while (retry) {
            try {
                clearArena();
                thread.setRunning(false);
                //  Finish running thread, and terminates it
                thread.join();
                retry = false;
            } catch (Exception e) {
                Log.e(TAG, "Error occurred on surfaceDestroyed(): " + e);
            }
        }
        thread = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Cell cell = getTouchedCell(x, y);
        Log.d(MY_TAG, "action: "+event.getAction());
        // ACTION_DOWN = 0
        // ACTION_UP = 1
        // ACTION_MOVE = 2
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (cell != null) {
                    // ARENA_NONE = 100
                    // ARENA_PLACING_ROBOT = 101
                    // ARENA_PLACING_WAYPOINT = 102
                    Log.d(MY_TAG, "arenaAction: ACTION_DOWN: "+arenaAction);
                    switch (arenaAction) {
                        case ARENA_PLACING_ROBOT:
                            //  Place robot in Arena
                            Log.d(MY_TAG, "Robot coordinates: ("+cell.getX()+","+cell.getY()+")");
                            //robot = new Robot(cell.getX(), cell.getY(), ROBOT_HEIGHT, ROBOT_WIDTH);
                            double newX = Math.floor(cell.getX()/100);
                            double newY = Math.floor(cell.getY()/ 100);
                            Log.d(MY_TAG, "Robot coordinates new : ("+newX+","+newY+")");
                            //robot = new Robot((int)newX, (int)newY, ROBOT_HEIGHT, ROBOT_WIDTH);
                            moveRobot(cell.getColumnIndex(), Math.abs(19-cell.getRowIndex()), 0);
                            setRobotLoc(cell.getColumnIndex(), Math.abs(19-cell.getRowIndex()));
                            break;
                        case ARENA_PLACING_WAYPOINT:
                            //  Place waypoint in Arena

                            //  Remove current way point
                            if (wp != null) {
                                wp.setType3(Cell.Type3.NONE);
                            }

                            //  If same cell, remove current way point
                            if (wp == cell) {
                                wp = null;
                            } else {
                                //  If different cell, add new current way point
                                wp = cell;
                                cell.setType3(Cell.Type3.WAYPOINT);
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (cell != null) {
                    // ARENA_PLACING_ROBOT = 101
                    Log.d(MY_TAG, "arenaAction: ACTION_MOVE: "+arenaAction);
                    switch (arenaAction) {
                        case ARENA_PLACING_ROBOT:
                            //  Rotate robot in Arena
                            if (x > (robot.getPosition().x + ROBOT_WIDTH)) {
                                robot.setRotation(90);
                                robotRotation = 90;
                            } else if (y > (robot.getPosition().y + ROBOT_HEIGHT)) {
                                robot.setRotation(180);
                                robotRotation = 180;
                            } else if (x < robot.getPosition().x) {
                                robot.setRotation(270);
                                robotRotation = 270;
                            } else {
                                robot.setRotation(0);
                                robotRotation = 0;
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // ARENA_PLACING_ROBOT = 101
                Log.d(MY_TAG, "arenaAction: ACTION_UP: "+arenaAction);
                switch (arenaAction) {
                    case ARENA_PLACING_ROBOT:
                        if (robot != null) {
                            sendRobotStartingPosition();
                        }
                        break;
                }
                break;
        }
        return true;
    }

    public void update() {
        //  Update robot to specific point
        if (robot != null) {
            robot.update();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        //  Canvas color
        canvas.drawColor(Color.WHITE);

        //  Draw Arena
        if (cells != null) {
            for (int i = 0; i < ARENA_ROW_COUNT; i++) {
                for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                    cells[i][j].draw(canvas);
                }
            }
        }

        //  Draw Robot on screen
        if (robot != null) {
            robot.draw(canvas);
        }

        //  Draw Waypoint on screen
        if (wp != null) {
            wp.draw(canvas);
        }
    }

    /**
     * Returns Cell that was touched
     * @param x Position where user touched in the x-axis
     * @param y Position where user touched in the y-axis
     * @return Cell that was touched
     */
    private Cell getTouchedCell(float x, float y) {
        //  Loop through arena and return chosen cell based on x and y coordinates
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if (cells[i][j].isClicked(x, y)) {
                    return cells[i][j];
                }
            }
        }

        //  If touched outside of Grid, return to (0, 0)
        return null;
    }

    /**
     * Generates hexadecimal map descriptor string of given format.
     *  - First and last two bits set to 11, padding the bit sequence to 38 bytes
     *  - Row by row from bottom to top, left to right
     *  - Explored/Unexplored state of a cell is represented by 1 bit
     *  - Value 0 represents a cell that is unexplored/unknown
     *  - Value 1 represents a cell whose state is known or has been explored
     * @return Part 1 map descriptor string
     */
    public String generatePart1MapDescriptor() {
        //  todo: Determine if this is required
        sb = new StringBuilder();

        //  Pad first two bits
        sb.append("11");

        //  Traverse arena row by row from bottom to top, left to right
        for (int i = ARENA_ROW_COUNT - 1; i > -1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                //  Set value 0 if unexplored/unknown, 1 if otherwise
                if (cells[i][j].getType().equals(Cell.Type.UNEXPLORED)
                        || cells[i][j].getType().equals(Cell.Type.UNKNOWN)) {
                    sb.append("0");
                } else {
                    sb.append("1");
                }
            }
        }

        //  Pad last two bits
        sb.append("11");

        convertBinaryToHexadecimalString(sb);

        return sb.toString();
    }

    /**
     * Generates hexadecimal map descriptor string of given format.
     *  - Only cells previously marked as explored in Part 1 will be represented
     *  - Row by row from bottom to top, left to right
     *  - Value 0 represents empty cells
     *  - Values 1 represents obstacle cells
     *  - Pad bitstream to full byte lengths with 0
     * @return Part 2 map descriptor string
     */
    public String generatePart2MapDescriptor() {
        //  todo: Determine if this is required
        sb = new StringBuilder();

        //  Traverse arena row by row from bottom to top, left to right
        for (int i = ARENA_ROW_COUNT - 1; i > -1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                //  Set value 0 if unexplored/unknown, 1 if otherwise
                if (cells[i][j].getType().equals(Cell.Type.EXPLORED)) {
                    if (cells[i][j].getType2().equals(EMPTY)) {
                        sb.append("0");
                    } else {
                        sb.append("1");
                    }
                }
            }
        }

        //  Pad string with zeroes to ensure full byte length of 8s
        for (int i = 0; i < (sb.length() % 8); i++) {
            sb.append("0");
        }

        convertBinaryToHexadecimalString(sb);

        return sb.toString();
    }

    /**
     * Converts binary values to hexadecimal strings
     * @param sb StringBuilder used to build final map descriptor string
     */
    private void convertBinaryToHexadecimalString(StringBuilder sb) {
        int index = 0;
        String tmp;

        //  Loop through map descriptor string
        while (index < sb.length()) {
            //  Get every four characters
            tmp = sb.substring(index, index + 4);
            //  Replace every four characters with its equivalent hexadecimal value
            sb.replace(index, index + 4, Integer.toString(Integer.parseInt(tmp, 2), 16));
            index += 1;
        }
    }

    /**
     * Re-renders robot position on the map based on information from Pi
     * @param x The new x location of the robot (top left)
     * @param y The new y location of the robot (top left)
     * @param rotation The rotation angle of the robot, with the origin facing up
     */
    public void moveRobot(int x, int y, float rotation) {
        if (robot == null) {
            //  If robot was previously not created, create a new robot
            robot = new Robot(x, y, ROBOT_HEIGHT, ROBOT_WIDTH, (int) rotation);
        }
        robot.setPosition(x * CELL_WIDTH, MAX_HEIGHT - y * CELL_HEIGHT);
        robot.setRotation(rotation);
        this.update();
    }

    /**
     * Converts an unpadded map descriptor (in StringBuilder) to binary string to update map,
     *  final string can be found in StringBuilder object passed in
     * @param sb StringBuilder that builds a string
     * @param paddedMapDescriptor Unpadded map descriptor (either p1 or p2)
     */
    public void convertPaddedMapDescriptorToBinary(StringBuilder sb, String paddedMapDescriptor) {

        //  Temporary values
        String character;
        Integer value;
        String binaryString;
        int k = 0;

        //  Iterate through unpadded map descriptor string
        for (int i = 0; i < paddedMapDescriptor.length(); i++) {
            //  Get each character as String
            character = String.valueOf(paddedMapDescriptor.charAt(i));
            //  Parse each character as a hexadecimal value
            value = Integer.parseInt(character, 16);
            //  Convert character to binary value (does not pad to four characters)
            binaryString = Integer.toBinaryString(value);
            //  Pad binary value with zeroes, if necessary
            if (binaryString.length() < 4) {
                for (int j = 0; j < (4 - binaryString.length()); j++) {
                    sb.append("0");
                }
            }
            //  Append padded binary value to final string
            sb.append(binaryString);
        }
    }

    /**
     * Given an unpadded map descriptor string P1, re-draw map
     * @param paddedMapDescriptorP1 Map descriptor string with padding removed
     */
    public void updateMapP1(String paddedMapDescriptorP1) {
        //  StringBuilder that builds the final string
        sb = new StringBuilder();

        //  Convert unpadded map descriptor P1 to binary string
        convertPaddedMapDescriptorToBinary(sb, paddedMapDescriptorP1);

        //  Remove P1 padding (first 2, last 2)
        sb = sb.delete(0, 2);
        sb = sb.delete(sb.length() - 1, sb.length() + 1);

        //  Helper values
        String character;
        int k = 0;

        //  Iterate through Arena and update accordingly
        for (int i = ARENA_ROW_COUNT - 1; i > - 1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                //  Get each binary digit
                character = String.valueOf(sb.charAt(k));
                //  Assign cell type based on binary value
                if (Integer.parseInt(character) == 0) {
                    cells[i][j].setType(Cell.Type.UNEXPLORED);
                } else {
                    cells[i][j].setType(Cell.Type.EXPLORED);
                }
                k++;
            }
        }
    }

    /**
     * Given a unpadded map descriptor string P2, re-draw map
     * @param paddedMapDescriptorP2 Map descriptor string with padding removed
     */
    public void updateMapP2(String paddedMapDescriptorP2) {
        //  StringBuilder that builds the final string
        sb = new StringBuilder();

        //  Convert unpaddedMapDescriptor in StringBuilder to binary string
        convertPaddedMapDescriptorToBinary(sb, paddedMapDescriptorP2);

        //  Helper values
        String character;
        int k = 0;

        //  Iterate through Arena and update accordingly
        for (int i = ARENA_ROW_COUNT - 1; i > -1; i--) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                if(i == 19 - imageY && j == imageX){
                    Log.d(MY_TAG, "updateMapP2: Setting IMAGE Color to map");
                    cells[i][j].setType4(IMAGE);
                }
                if (cells[i][j].getType() == Cell.Type.EXPLORED) {
                    //  Get each binary digit
                    character = String.valueOf(sb.charAt(k));
                    //  Assign cell type based on binary value
                    if (Integer.parseInt(character) == 0) {
                        cells[i][j].setType2(EMPTY);
                    } else {
                        cells[i][j].setType2(OBSTACLE);
                    }
                    k++;
                }
            }
        }
    }

    public void setImageOnMap(int y, int x, int id){
        Log.d("IMAGETEST", "setImageOnMap: x= "+x+" y= "+y);
        Drawable img = getResources().getDrawable(R.drawable.imagearrowwhite);
        cells[19-x][y].setType4(IMAGE);
        cells[19-x][y].setImageId(id);
        switch (id){
            case 1: img = getResources().getDrawable(R.drawable.imagearrowwhite);
                break;
            case 2: img = getResources().getDrawable(R.drawable.imagearrowred);
                break;
            case 3: img = getResources().getDrawable(R.drawable.imagearrowgreen);
                break;
            case 4: img = getResources().getDrawable(R.drawable.imagearrowblue);
                break;
            case 5: img = getResources().getDrawable(R.drawable.imagecircleyellow);
                break;
            case 6: img = getResources().getDrawable(R.drawable.image1blue);
                break;
            case 7: img = getResources().getDrawable(R.drawable.image2green);
                break;
            case 8: img = getResources().getDrawable(R.drawable.image3red);
                break;
            case 9: img = getResources().getDrawable(R.drawable.image4white);
                break;
            case 10: img = getResources().getDrawable(R.drawable.image5yellow);
                break;
            case 11: img = getResources().getDrawable(R.drawable.imageared);
                break;
            case 12: img = getResources().getDrawable(R.drawable.imagebgreen);
                break;
            case 13: img = getResources().getDrawable(R.drawable.imagecwhite);
                break;
            case 14: img = getResources().getDrawable(R.drawable.imagedblue);
                break;
            case 15: img = getResources().getDrawable(R.drawable.imageeyellow);
                break;
            default: break;

        }
        cells[19-x][y].setMyImg(img);
        Log.d("IMAGETEST", "setImageOnMap: pt2");
    }

    /**
     * Return the action being performed on the Arena
     * @return int indicating Arena action
     */
    public int getArenaAction() {
        return arenaAction;
    }

    /**
     * Sets the current action being performed on the Arena
     * @param arenaAction int indicating Arena action
     */
    public void setArenaAction(int arenaAction) {
        this.arenaAction = arenaAction;
    }

    /**
     * Sends robot starting position
     */
    private void sendRobotStartingPosition() {
        mFragment.sendStartingPosition(robot.getPosition(), CELL_WIDTH, CELL_HEIGHT, robot.getRotation());
    }

    /**
     * Get waypoint position
     */
    public String getWaypointInfo() {
        if (wp != null) {
            return (wp.getX() / CELL_WIDTH) + "," + (ARENA_ROW_COUNT - (wp.getY() / CELL_HEIGHT) - 1);
        } else {
            return "";
        }
    }

    /**
     * Sets way point position given (x, y) position
     * @param x x-axis position of the way point
     * @param y y-axis position of the way point
     */
    public void setWaypoint(int x, int y) {
        //  If there is an existing way point, remove it
        if (wp != null) {
            wp.setType3(Cell.Type3.NONE);
        }

        //  Add new way point
        wp = cells[ARENA_ROW_COUNT - y - 1][x];
        wp.setType3(Cell.Type3.WAYPOINT);
    }

    /**
     * Sets parent fragment of this SurfaceView, to obtain a reference for manipulation
     * @param parent ArenaFragment containing this SurfaceView
     */
    public void setFragment(ArenaFragment parent) {
        mFragment = parent;
    }

    /**
     * Clears arena of all objects
     */
    public void clearArena() {
        //  Clear robot
        robot = null;

        //  Clear arena objects
        for (int i = 0; i < ARENA_ROW_COUNT; i++) {
            for (int j = 0; j < ARENA_COLUMN_COUNT; j++) {
                cells[i][j].setType(Cell.Type.UNEXPLORED);
                cells[i][j].setType2(Cell.Type2.EMPTY);
                cells[i][j].setType4(Cell.Type4.EMPTY);
            }
        }
    }
    public void setRobotLoc(int x, int y){
        this.robotX = x;
        this.robotY = y;
    }

    public int[] getRobotLoc(){
        int[] arr = new int[3];
        arr[0] = robotX;
        arr[1] = robotY;
        Log.d(MY_TAG, "robot rotation: "+robotRotation);
        arr[2] = robotRotation;
        return arr;
    }

    public void setManualArena(int state){
        manualState = state;
    }
    public int getManualArena(){
        return manualState;
    }
}