package edu.temple.sillygame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GameView extends View {

    int canvasWidth, canvasHeight;
    Paint myColor;

    public GameObject enemy, otherEnemy;

    int myX, myY, mySize, myDx, myDy;

    Rectangle ball;

    Context context;

    Queue<GameObject> toBeDrawn = new LinkedList<>();

    interface CollisionListener {
        void collidedWithEnemy();
        void collidedWithBall();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        myColor = new Paint();
        myColor.setColor(Color.BLUE);

        enemy = new GameObject(0, 0, 80, Color.RED, 20, null) {

            int xDirection; // 0 - left, 1 - right
            int yDirection; // 0 - up, 1 - down

            @Override
            public void performAction(Object data) {

                final int movePixels = 5;

                if (collidesWith(otherEnemy, 5) == 0 && collidesWithBall() == 0) {
                    if (xDirection == 0) {
                        if ((x - movePixels) > 0) {
                            x -= movePixels;
                        } else {
                            xDirection = 1;
                        }

                    } else {
                        if (((x + movePixels) + size < canvasWidth)) {
                            x += (movePixels);
                        } else {
                            xDirection = 0;
                        }
                    }

                    if (yDirection == 0) {
                        if ((y - movePixels) > 0) {
                            y -= movePixels;
                        } else {
                            yDirection = 1;
                        }
                    } else {
                        if ((y + movePixels) + size < canvasHeight) {
                            y += movePixels;
                        } else {
                            yDirection = 0;
                        }
                    }
                } else {
                    xDirection = getDx();
                    yDirection = getDy();
                }


                addToBeDrawn(this);

            }
        };

        enemy.start();

        otherEnemy = new GameObject(300, 670, 150, Color.YELLOW, 10, null) {

            int xDirection; // 0 - left, 1 - right
            int yDirection; // 0 - up, 1 - down

            @Override
            public void performAction(Object data) {

                final int movePixels = 5;

                if (collidesWith(enemy, 5) == 0 && collidesWithBall() == 0) {
                    if (xDirection == 0) {
                        if ((x - movePixels) > 0) {
                            x -= movePixels;
                        } else {
                            xDirection = 1;
                        }
                    } else {
                        if (((x + movePixels) + size < canvasWidth)) {
                            x += movePixels;
                        } else {
                            xDirection = 0;
                        }
                    }

                    if (yDirection == 0) {
                        if ((y - movePixels) > 0) {
                            y -= movePixels;
                        } else {
                            yDirection = 1;
                        }
                    } else {
                        if ((y + movePixels) + size < canvasHeight) {
                            y += movePixels;
                        } else {
                            yDirection = 0;
                        }
                    }
                } else {
                    xDirection = getDx();
                    yDirection = getDy();
                }

                addToBeDrawn(this);

            }

        };

        otherEnemy.start();


        myX = 0;
        myY = 200;
        mySize = 100;

        ball = new Rectangle(myX, myY, mySize, mySize);
    }

    float[] values = new float[3];

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        GameObject object;

        while ((object = getObjectToBeDrawn()) != null)
            drawObject(canvas, object);

        values = ((Rotatable) context).getRotationInfo();


        drawSelf(canvas, values);

    }

    private synchronized void addToBeDrawn(GameObject gameObject) {
        toBeDrawn.add(gameObject);
    }

    private synchronized GameObject getObjectToBeDrawn() {
        return toBeDrawn.poll();
    }

    private void drawSelf(Canvas canvas, float[] values) {

        int xDirection;
        int yDiirection;

        int multiplier = 50;

        myDy = (int) (Math.abs(values[1]) * multiplier);
        myDx = (int) (Math.abs(values[2]) * multiplier);

        if (values[1] < 0) {
            if (myY + mySize - (int) (Math.abs(values[1]) * multiplier) > 0) {
                myY = myY + (int) (Math.abs(values[1]) * multiplier);
            }
        } else {
            if (myY - (int) (Math.abs(values[1]) * multiplier) + mySize < canvasHeight) {
                myY = myY - (int) (Math.abs(values[1]) * multiplier);
            }
        }

        if (values[2] < 0) {
            if (myX - (int) (Math.abs(values[2]) * multiplier) - mySize > 0) {
                myX = myX - (int) (Math.abs(values[2]) * multiplier);
            }
        } else {
            if (myX + (int) (Math.abs(values[2]) * multiplier) + mySize < canvasWidth) {
                myX = myX + (int) (Math.abs(values[2]) * multiplier);
            }
        }

        canvas.drawCircle(myX, myY, mySize, myColor);
        ball.x = myX - mySize;
        ball.y = myY - mySize;
    }


    private void drawObject(Canvas canvas, GameObject object) {
        canvas.drawRect(object.x
                , object.y
                , object.x + object.size
                , object.y + object.size
                , object.color);
    }


    abstract class GameObject {
        int x;
        int y;
        int size;
        int dx;
        int dy;

        Paint color;
        private ObjectThread thread;
        private List<CollisionListener> listeners = new ArrayList<CollisionListener>();

        GameObject(int x, int y, int size, int color, int delay, Object data) {
            this.x = x;
            this.y = y;
            this.size = size;
            Paint p = new Paint();
            p.setColor(color);
            this.color = p;
            this.thread = new ObjectThread(this, delay, data);
        }


        public int getDx() {
            return dx;
        }

        public void setDx(int dx) {
            this.dx = dx;
        }

        public int getDy() {
            return dy;
        }

        public void setDy(int dy) {
            this.dy = dy;
        }

        public int collidesWithBall() {
            Rectangle me = new Rectangle(this.x, this.y, this.size, this.size);

            Rectangle intersect;

            if (me.intersects(ball)) {
                intersect = me.intersection(ball);

                boolean vertical = false;
                boolean horizontal = false;
                boolean isLeft = false;
                boolean isTop = false;

                // Left side...
                if (intersect.x == me.x) {
                    horizontal = true;
                    isLeft = true;
                    // Right side
                } else if (intersect.x + intersect.width == me.x + me.width) {
                    horizontal = true;
                }
                // Top
                if (intersect.y == me.y) {
                    vertical = true;
                    isTop = true;
                    // Bottom
                } else if (intersect.y + intersect.height == me.y + me.height) {
                    vertical = true;
                }

                if (horizontal) {
                    if (isLeft) {
                        x += 50;
                        Log.wtf("Ball Collision", "left");
                        setDx(1);
                    } else {
                        x -= 50;
                        setDx(0);
                        Log.wtf("Ball Collision", "right");
                    }
                }

                if (vertical) {
                    if (isTop) {
                        y += 50;
                        Log.wtf("Ball Collision", "top");
                        setDy(1);
                    } else {
                        y -= 50;
                        Log.wtf("Ball Collision", "bottom");
                        setDy(0);
                    }
                }
                return 1;
            }
            return 0;
        }

        public int collidesWith(GameObject object, int move) {
            Rectangle me = new Rectangle(this.x, this.y, this.size, this.size);
            Rectangle them = new Rectangle(object.x, object.y, object.size, object.size);
            Rectangle intersect;

            if (me.intersects(them)) {
                intersect = me.intersection(them);

                boolean vertical = false;
                boolean horizontal = false;
                boolean isLeft = false;
                boolean isTop = false;

                // Left side...
                if (intersect.x == me.x) {
                    horizontal = true;
                    isLeft = true;
                    // Right side
                } else if (intersect.x + intersect.width == me.x + me.width) {
                    horizontal = true;
                }
                // Top
                if (intersect.y == me.y) {
                    vertical = true;
                    isTop = true;
                    // Bottom
                } else if (intersect.y + intersect.height == me.y + me.height) {
                    vertical = true;
                }
/*
                // Technically, we can really only collide with a single edge...more or less
                if (horizontal && vertical) {
                    // Basically, we try and give precedence to the longer edge...
                    if (intersect.width > intersect.height) {
                        horizontal = false;
                    } else {
                        vertical = false;
                    }
                }
*/
                if (horizontal) {
                    if (isLeft) {
                        x += 20;
                        Log.wtf("Collision", "left");
                        setDx(1);
                    } else {
                        x -= 20;
                        setDx(0);
                        Log.wtf("Collision", "right");
                    }
                }

                if (vertical) {
                    if (isTop) {
                        y += 20;
                        Log.wtf("Collision", "top");
                        setDy(1);
                    } else {
                        y -= 20;
                        Log.wtf("Collision", "bottom");
                        setDy(0);
                    }
                }

                return 1;
            }

            return 0;
        }

        public void start() {
            thread.start();
        }

        public abstract void performAction(Object data);

        class ObjectThread extends Thread {

            int delay;
            GameObject object;
            Object data;
            boolean running = true;

            ObjectThread(GameObject object, int delay, Object data) {
                this.object = object;
                this.delay = delay;
                this.data = data;

            }

            public void run() {
                while (running) {
                    try {
                        Thread.sleep(delay);
                        object.performAction(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    interface Rotatable {
        float[] getRotationInfo();
    }

    class Rectangle {
        int x;
        int y;
        int width;
        int height;

        public Rectangle(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void setX(int x) {
            this.x = x;
        }


        public void setY(int y) {
            this.y = y;
        }

        public Rectangle intersection(Rectangle r) {
            int tx1 = this.x;
            int ty1 = this.y;
            int rx1 = r.x;
            int ry1 = r.y;
            long tx2 = tx1;
            tx2 += this.width;
            long ty2 = ty1;
            ty2 += this.height;
            long rx2 = rx1;
            rx2 += r.width;
            long ry2 = ry1;
            ry2 += r.height;
            if (tx1 < rx1) tx1 = rx1;
            if (ty1 < ry1) ty1 = ry1;
            if (tx2 > rx2) tx2 = rx2;
            if (ty2 > ry2) ty2 = ry2;
            tx2 -= tx1;
            ty2 -= ty1;
            // tx2,ty2 will never overflow (they will never be
            // larger than the smallest of the two source w,h)
            // they might underflow, though...
            if (tx2 < Integer.MIN_VALUE) tx2 = Integer.MIN_VALUE;
            if (ty2 < Integer.MIN_VALUE) ty2 = Integer.MIN_VALUE;
            return new Rectangle(tx1, ty1, (int) tx2, (int) ty2);
        }

        public boolean intersects(Rectangle r) {
            int tw = this.width;
            int th = this.height;
            int rw = r.width;
            int rh = r.height;
            if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
                return false;
            }
            int tx = this.x;
            int ty = this.y;
            int rx = r.x;
            int ry = r.y;
            rw += rx;
            rh += ry;
            tw += tx;
            th += ty;
            //      overflow || intersect
            return ((rw < rx || rw > tx) &&
                    (rh < ry || rh > ty) &&
                    (tw < tx || tw > rx) &&
                    (th < ty || th > ry));

        }
    }



}
