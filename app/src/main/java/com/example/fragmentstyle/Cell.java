package com.example.fragmentstyle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

class Cell {

    private Paint paint;
    private Type type;
    private Type2 type2;
    private Type3 type3;
    private Type4 type4;
    private int x;
    private int y;
    private int height;
    private int width;
    private int row;
    private int col;

    //  Part 1 Map Descriptor types
    enum Type {
        UNKNOWN, UNEXPLORED, EXPLORED
    }

    //  Part 2 Map Descriptor types
    enum Type2 {
        EMPTY, OBSTACLE
    }

    //  Type 3
    enum Type3 {
        NONE, WAYPOINT
    }

    // Type 4
    enum Type4 {
        EMPTY, IMAGE
    }

    Cell(int x, int y, int height, int width, int rowIndex, int colIndex) {
        paint = new Paint();
        this.type = Type.UNKNOWN;
        this.type2 = Type2.EMPTY;
        this.type3 = Type3.NONE;
        this.type4 = Type4.EMPTY;
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.row = rowIndex;
        this.col = colIndex;
    }

    boolean isClicked(float x, float y) {
        return x < (this.x + width/2) && x > (this.x - width/2) && y < (this.y + height/2) && y > (this.y - height/2);
    }

    void draw(Canvas canvas) {
        Rect cellRect = new Rect(x - width / 2, y - height / 2, x + width / 2, y + height / 2);

        //  Draw based on Part 1 Map Descriptor string
        switch (type) {
            case UNKNOWN:
            case UNEXPLORED:
                paint.setColor(Color.LTGRAY);
                break;
            case EXPLORED:
                paint.setColor(Color.WHITE);
                break;
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(cellRect, paint);

        //  Draw based on Part 2 Map Descriptor String
        switch (type2) {
            case OBSTACLE:
                paint.setColor(Color.BLACK);
                break;
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(cellRect, paint);

        //  Draw waypoint, if available
        switch (type3) {
            case WAYPOINT:
                paint.setColor(Color.rgb(255, 193, 71));
                break;
        }
        // Image ID and color set
        switch (type4){
            case IMAGE:
                paint.setColor(Color.rgb(0, 0,255));
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(cellRect, paint);

        //  Color destination
        if (row == 0 || row == 1 || row == 2) {
           if (col == 12 || col == 13 || col == 14) {
               paint.setColor(Color.GREEN);
               paint.setStyle(Paint.Style.FILL);
               canvas.drawRect(cellRect, paint);
           }
        }

        //  Draw grid
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(cellRect, paint);
    }

    Type getType() {
        return type;
    }

    void setType(Type type) {
        this.type = type;
    }

    Type2 getType2() {
        return type2;
    }

    void setType2(Type2 type2) {
        this.type2 = type2;
    }

    void setType3(Type3 type3) {
        this.type3 = type3;
    }
    void setType4(Type4 type4){
        this.type4 = type4;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    int getRowIndex(){
        return row;
    }

    int getColumnIndex(){
        return col;
    }
}
