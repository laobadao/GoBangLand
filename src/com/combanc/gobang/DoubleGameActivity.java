package com.combanc.gobang;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.combanc.gobang.game.Game;
import com.combanc.gobang.game.GameConstants;
import com.combanc.gobang.game.GameView;
import com.combanc.gobang.game.Player;

/**
 * 双人对战
 *
 * @author zhaojun (Email:laobadaozjj@gmail.com)
 *         <p>
 *         2017/11/3 14:34
 */

public class DoubleGameActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.double_game_activity);
        initViews();
        initGame();
    }

    private static final String TAG = "DoubleGameActivity";

    GameView mGameView = null;

    Game mGame;
    Player black;
    Player white;

    private TextView mBlackWin;
    private TextView mWhiteWin;
    private ScrollView mBlackSv;
    private ScrollView mWhiteSv;
    private LinearLayout mWhiteLl;
    private LinearLayout mBlackLl;
    private LinearLayout mInfoViewWhite;
    private LinearLayout mInfoViewBlack;
    private ImageView mBlackActive;
    private ImageView mWhiteActive;
    private Button mNum0;
    private Button mNum1;
    private Button mNum2;
    private Button mNum3;
    private Button mNum4;
    private Button mNum5;
    private Button mNum6;
    private Button mNum7;
    private Button mNum8;
    private Button mNum9;

    private TextView mXNumTv;
    private TextView mYNumTv;


    // Control Button
    private Button restart;
