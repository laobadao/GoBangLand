package com.combanc.gobang.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.combanc.gobang.R;

/**
 * 负责游戏的显示，游戏的逻辑判断在Game.java中
 *
 * @author cuiqing
 * @modified by zhaojunjun 添加横纵坐标数字显示
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "GameView";
    private static final boolean DEBUG = true;

    // 定义SurfaceHolder对象
    SurfaceHolder mHolder = null;

    // 棋子画笔
    private Paint chessPaint = new Paint();
    // 棋盘画笔
    private Paint boardPaint = new Paint();
    // 横纵坐标数字画笔
    private Paint numberPaint = new Paint();

    private int boardColor = 0;
    private float boardWidth = 0.0f;
    private float anchorWidth = 0.0f;

    private int left = 0;
    private int top = 0;
    private int bottom = 0;

    // 清屏画笔
    Paint clear = new Paint();

    public int[][] mChessArray = null;

    Bitmap mBlack = null;
    Bitmap mBlackNew = null;
    Bitmap mWhite = null;
    Bitmap mWhiteNew = null;

    int mChessboardWidth = 0;
    int mChessboardHeight = 0;
    int mChessSize = 0;

    Context mContext = null;

    private Game mGame;

    private Coordinate focus;
    private boolean isDrawFocus;
    private Bitmap bFocus;
    private OnDownActionListener mDown = null;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        boardColor = Color.BLACK;
        boardWidth = getResources().getDimensionPixelSize(R.dimen.boardWidth);
        anchorWidth = getResources().getDimensionPixelSize(R.dimen.anchorWidth);
        focus = new Coordinate();
        init();
    }

    private void init() {
        mHolder = this.getHolder();
        mHolder.addCallback(this);
        // 设置透明
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        chessPaint.setAntiAlias(true);
        boardPaint.setStrokeWidth(boardWidth);
        boardPaint.setColor(boardColor);
//        numberPaint.setTextAlign(Paint.Align.RIGHT);
        numberPaint.setTypeface(Typeface.DEFAULT_BOLD);
        numberPaint.setColor(Color.BLACK);
        numberPaint.setTextSize(40);

        clear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        setFocusable(true);
    }

    /**
     * 设置游戏
     *
     * @param game
     */
    public void setGame(Game game) {
        mGame = game;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置高度与宽度一样
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        if (mGame != null) {
            if (width % mGame.getWidth() == 0) {
                float scale = ((float) mGame.getHeight()) / mGame.getWidth();
                int height = (int) (width * scale);
                setMeasuredDimension(width, height);
            } else {
                width = width / mGame.getWidth() * mGame.getWidth();
                float scale = ((float) mGame.getHeight()) / mGame.getWidth();
                int height = (int) (width * scale);
                setMeasuredDimension(width, height);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (DEBUG)
            Log.d(TAG, "left=" + left + "  top=" + top + " right=" + right + " bottom=" + bottom);
        if (mGame != null) {
            this.left = left;
            this.bottom = bottom;
            this.top = top;
            // mGame.getWidth() 获得是设定的棋盘的行数，列数可以修改
            mChessboardWidth = mGame.getWidth();
            mChessboardHeight = mGame.getHeight();
            mChessSize = (right - left) / mChessboardWidth;
            Log.d(TAG, "mChessSize=" + mChessSize + " mChessboardWidth="
                    + mChessboardWidth + " mChessboardHeight"
                    + mChessboardHeight);
        }
    }

    /**
     * 绘制游戏界面
     */
    public void drawGame() {
        Canvas canvas = mHolder.lockCanvas();
        if (mHolder == null || canvas == null) {
            return;
        }
        // 清屏  ：是否可以不用清屏，用双缓冲技术实现
        canvas.drawPaint(clear);
        drawChessBoard(canvas);
        drawNumber(canvas);
        drawChess(canvas);
        drawFocus(canvas);
        mHolder.unlockCanvasAndPost(canvas);
    }


    /**
     * 增加一个棋子
     *
     * @param x 横坐标
     * @param y 纵坐标
     */
    public void addChess(int x, int y) {
        if (mGame == null) {
            return;
        }

        if (mGame.canAddChess(x, y)) {
            // 添加监听 回调 返回坐标
            if (mDown != null) {
                mDown.OnDown(x, mChessboardWidth - 1 - y);
            }
            mGame.addChess(x, y);
        }
        drawGame();
    }

    /**
     * 输入数对增加一个棋子
     *
     * @param x 横坐标
     * @param y 纵坐标
     */
    public boolean addNumberChess(int x, int y) {
        if (mGame == null) {
            return false;
        }

        if (mGame.canAddChess(x, mChessboardWidth-1-y)) {
            // 添加监听 回调 返回坐标
            if (mDown != null) {
                mDown.OnDown(x,  y);
            }
            mGame.addChess(x, mChessboardWidth-1-y);
            drawGame();
            return true;
        } else return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                focus.x = (int) (x / mChessSize);
                focus.y = (int) (y / mChessSize);
                isDrawFocus = true;
                drawGame();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                isDrawFocus = false;
                int newx = (int) (x / mChessSize);
                int newy = (int) (y / mChessSize);
                if (canAdd(newx, newy, focus)) {
                    addChess(focus.x, focus.y);
                } else {
                    drawGame();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 判断是否取消此次下子
     *
     * @param x x位置
     * @param y y位置
     * @return
     */
    private boolean canAdd(float x, float y, Coordinate focus) {
        return x < focus.x + 3 && x > focus.x - 3
                && y < focus.y + 3 && y > focus.y - 3;
    }

    /**
     * 创建棋子
     *
     * @param width  VIEW的宽度
     * @param height VIEW的高度
     * @param type   类型——白子或黑子
     * @return Bitmap
     */
    private Bitmap createChess(int width, int height, int type) {
        int tileSize = width / 15;
        Bitmap bitmap = Bitmap.createBitmap(tileSize, tileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable d = null;
        if (type == 0) {
            d = getResources().getDrawable(R.drawable.black);
        } else if (type == 1) {
            d = getResources().getDrawable(R.drawable.white);
        } else if (type == 2) {
            d = getResources().getDrawable(R.drawable.black_new);
        } else if (type == 3) {
            d = getResources().getDrawable(R.drawable.white_new);
        } else if (type == 4) {
            d = getResources().getDrawable(R.drawable.focus);
        }
        d.setBounds(0, 0, tileSize, tileSize);
        d.draw(canvas);
        return bitmap;
    }

    // 画棋盘背景
    private void drawChessBoard() {
        Canvas canvas = mHolder.lockCanvas();
        if (mHolder == null || canvas == null) {
            return;
        }
        drawChessBoard(canvas);
        drawNumber(canvas);
        mHolder.unlockCanvasAndPost(canvas);
    }

    // 画棋盘背景
    private void drawChessBoard(Canvas canvas) {
        // 绘制锚点(中心点)
        int startX = mChessSize / 2;
        int startY = mChessSize / 2;
        int endX = startX + (mChessSize * (mChessboardWidth - 1));
        int endY = startY + (mChessSize * (mChessboardHeight - 1));
        // draw 竖直线
        for (int i = 0; i < mChessboardWidth; ++i) {
            canvas.drawLine(startX + (i * mChessSize), startY, startX + (i * mChessSize), endY, boardPaint);
        }
        // draw 水平线
        for (int i = 0; i < mChessboardHeight; ++i) {
            canvas.drawLine(startX, startY + (i * mChessSize), endX, startY + (i * mChessSize), boardPaint);
        }
        // 绘制锚点(中心点)
        int circleX = startX + mChessSize * (mChessboardWidth / 2);
        int circleY = startY + mChessSize * (mChessboardHeight / 2);
        canvas.drawCircle(circleX, circleY, anchorWidth, boardPaint);
    }

    /**
     * 绘制横纵坐标数字显示
     */
    public void drawNumber(Canvas canvas) {
        // 画纵轴坐标
        for (int i = 0; i < mChessboardWidth; i++) {
            canvas.drawText(Integer.toString(i), left+10, bottom - (i * mChessSize) - mChessSize / 2 - top+left, numberPaint);
        }

        // 画横轴坐标
        for (int i = 1; i < mChessboardWidth; i++) {
            canvas.drawText(Integer.toString(i), left + (i * mChessSize)+20, bottom - 8/*- mChessSize /4*/, numberPaint);
        }


    }

    // 画棋子
    private void drawChess(Canvas canvas) {
        int[][] chessMap = mGame.getChessMap();
        for (int x = 0; x < chessMap.length; ++x) {
            for (int y = 0; y < chessMap[0].length; ++y) {
                int type = chessMap[x][y];
                if (type == Game.BLACK) {
                    canvas.drawBitmap(mBlack, x * mChessSize+left, y * mChessSize+left, chessPaint);
                } else if (type == Game.WHITE) {
                    canvas.drawBitmap(mWhite, x * mChessSize+left, y * mChessSize+left, chessPaint);
                }
            }
        }
        // 画最新下的一个棋子
        if (mGame.getActions() != null && mGame.getActions().size() > 0) {
            Coordinate last = mGame.getActions().getLast();
            int lastType = chessMap[last.x][last.y];
            if (lastType == Game.BLACK) {
                canvas.drawBitmap(mBlackNew, last.x * mChessSize+left, last.y * mChessSize+left, chessPaint);
            } else if (lastType == Game.WHITE) {
                canvas.drawBitmap(mWhiteNew, last.x * mChessSize+left, last.y * mChessSize+left, chessPaint);
            }
        }
    }

    /**
     * 画当前框
     *
     * @param canvas
     */
    private void drawFocus(Canvas canvas) {
        if (isDrawFocus) {

            canvas.drawBitmap(bFocus, focus.x * mChessSize+left, focus.y * mChessSize+left, chessPaint);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mBlack != null) {
            mBlack.recycle();
        }
        if (mWhite != null) {
            mWhite.recycle();
        }
        mWhite = createChess(width, height, 1);
        mBlack = createChess(width, height, 0);
        mBlackNew = createChess(width, height, 2);
        mWhiteNew = createChess(width, height, 3);
        bFocus = createChess(width, height, 4);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 初始化棋盘
        drawChessBoard();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

    }

    // 为每个接口设置监听器
    public void setOnDownActionListener(OnDownActionListener down) {
        mDown = down;
    }

    // 定义三个接口
    public interface OnDownActionListener {
        public void OnDown(int x, int y);
    }

}
