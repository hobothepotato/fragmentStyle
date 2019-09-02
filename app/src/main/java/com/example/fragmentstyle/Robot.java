package com.example.fragmentstyle;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

public class Robot {

    //  Robot parameters
    private Point position;
    private int height;
    private int width;
    private float rotation;

    //  Robot design
    private Paint boundsPaint = new Paint();
    private Rect bounds;
    private Paint trianglePaint = new Paint();
    private Path triangle;

    public Robot(int x, int y, int height, int width) {

        //  Set robot parameters
        this.position = new Point(x, y);
        this.height = height;
        this.width = width;
        this.rotation = 0;

        //  Set Paint variables
        boundsPaint.setColor(Color.rgb(13, 71, 161));
        trianglePaint.setColor(Color.rgb(255, 255, 0));

        //  Set Robot dimensions and shapes
        this.position = new Point(x, y);
        this.bounds = new Rect(position.x - (width / 3), position.y - (height / 3) * 2, position.x + (width / 3) * 2, position.y + (height / 3));

        //  Draw direction rectangle
        drawTriangle();
    }

    public Robot(int x, int y, int height, int width, int rotation) {
        this(x, y, height, width);
        this.rotation = rotation;
    }

    void draw(Canvas canvas) {
        //  Save state of canvas
        canvas.save();

        //  Rotate canvas according to Robot's rotation (middle of robot)
        canvas.rotate(this.rotation, position.x + (width / 6), position.y - (height / 6));

        //  Draw rotated robot
        canvas.drawRect(bounds, boundsPaint);
        canvas.drawPath(triangle, trianglePaint);

        //  Restore state of canvas, but does not affect robot
        canvas.restore();
    }

    /**
     * Draws triangle on robot, indicating it's direction
     */
    private void drawTriangle() {
        triangle = new Path();
        //  Move to top middle
        triangle.moveTo((bounds.left + width / 2), (bounds.top + height / 6));
        //  Draw line to bottom left
        triangle.lineTo((bounds.left + width / 6), (bounds.bottom - height / 6));
        //  Draw line to bottom right
        triangle.lineTo((bounds.right - width / 6), (bounds.bottom - height / 6));
        //  Draw line to top middle
        triangle.lineTo((bounds.left + width / 2), (bounds.top + height / 6));
        //  Complete
        triangle.close();
    }

    /**
     * Update robot information
     */
    void update() {
        bounds.set(position.x - (width / 3), position.y - (height / 3) * 2, position.x + (width / 3) * 2, position.y + (height / 3));
        drawTriangle();
        setRotation(rotation);
    }

    Point getPosition() {
        return position;
    }

    void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }

    float getRotation() {
        return rotation;
    }

    void setRotation(float rotation) {
        this.rotation = rotation;
    }
}
