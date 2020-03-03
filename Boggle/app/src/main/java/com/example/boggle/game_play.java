package com.example.boggle;


import android.content.Context;
import android.os.Bundle;

import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.graphics.Point;
import android.widget.GridLayout;
import android.widget.Button;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.io.*;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class game_play extends Fragment {
    public Button[][] buttons;
    public String[] alpha = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public String[] LetterCDF = {"E", "T", "A", "O", "I", "N", "S", "R", "H", "D" ,"L", "U",
             "C", "M", "F", "Y", "W", "G", "P", "B", "V", "K", "X", "Q", "J", "Z"};

    public double[] frequency = {.1202, .091, .0812, .0768, .0731, .0695, .0628, .0602, .0592, .0432, .0398, .0288,
              .0271, .0261, .0230, .0211, .0209, .0203, .0182, .0149, .0111, .0069, .0017, .0011, .010, .007};

    // Saves the current letter of this game
    public String[][] letters;

    public TextView selected_words;

    public Button clear;
    public Button submit;

    public int score = 0;

    private ArrayList<String> dictionary = new ArrayList<String>();

    private Set<String> usedWords = new HashSet<String>();

    public View view;

    public int[] lastNeighbor;

    public boolean testFirst = true;

    public interface GameListener {
        // Interface to pass desired objects back to Main to communicate with
        // the score fragment
        public void sendScore(int score);
    }

    GameListener GL;

//    public game_play() {
//        // Required empty public constructor
//    }

    @Override
    public void onAttach(Context context) {   //The onAttach method, binds the fragment to the owner.  Fragments are hosted by Activities, therefore, context refers to: ____________?
        super.onAttach(context);
        GL = (GameListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_game_play, container, false);
        selected_words = (TextView) view.findViewById(R.id.words);
        clear = (Button) view.findViewById(R.id.Clear);
        submit = (Button) view.findViewById(R.id.Submit);

        buildGUIByCode(view);


        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selected_words.setText("");
                lastNeighbor = null;
                testFirst = true;
                enable();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fill in, should interact with score through main
                // to update the score of current game
                Log.i(String.valueOf(score) , " This is the score rn");
                playGame();
                GL.sendScore(score);
                enable();
                selected_words.setText("");
                lastNeighbor = null;
                testFirst = true;
            }
        });

        // Loading in the Dictionary of words
        try {
           InputStream inputStream =  getContext().getAssets().open("words.txt");
           BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
           String s;
           while((s = reader.readLine()) != null) {
               dictionary.add(s.toLowerCase());
           }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return view;
    }

    public String generateRandomLetter(double r) {
        double randomNum = r;
        int counter = 0;
        double[] totalProb = new double[26];
        for (int i  = 0; i < totalProb.length; i++) {
            if (i == 0) {
                totalProb[i] = frequency[i];
            } else {
                totalProb[i] = frequency[i] + frequency[(i-1)];
            }
        }
        for (double probabilities: totalProb) {
            counter++;
            if (randomNum >= probabilities){
                int index = counter;
                return LetterCDF[index];
            }
        }
        return "";
    }

    public void restartGame() {
        score = 0;
        usedWords.clear();
        selected_words.setText("");
        lastNeighbor = null;
        testFirst = true;
        Random r = new Random();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int randomNumber = r.nextInt(alpha.length);
                buttons[row][col].setText(alpha[randomNumber]);
                letters[row][col] = alpha[randomNumber];
//                  String randomLetter = generateRandomLetter(r.nextDouble());
//                  buttons[row][col].setText(randomLetter);
//                  letters[row][col] = generateRandomLetter(r.nextDouble());
            }
        }
        enable();
    }


    public void buildGUIByCode(View v){
        // Get width of screen
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        double w = size.x / 4.15;

        // Set the columns and rows of the layout
        GridLayout gridLayout = (GridLayout) v.findViewById(R.id.grid_lay);
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount(4);

        // Create the buttons and add them to gridLayout
        buttons = new Button[4][4];
        letters = new String[4][4];
        Random r = new Random();
        ButtonHandler bh = new ButtonHandler();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                int randomNumber = r.nextInt(alpha.length);
//                String randomLetter = generateRandomLetter(r.nextDouble());
                buttons[row][col] = new Button(getActivity());
                buttons[row][col].setTextSize((int) (w * .2));
                buttons[row][col].setText(alpha[randomNumber]);
                letters[row][col] = alpha[randomNumber];
//                Log.i(randomLetter, "This is the random Letter");
//                buttons[row][col].setText(randomLetter);
//                letters[row][col] = randomLetter;
                buttons[row][col].setOnClickListener(bh);
                gridLayout.addView(buttons[row][col], (int) w, (int) w);
            }
        }
    }


    public void enable() {
        // Enables all buttons
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                buttons[row][col].setEnabled(true);
            }
        }
    }

    public void update(int row, int col) {
        // This method should check if it is a valid click, such as
        // if the button is adjacent to another button (kept track by
        // keeping the row,col of last button clicked).
        // Should disable button when clicked also update the textView
//        if (lastNeighbor == null) {
//            lastNeighbor[0] = row;
//            lastNeighbor[1] = col;
//        }
        if (testFirst == true) {
            buttons[row][col].setEnabled(false);
            String s = selected_words.getText().toString();
            s = s.concat(letters[row][col]);
            selected_words.setText(s);
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return;
        }
        if (isNeighbor(row, col)) {
            buttons[row][col].setEnabled(false);
            String s = selected_words.getText().toString();
            s = s.concat(letters[row][col]);
            selected_words.setText(s);
            return;
        }
        if (testFirst == false) {
            Toast.makeText(getActivity(), "You may only select connected letters", Toast.LENGTH_SHORT).show();
            return;
        }
//        buttons[row][col].setEnabled(false);
//        String s = selected_words.getText().toString();
//        s = s.concat(letters[row][col]);
//        selected_words.setText(s);
    }

    public boolean isNeighbor(int row, int col) {
        int lastRow = lastNeighbor[0];
        int lastCol = lastNeighbor[1];
        if (isCorner(row, col)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if (isEdge(row, col)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        }
        if ((row - 1 == lastRow) && (col - 1 == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row - 1 == lastRow) && (col == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row - 1 == lastRow) && (col + 1 == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row == lastRow) && (col - 1 == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row == lastRow) && (col + 1 == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row + 1 == lastRow) && (col - 1 == lastCol)){
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row + 1 == lastRow) && (col == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        } else if ((row + 1 == lastRow) && (col + 1 == lastCol)) {
            lastNeighbor[0] = row;
            lastNeighbor[1] = col;
            return true;
        }
        return false;
    }

    public boolean isCorner(int row, int col) {
        int lastRow = lastNeighbor[0];
        int lastCol = lastNeighbor[1];
        if (lastRow == 0 && lastCol == 0) {
            if (row == 0  && col == 1) {
                return true;
            } else if (row == 1 && col == 1) {
                return true;
            } else if (row == 1 && col == 0) {
                return true;
            }
            return false;
        } else if (lastRow == 0 && lastCol == 3) {
            if (row == 0 && col == 2) {
                return true;
            } else if (row == 1 && col == 2) {
                return true;
            } else if (row == 1 && col == 3) {
                return true;
            }
            return false;
        } else if (lastRow == 3 && lastCol == 0) {
            if (row == 2 && col == 0) {
                return true;
            } else if (row == 2 && col == 1) {
                return true;
            } else if (row == 3 && col == 1) {
                return true;
            }
            return false;
        } else if (lastRow == 3 && lastCol == 3) {
            if (row == 3 && col == 2) {
                return true;
            } else if (row == 2 && col == 2) {
                return true;
            } else if (row == 2 && col == 3) {
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean isEdge(int row, int col) {
        int lastRow = lastNeighbor[0];
        int lastCol = lastNeighbor[1];
        if (lastRow == 0 && lastCol == 1) {
            if (row == 0 && col == 0) {
                return true;
            } else if (row == 1 && col == 0) {
                return true;
            } else if (row == 1 && col == 1) {
                return true;
            } else if (row == 1 && col == 2) {
                return true;
            } else if (row == 0 && col == 2) {
                return true;
            }
            return false;
        } else if (lastRow == 0 && lastCol == 2) {
            if (row == 0 && col == 1) {
                return true;
            } else if (row == 1 && col == 1) {
                return true;
            } else if (row == 1 && col == 2) {
                return true;
            } else if (row == 1 && col == 3) {
                return true;
            } else if (row == 0 && col == 3) {
                return true;
            }
            return false;
        } else if (lastRow == 1 && lastCol == 0) {
            if (row == 0 && col == 0) {
                return true;
            } else if (row == 0 && col == 1) {
                return true;
            } else if (row == 1 && col == 1) {
                return true;
            } else if (row == 2 && col == 1) {
                return true;
            } else if (row == 2 && col == 0) {
                return true;
            }
            return false;
        } else if (lastRow == 2 && lastCol == 0) {
            if (row == 1 && col == 0) {
                return true;
            } else if (row == 1 && col == 1) {
                return true;
            } else if (row == 2 && col == 1) {
                return true;
            } else if (row == 3 && col == 1) {
                return true;
            } else if (row == 3 && col == 0) {
                return true;
            }
            return false;
        } else if (lastRow == 3 && lastCol == 1) {
            if (row == 3 && col == 0) {
                return true;
            } else if (row == 2 && col == 0) {
                return true;
            } else if (row == 2 && col == 1) {
                return true;
            } else if (row == 2 && col == 2) {
                return true;
            } else if (row == 3 && col == 2) {
                return true;
            }
            return false;
        } else if (lastRow == 3 && lastCol == 2) {
            if (row == 3 && col == 1) {
                return true;
            } else if (row == 2 && col == 1) {
                return true;
            } else if (row == 2 && col == 2) {
                return true;
            } else if (row == 2 && col == 3) {
                return true;
            } else if (row == 3 && col == 3) {
                return true;
            }
            return false;
        } else if (lastRow == 1 && lastCol == 3) {
            if (row == 0 && col == 3) {
                return true;
            } else if (row == 0 && col == 2) {
                return true;
            } else if (row == 1 && col == 2) {
                return true;
            } else if (row == 2 && col == 2) {
                return true;
            } else if (row == 2 && col == 3) {
                return true;
            }
            return false;
        } else if (lastRow == 2 && lastCol == 3) {
            if (row == 1 && col == 3) {
                return true;
            } else if (row == 1 && col == 2) {
                return true;
            } else if (row == 2 && col == 2) {
                return true;
            } else if (row == 3 && col == 2) {
                return true;
            } else if (row == 3 && col == 3) {
                return true;
            }
            return false;
        }
        return false;
    }


    private class ButtonHandler implements View.OnClickListener {
        public void onClick(View v) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 4; col++) {
                    if (v == buttons[row][col]) {
                        if (testFirst == true) {
                            lastNeighbor = new int[] {row, col};
                            update(row, col);
                            testFirst = false;
                        } else {
                            update(row, col);
                        }
                    }
                }
            }
        }
    }

    public boolean checkVowels(String s){
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'A') {
                count += 1;
            } else if (s.charAt(i) == 'E') {
                count += 1;
            } else if (s.charAt(i) == 'I') {
                count += 1;
            } else if (s.charAt(i) == 'O') {
                count += 1;
            } else if (s.charAt(i) == 'U') {
                count += 1;
            }
        }
        return (count >= 2);
    }


    public int calcuatePoints (String answer){
        int points = 0;
        int bonusDouble = 0;
        Log.i(answer, "This is the answer");
        for(int i = 0; i < answer.length(); i++) {
            if (answer.charAt(i) ==  'A') {
                points += 5;
            } else if (answer.charAt(i) == 'E'){
                points += 5;
            } else if (answer.charAt(i) == 'I') {
                points += 5;
            } else if (answer.charAt(i) == 'O') {
                points += 5;
            } else if (answer.charAt(i) == 'U') {
                points += 5;
            } else if (answer.charAt(i) == 'S') {
                bonusDouble += 1;
            } else if (answer.charAt(i) == 'Z') {
                bonusDouble += 1;
            } else if (answer.charAt(i) == 'P') {
                bonusDouble += 1;
            } else if (answer.charAt(i) == 'X') {
                bonusDouble += 1;
            } else if (answer.charAt(i) == 'Q') {
                bonusDouble += 1;
            } else {
                points += 1;
            }
        }
        if (bonusDouble == 0) {
            return points;
        }
        return (points * (bonusDouble * 2));
    }

    public void playGame() {
        String answer = String.valueOf(selected_words.getText());
        int points = 0;
        Log.i(answer, "This is the answer response!" );
        if (answer.equals("")) {
            Toast.makeText(getActivity(), "Please select letters!" , Toast.LENGTH_SHORT).show();
            return;
        }
        if (isValid()){
                points = calcuatePoints(answer);
                Log.i(String.valueOf(points), "This is the points");
                Toast.makeText(getActivity(), "That's Correct +" + String.valueOf(points), Toast.LENGTH_LONG).show();
                usedWords.add(answer);
        } else {
            Log.i(answer, "This is the answer");
            points -= 10;
        }
        score += points;
    }


    // Checks if the word is valid
    public boolean isValid() {
        String checkValid = String.valueOf(selected_words.getText());
        if (checkValid.length() < 4) {
            Log.i(String.valueOf(checkValid.length()), "This is the answer2");
            Toast.makeText(getActivity(), "That's incorrect, -10", Toast.LENGTH_LONG).show();
            return false;
        } else if (checkVowels(checkValid) != true) {
            Log.i(checkValid, "This is the answer3");
            Toast.makeText(getActivity(), "That's incorrect, -10", Toast.LENGTH_LONG).show();
            return false;
        } else if (!dictionary.contains(checkValid.toLowerCase())) {
            Log.i(String.valueOf(!dictionary.contains(checkValid.toLowerCase())), "This is a test");
            Log.i(checkValid, "This is the answer4");
            Toast.makeText(getActivity(), "That's incorrect, -10", Toast.LENGTH_LONG).show();
            return false;
        } else if (usedWords.contains(checkValid)) {
            Log.i(checkValid, "This is the answer5");
            Log.i(usedWords.toString(), "This is the used words");
            Toast.makeText(getActivity(), "That's incorrect, -10", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
