package com.yangqi.wuziqi;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangQI on 2017/3/29.
 * 自定义五子棋控件
 */
public class ChessView extends View {

    private static final String TAG = "ChessView";
    private boolean isBlackPlay = true;
    private boolean isLocked = false;
    private Paint mBoardPaint;
    private Paint mChessPaint;
    private Paint mBgPaint;
    private Chess[][] mChessArray;
    private List<Point> mEveryPlay;

    public ChessView(Context context) {
        this(context, null);
    }

    public ChessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEveryPlay();
        initChess();
        initBoardPaint();
        initChessPaint();
        initBgPaint();
    }

    private void initEveryPlay() {
        // 初始化 List 大小，此方法不影响 list.size() 返回值
        mEveryPlay = new ArrayList<>(225);
    }

    private void initChess() {
        mChessArray = new Chess[15][15];
        for (int i = 0; i < mChessArray.length; i++) {
            for (int j = 0; j < mChessArray[i].length; j++) {
                mChessArray[i][j] = new Chess();
            }
        }
    }

    private void initChessPaint() {
        mChessPaint = new Paint();
        mChessPaint.setColor(android.graphics.Color.WHITE);
        mChessPaint.setAntiAlias(true);
    }

    private void initBoardPaint() {
        mBoardPaint = new Paint();
        mBoardPaint.setColor(android.graphics.Color.BLACK);
        mBoardPaint.setStrokeWidth(2);
    }

    private void initBgPaint() {
        mBgPaint = new Paint();
        mBgPaint.setColor(android.graphics.Color.GRAY);
        mBgPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int min = widthSize < heightSize ? widthSize : heightSize;
        min = min / 16 * 16;

        setMeasuredDimension(min, min);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int avg = height / 16;

        canvas.drawRect(0, 0, width, height, mBgPaint);
        for (int i = 1; i < 16; i++) {
            // 画竖线
            canvas.drawLine(avg * i, avg, avg * i, height - avg, mBoardPaint);
            // 画横线
            canvas.drawLine(avg, avg * i, width - avg, avg * i, mBoardPaint);
        }
        for (int i = 1; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                switch (mChessArray[i - 1][j - 1].getColor()) {
                    case BLACK:
                        mChessPaint.setColor(android.graphics.Color.BLACK);
                        break;
                    case WHITE:
                        mChessPaint.setColor(android.graphics.Color.WHITE);
                        break;
                    case NONE:
                        continue;
                }
                canvas.drawCircle(avg * i, avg * j, avg / 2 - 0.5f, mChessPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 如果棋盘被锁定（即胜负已分，返回查看棋局的时候）
                // 此时只允许查看，不允许落子了
                if (isLocked) {
                    return true;
                }
                float x = event.getX();
                float y = event.getY();
                // 以点击的位置为中心，新建一个小矩形
                Rect rect = getLittleRect(x, y);
                // 获得上述矩形包含的棋盘上的点
                Point point = getContainPoint(rect);
                if (point != null) {
                    // 若点不为空，则刷新对应位置棋子的属性
                    setChessState(point);
                    // 记录下每步操作，方便悔棋操作
                    mEveryPlay.add(point);
                    if (gameIsOver(point.x, point.y)) {
                        // 游戏结束弹窗提示
                        showDialog();
                    }
                    // 更改游戏玩家
                    isBlackPlay = !isBlackPlay;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 游戏结束，显示对话框
     */
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("游戏结束");
        if (isBlackPlay) {
            builder.setMessage("黑方获胜！！！");
        } else {
            builder.setMessage("白方获胜！！！");
        }
        builder.setCancelable(false);
        builder.setPositiveButton("重新开始", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetChessBoard();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("返回查看", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isLocked = true;
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 判断游戏是否结束，游戏结束标志：当前落子位置与其他同色棋子连成 5 个
     *
     * @param x 落子位置 x 坐标
     * @param y 落子位置 y 坐标
     * @return 若连成 5 个，游戏结束，返回 true，负责返回 false
     */
    private boolean gameIsOver(int x, int y) {
        Chess.Color color = mChessArray[x][y].getColor();
        return isOverA(x, y, color) || isOverB(x, y, color) || isOverC(x, y, color) || isOverD(x, y, color);
    }

    /**
     * 判断坐标竖直方向是否连成五子
     *
     * @param x     坐标 x
     * @param y     坐标 y
     * @param color 棋子颜色
     * @return 连成五子返回 true，反之返回 false
     */
    private boolean isOverA(int x, int y, Chess.Color color) {
        int amount = 0;
        for (int i = y; i >= 0; i--) {
            if (mChessArray[x][i].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        for (int i = y; i < mChessArray[x].length; i++) {
            if (mChessArray[x][i].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        // 循环执行完成后，当前落子位置算了两次，故条件应是大于 5
        return amount > 5;
    }

    /**
     * 判断坐标水平方向是否连成五子
     *
     * @param x     坐标 x
     * @param y     坐标 y
     * @param color 棋子颜色
     * @return 连成五子返回 true，反之返回 false
     */
    private boolean isOverB(int x, int y, Chess.Color color) {
        int amount = 0;
        for (int i = x; i >= 0; i--) {
            if (mChessArray[i][y].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        for (int i = x; i < mChessArray.length; i++) {
            if (mChessArray[i][y].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        // 循环执行完成后，当前落子位置算了两次，故条件应是大于 5
        return amount > 5;
    }

    /**
     * 判断坐标左上至右下方向是否连成五子
     *
     * @param x     坐标 x
     * @param y     坐标 y
     * @param color 棋子颜色
     * @return 连成五子返回 true，反之返回 false
     */
    private boolean isOverC(int x, int y, Chess.Color color) {
        int amount = 0;
        for (int i = x, j = y; i >= 0 && j >= 0; i--, j--) {
            if (mChessArray[i][j].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        for (int i = x, j = y; i < mChessArray.length && j < mChessArray[i].length; i++, j++) {
            if (mChessArray[i][j].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        // 循环执行完成后，当前落子位置算了两次，故条件应是大于 5
        return amount > 5;
    }

    /**
     * 判断坐标左下至右上方向是否连成五子
     *
     * @param x     坐标 x
     * @param y     坐标 y
     * @param color 棋子颜色
     * @return 连成五子返回 true，反之返回 false
     */
    private boolean isOverD(int x, int y, Chess.Color color) {
        int amount = 0;
        for (int i = x, j = y; i < mChessArray.length && j >= 0; i++, j--) {
            if (mChessArray[i][j].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        for (int i = x, j = y; i >= 0 && j < mChessArray[i].length; i--, j++) {
            if (mChessArray[i][j].getColor() == color) {
                amount++;
            } else {
                break;
            }
        }
        // 循环执行完成后，当前落子位置算了两次，故条件应是大于 5
        return amount > 5;
    }

    /**
     * 重新设定用户所点位置的棋子状态
     *
     * @param point 棋子的位置
     */
    private void setChessState(Point point) {
        if (isBlackPlay) {
            mChessArray[point.x][point.y].setColor(Chess.Color.BLACK);
        } else {
            mChessArray[point.x][point.y].setColor(Chess.Color.WHITE);
        }
        invalidate();
    }

    /**
     * 以传入点为中心，获得一个矩形
     *
     * @param x 传入点 x 坐标
     * @param y 传入点 y 坐标
     * @return 所得矩形
     */
    private Rect getLittleRect(float x, float y) {
        int side = getMeasuredHeight() / 16;
        int left = (int) (x - side / 2);
        int top = (int) (y - side / 2);
        int right = (int) (x + side / 2);
        int bottom = (int) (y + side / 2);
        return new Rect(left, top, right, bottom);
    }

    /**
     * 获取包含在 rect 中并且是能够下棋的位置的点
     *
     * @param rect 矩形
     * @return 返回包含的点，若没有包含任何点或者包含点已有棋子返回 null
     */
    private Point getContainPoint(Rect rect) {
        int avg = getMeasuredHeight() / 16;
        for (int i = 1; i < 16; i++) {
            for (int j = 1; j < 16; j++) {
                if (rect.contains(avg * i, avg * j)) {
                    Point point = new Point(i - 1, j - 1);
                    // 包含点没有棋子才返回 point
                    if (mChessArray[point.x][point.y].getColor() == Chess.Color.NONE) {
                        return point;
                    }
                    break;
                }
            }
        }
        return null;
    }

    /**
     * 悔棋，实现思路为：记录每一步走棋的坐标，若点击了悔棋，
     * 则拿出最后记录的坐标，对 mChessArray 里面对应坐标的
     * 棋子进行处理（设置颜色为 NONE），并移除集合里面最后
     * 一个元素
     */
    public void retract() {
        if (mEveryPlay.isEmpty()) {
            return;
        }
        Point point = mEveryPlay.get(mEveryPlay.size() - 1);
        mChessArray[point.x][point.y].setColor(Chess.Color.NONE);
        mEveryPlay.remove(mEveryPlay.size() - 1);
        isLocked = false;
        isBlackPlay = !isBlackPlay;
        invalidate();
    }

    /**
     * 重置棋盘
     */
    public void resetChessBoard() {
        for (Chess[] chessRow : mChessArray) {
            for (Chess chess : chessRow) {
                chess.setColor(Chess.Color.NONE);
            }
        }
        mEveryPlay.clear();
        isBlackPlay = true;
        isLocked = false;
        invalidate();
    }
}
