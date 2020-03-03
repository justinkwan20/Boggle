package com.example.boggle;

//import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;

public class MainActivity extends Activity implements game_play.GameListener, Score.scoreListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void sendScore(int score) {
        Score  recieveScore = (Score) getFragmentManager().findFragmentById(R.id.score_fragment);
        recieveScore.setScore(score);
    }

    @Override
    public void newGame() {
        game_play gamePlay = (game_play) getFragmentManager().findFragmentById((R.id.game_fragment));
        gamePlay.restartGame();
    }

}
