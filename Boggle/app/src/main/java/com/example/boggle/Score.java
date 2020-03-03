package com.example.boggle;


import android.content.Context;
import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import javax.microedition.khronos.opengles.GL;


/**
 * A simple {@link Fragment} subclass.
 */
public class Score extends Fragment {

    public TextView score;
    public Button newGame;
//    public Score() {
//        // Required empty public constructor
//    }

    public interface scoreListener {
        // Interface to pass desired objects back to Main to communicate with
        // the score fragment
       public void newGame();
    }

    scoreListener SL;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        SL = (scoreListener) context;

        //may need to add interface, then onAttach is needed, otherwise don't need onAttach
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_score, container, false);

        score = (TextView) view.findViewById(R.id.score);
        newGame = (Button) view.findViewById(R.id.newGame);


        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // should interact with game play through main to clear game
                // enable buttons, etc
                score.setText(String.valueOf(0));
                SL.newGame();

            }
        });

        return view;
    }

    public void setScore (int scoreSet) {
        Log.i(String.valueOf(score) , "This is from Score");
        score.setText(String.valueOf(scoreSet));
    }

}