//    private Button rollback;


    private Handler mRefreshHandler = new Handler() {

        public void handleMessage(Message msg) {
            Log.d(TAG, "refresh action=" + msg.what);
            switch (msg.what) {
                case GameConstants.GAME_OVER:
                    if (msg.arg1 == Game.BLACK) {
                        showWinDialog("黑方胜");
                        black.win();
                    } else if (msg.arg1 == Game.WHITE) {
                        showWinDialog("白方胜");
                        white.win();
                    }
                    updateScore(black, white);
                    break;
                case GameConstants.ADD_CHESS:
                    updateActive(mGame);
                    break;
                default:
                    break;
            }
        }
    };

    private void initViews() {
        mGameView = (GameView) findViewById(R.id.game_view);
        mBlackWin = (TextView) findViewById(R.id.black_win);
        mBlackActive = (ImageView) findViewById(R.id.black_active);
        mWhiteWin = (TextView) findViewById(R.id.white_win);
        mWhiteActive = (ImageView) findViewById(R.id.white_active);
        restart = (Button) findViewById(R.id.restart);
//        rollback = (Button) findViewById(R.id.rollback);
        mWhiteLl = (LinearLayout) findViewById(R.id.white_ll);
        mBlackLl = (LinearLayout) findViewById(R.id.black_ll);
        mBlackSv = (ScrollView) findViewById(R.id.black_sv);
        mWhiteSv = (ScrollView) findViewById(R.id.white_sv);
        mNum0 = (Button) findViewById(R.id.num_0);
        mNum1 = (Button) findViewById(R.id.num_1);
        mNum2 = (Button) findViewById(R.id.num_2);
        mNum3 = (Button) findViewById(R.id.num_3);
        mNum4 = (Button) findViewById(R.id.num_4);
        mNum5 = (Button) findViewById(R.id.num_5);
        mNum6 = (Button) findViewById(R.id.num_6);
        mNum7 = (Button) findViewById(R.id.num_7);
        mNum8 = (Button) findViewById(R.id.num_8);
        mNum9 = (Button) findViewById(R.id.num_9);

        mXNumTv = (TextView) findViewById(R.id.x_num_tv);
        mYNumTv = (TextView) findViewById(R.id.y_num_tv);

        mNum0.setOnClickListener(this);
        mNum1.setOnClickListener(this);
        mNum2.setOnClickListener(this);
        mNum3.setOnClickListener(this);
        mNum4.setOnClickListener(this);
        mNum5.setOnClickListener(this);
        mNum6.setOnClickListener(this);
        mNum7.setOnClickListener(this);
        mNum8.setOnClickListener(this);
        mNum9.setOnClickListener(this);

        restart.setOnClickListener(this);
//        rollback.setOnClickListener(this);
    }

    private void initGame() {
        black = new Player(Game.BLACK);
        white = new Player(Game.WHITE);
        mGame = new Game(mRefreshHandler, black, white);
        mGame.setMode(GameConstants.MODE_FIGHT);
        mGameView.setGame(mGame);
        updateActive(mGame);
        updateScore(black, white);
        mGameView.setOnDownActionListener(new GameView.OnDownActionListener() {

            @Override
            public void OnDown(int x, int y) {
                // 回调显示横纵坐标
                if (mGame.getActive() == Game.BLACK)
                    addBlackView(x, y);
                else
                    addWhiteView(x, y);
                mXNumTv.setText("x");
                mYNumTv.setText("y");

            }
        });
    }


    public void Scroll(final ScrollView scrollView) {
        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.post(new Runnable() {
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    public void addWhiteView(int x, int y) {
        LayoutInflater mflater = LayoutInflater.from(this);
        mInfoViewWhite = (LinearLayout) mflater.inflate(R.layout.coordinate_xy, null);
        TextView xTv = (TextView) mInfoViewWhite.findViewById(R.id.x_tv);
        TextView yTv = (TextView) mInfoViewWhite.findViewById(R.id.y_tv);
        xTv.setText(Integer.toString(x));
        yTv.setText(Integer.toString(y));
        mWhiteLl.addView(mInfoViewWhite);
        Scroll(mBlackSv);
    }

    public void addBlackView(int x, int y) {
        LayoutInflater mflater = LayoutInflater.from(this);
        mInfoViewBlack = (LinearLayout) mflater.inflate(R.layout.coordinate_xy, null);
        TextView xTv = (TextView) mInfoViewBlack.findViewById(R.id.x_tv);
        TextView yTv = (TextView) mInfoViewBlack.findViewById(R.id.y_tv);
        xTv.setText(Integer.toString(x));
        yTv.setText(Integer.toString(y));
        mBlackLl.addView(mInfoViewBlack);
        Scroll(mWhiteSv);
    }

    private void updateActive(Game game) {
        if (game.getActive() == Game.BLACK) {
            mBlackActive.setVisibility(View.VISIBLE);
            mWhiteActive.setVisibility(View.INVISIBLE);
        } else {
            mBlackActive.setVisibility(View.INVISIBLE);
            mWhiteActive.setVisibility(View.VISIBLE);
        }
    }

    private void updateScore(Player black, Player white) {
        mBlackWin.setText(black.getWin());
        mWhiteWin.setText(white.getWin());
    }

    private void showWinDialog(String message) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setCancelable(false);
        b.setMessage(message);
        b.setPositiveButton(R.string.Continue, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGame.reset();
                resetAllView();
                mGameView.drawGame();
            }
        });
        b.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        b.show();
    }

    public void resetAllView() {
        mWhiteLl.removeAllViews();
        mBlackLl.removeAllViews();
        mXNumTv.setText("x");
        mYNumTv.setText("y");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restart:
                mGame.reset();
                updateActive(mGame);
                updateScore(black, white);
                resetAllView();
                mGameView.drawGame();
                break;
            /*case R.id.rollback:
                mGame.rollback();
                updateActive(mGame);
                mGameView.drawGame();
                break;*/

            case R.id.num_0:
                inputNumber(0);
                break;
            case R.id.num_1:
                inputNumber(1);
                break;
            case R.id.num_2:
                inputNumber(2);
                break;
            case R.id.num_3:
                inputNumber(3);
                break;
            case R.id.num_4:
                inputNumber(4);
                break;
            case R.id.num_5:
                inputNumber(5);
                break;
            case R.id.num_6:
                inputNumber(6);
                break;
            case R.id.num_7:
                inputNumber(7);
                break;
            case R.id.num_8:
                inputNumber(8);
                break;
            case R.id.num_9:
                inputNumber(9);
                break;
            default:
                break;
        }

    }

    private void inputNumber(int num) {
        if (mXNumTv.getText().equals("x")) {
            mXNumTv.setText(Integer.toString(num));
        } else if (mYNumTv.getText().equals("y")) {
            mYNumTv.setText(Integer.toString(num));
            boolean sucess = mGameView.addNumberChess(Integer.parseInt(mXNumTv.getText().toString()), Integer.parseInt(mYNumTv.getText().toString()));
            if (!sucess) {
                Toast.makeText(this, "该棋子已存在，请重新落子", Toast.LENGTH_LONG).show();
                mXNumTv.setText("x");
                mYNumTv.setText("y");
            }
        }
    }
}
