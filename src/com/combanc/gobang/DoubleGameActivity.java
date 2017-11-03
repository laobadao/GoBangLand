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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    private TextView mBlackX;
    private TextView mBlackY;

    private LinearLayout white_ll;
    private LinearLayout infoView_white;
    private ImageView mBlackActive;
    private ImageView mWhiteActive;

    // Control Button
    private Button restart;
    private Button rollback;

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

        ;
    };

    private void initViews() {
        mGameView = (GameView) findViewById(R.id.game_view);
        mBlackWin = (TextView) findViewById(R.id.black_win);
        mBlackActive = (ImageView) findViewById(R.id.black_active);
        mWhiteWin = (TextView) findViewById(R.id.white_win);
        mBlackX = (TextView) findViewById(R.id.x_tv);
        mBlackY = (TextView) findViewById(R.id.y_tv);
        mWhiteActive = (ImageView) findViewById(R.id.white_active);
        restart = (Button) findViewById(R.id.restart);
        rollback = (Button) findViewById(R.id.rollback);
        white_ll = (LinearLayout) findViewById(R.id.white_ll);

        restart.setOnClickListener(this);
        rollback.setOnClickListener(this);
    }

    private void initGame() {
        black = new Player(Game.BLACK);
        white = new Player(Game.WHITE);
        mGame = new Game(mRefreshHandler, black, white);
        mGame.setMode(GameConstants.MODE_FIGHT);
        mGameView.setGame(mGame);
        mGameView.setOnDownActionListener(new GameView.OnDownActionListener() {

            @Override
            public void OnDown(int x, int y) {
                // 回调显示横纵坐标
                Log.d(TAG, "down x = " + x + " y = " + y);
                mBlackX.setText(Integer.toString(x));
                mBlackY.setText(Integer.toString(y));
                addView(x, y);
            }
        });
        updateActive(mGame);
        updateScore(black, white);
    }

    public void addView(int x, int y) {
        LayoutInflater mflater = LayoutInflater.from(this);
        infoView_white = (LinearLayout) mflater.inflate(R.layout.coordinate_xy, null);
        TextView xTv = (TextView) infoView_white.findViewById(R.id.x_tv);
        TextView yTv = (TextView) infoView_white.findViewById(R.id.y_tv);
        xTv.setText(Integer.toString(x));
        yTv.setText(Integer.toString(y));
        white_ll.addView(infoView_white);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restart:
                mGame.reset();
                updateActive(mGame);
                updateScore(black, white);
                mGameView.drawGame();
                break;
            case R.id.rollback:
                mGame.rollback();
                updateActive(mGame);
                mGameView.drawGame();
                break;
            default:
                break;
        }

    }
}
