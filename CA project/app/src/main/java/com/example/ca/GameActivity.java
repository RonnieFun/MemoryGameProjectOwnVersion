package com.example.ca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    int a = 0;
    ImageView FirstChoice;
    int PlayerScore = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameboard);

        Intent intent = getIntent();
        List<String> ChosenImagesUrls = intent.getStringArrayListExtra("SelectedImagesUrls");
        List<String> GameImageUrls = new ArrayList<>();
        GameImageUrls.addAll(ChosenImagesUrls);
        GameImageUrls.addAll(ChosenImagesUrls);
        Collections.shuffle(GameImageUrls);
        ScoreIncrease();


        new Thread(() ->
        {
            for (int i = 21; i <= 32; i++) {
                String a = "imageView" + i;

                int emptyimageid = getResources().getIdentifier(a, "id", getPackageName());
                ImageView emptyimage = findViewById(emptyimageid);
                LoadImagesForGame(GameImageUrls.get(i - 21), emptyimage);
            }
        }).start();
    }

    public void LoadImagesForGame(String imgurl, ImageView iv) {
        runOnUiThread(() ->
        {
            Picasso.get().load(imgurl).fit().into(iv);
            iv.setContentDescription(imgurl);
            iv.setForeground(getDrawable(R.drawable.x));
            iv.setOnClickListener(view -> ChooseImage(iv));
        });
    }

    public void showImage(ImageView imgv) {
        runOnUiThread(() ->
        {
            imgv.setForeground(null);
        });
    }

    public void showX(ImageView imgv) {
        runOnUiThread(() ->
        {
            imgv.setForeground(getDrawable(R.drawable.x));
        });
    }

    public void showTick(ImageView imgv) {
        runOnUiThread(() ->
        {
            imgv.setForeground(getDrawable(R.drawable.tick));
        });
    }

    public void showCross(ImageView imgv) {
        runOnUiThread(() ->
        {
            imgv.setForeground(getDrawable(R.drawable.cross));
        });
    }

    public void ScoreIncrease() {
        TextView score = findViewById(R.id.Score);
        Chronometer GameTimer = findViewById(R.id.TimeElapsed);
        runOnUiThread(() ->
        {
            if (PlayerScore < 6) {
                score.setText("Current score:\n" + PlayerScore + " of 6 matches");
                PlayerScore++;
            } else {
                score.setText("COMPLETED! :)");
                GameTimer.stop();
            }
        });
    }

    public void ChooseImage(ImageView imagev) {
        Chronometer GameTimer = findViewById(R.id.TimeElapsed);
        new Thread(() ->
        {
            if (FirstChoice == null) {
                GameTimer.start();
            }
            a++;
            showImage(imagev);
            if (a == 2) {
                if (FirstChoice.getContentDescription() == imagev.getContentDescription()) {
                    showTick(FirstChoice);
                    FirstChoice.setOnClickListener(null);
                    showTick(imagev);
                    imagev.setOnClickListener(null);
                    ScoreIncrease();
                } else {
                    showCross(FirstChoice);
                    showCross(imagev);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    showX(FirstChoice);
                    showX(imagev);
                }
                a = 0;
            } else if (a == 1) {
                FirstChoice = imagev;
            }
        }).start();
    }
}
