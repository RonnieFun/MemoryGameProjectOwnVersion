package com.example.ca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class LoadImagesActivity extends AppCompatActivity {

    private Elements imagesDownload;
    private Document website;
    private ProgressBar downloadProgressBar;
    private TextView downloadProgressText;
    private TextView proceedToGame;
    private final Collection<ImageView> selectedImages = new ArrayList<>();
    private boolean buttonPressedAgain = false;
    int numberOfImages = 20;
    int numberOfGameImages = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadimages);

        Button getUrl = findViewById(R.id.FetchButton);
        getUrl.setOnClickListener((view) -> GetImagesFromUrl());

        Intent intent = getIntent();
        EditText enteredUrlRaw = findViewById(R.id.EnteredUrl);
        enteredUrlRaw.setText(intent.getStringExtra("EnteredUrl"));

        downloadProgressText = findViewById(R.id.DownloadProgress);
        proceedToGame = findViewById(R.id.ProceedToGame);

        getUrl.bringToFront();
        downloadProgressText.setText(R.string.awaitingUrlInput);
        downloadProgressText.bringToFront();

        if (intent.getBooleanExtra("startProgram", false)) {
            GetImagesFromUrl();
        }
    }

    public void GetImagesFromUrl() {
        //set up alternate Fetch button
        Button getUrlWhileScraping = findViewById(R.id.FetchButtonWhileScraping);
        getUrlWhileScraping.setOnClickListener((view) -> fetchButtonPressedWhileLoadingImages(true));
        getUrlWhileScraping.bringToFront();

        //load placeholder images
        ConstraintLayout loadImagesViewImages = findViewById(R.id.Images);
        for (int i = 0; i < loadImagesViewImages.getChildCount(); i++) {
            Picasso.get().load(R.drawable.x).fit().into((ImageView) loadImagesViewImages.getChildAt(i));
        }

        //scrape website and get images on new thread so main thread can load progress
        new Thread(() -> {
            try
            {
                //set up progress bar and status
                downloadProgressBar = findViewById(R.id.DownloadProgressBar);
                downloadProgressText.setText(getString(R.string.checkingTheWebsite));

                hideKeyboard();
                getWebsite();

                //get images from website
                imagesDownload = website.getElementsByTag("img");
                int i = 1;
                int j = 0;
                while (i <= numberOfImages && !buttonPressedAgain) {
                    if (imagesDownload.get(j).attr("src") != null && imagesDownload.get(j).attr("src").contains("http")) {
                        String a = "imageView" + i;
                        int emptyImageId = getResources().getIdentifier(a, "id", getPackageName());
                        ImageView emptyImage = findViewById(emptyImageId);
                        insertImages(imagesDownload, i, j, emptyImage);
                        setClickTrackerUsingMainThread(emptyImage);
                        i++;
                    }
                    j++;
                }
            }
            catch (Exception e)
            {
                runOnUiThread(() -> Toast.makeText(this, "We were unable to extract the images from the entered url. Please enter another url.", Toast.LENGTH_LONG).show());
                fetchButtonPressedWhileLoadingImages(false);
            }
        }).start();
    }

    public void fetchButtonPressedWhileLoadingImages(boolean startProgram) {
        //get entered url before closing current activity
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();

        //to stop the loop in GetImagesFromUrl
        buttonPressedAgain = true;

        //redirect to "restart" activity
        Intent intent = new Intent(this, LoadImagesActivity.class);
        intent.putExtra("EnteredUrl", EnteredUrl);
        intent.putExtra("startProgram", startProgram);
        startActivity(intent);
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(findViewById(R.id.EnteredUrl).getWindowToken(), 0);
    }

    public void getWebsite() {
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();
        try {
            website = Jsoup.connect(EnteredUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setClickTrackerUsingMainThread(ImageView iv) {
        runOnUiThread(() ->
                iv.setOnClickListener(view ->
                        clickImage(iv)));
    }

    public void insertImages(final Elements images, final int z, final int y,
                             final ImageView emptyImageViews) {
        runOnUiThread(() -> {
            //set images
            Picasso.get().load(images.get(y).attr("src")).fit().placeholder(R.drawable.x).into(emptyImageViews);
            emptyImageViews.setContentDescription(images.get(y).attr("src"));

            //increment progress bar and progress text
            downloadProgressBar.incrementProgressBy(1);
            if (z >= numberOfImages) {
                downloadProgressText.setText(getString(R.string.downloadCompletedImages, numberOfImages));
            } else {
                downloadProgressText.setText(getString(R.string.downloadingImageProgress, z, numberOfImages));
            }
        });
    }

    public void clickImage(ImageView iv) {
        //image not selected yet, <6 images selected
        if (!selectedImages.contains(iv) && selectedImages.size() < numberOfGameImages) {
            selectedImages.add(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size()));
            iv.setForeground(AppCompatResources.getDrawable(this, R.drawable.chosen));

            //6 images selected
            if (selectedImages.size() == numberOfGameImages) {
                proceedToGame.bringToFront();
                proceedToGame.setOnClickListener(view -> goToGame(selectedImages));
            }
        }
        //image already selected
        else if (selectedImages.contains(iv)) {
            selectedImages.remove(iv);
            downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size()));
            iv.setForeground(null);

            //image unselected results in <6 images selected
            if (selectedImages.size() < numberOfGameImages) {
                downloadProgressText.bringToFront();
                downloadProgressText.setText(getString(R.string.selectedImages, selectedImages.size()));
                proceedToGame.setOnClickListener(null);
            }
        }
    }

    public void goToGame(Collection<ImageView> selectedImages) {
        ArrayList<String> selectedImagesUrls = new ArrayList<>();
        for (ImageView image : selectedImages) {
            selectedImagesUrls.add((String) image.getContentDescription());
        }

        //set image urls in intent
        Intent intent = new Intent(this, GameActivity.class);
        intent.putStringArrayListExtra("SelectedImagesUrls", selectedImagesUrls);
        startActivity(intent);
    }
}