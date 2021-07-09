package com.example.ca;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoadImagesActivity extends AppCompatActivity implements View.OnClickListener {

    private Elements images;
    ProgressBar DownloadProgressBar;
    TextView DownloadProgressText;
    private Document website;
    ExecutorService executor = Executors.newFixedThreadPool(3);
    protected Collection<ImageView> selectedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.loadimages);
        Button GetUrl = findViewById(R.id.FetchButton);
        GetUrl.setOnClickListener(this);
        DownloadProgressText = findViewById(R.id.DownloadProgress);
        DownloadProgressText.setText("Awaiting url input");

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        new Thread(() -> {
            if (id == R.id.FetchButton) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.EnteredUrl).getWindowToken(), 0);
                DownloadProgressBar = findViewById(R.id.DownloadProgressBar);
                DownloadProgressText.setText("Checking the website...");
                Future f = executor.submit(ScrapeWebsite);
                while (!f.isDone()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                executor.shutdown();

                images = website.getElementsByTag("img");
                int i = 1;
                int j = 0;
                while (i <= 20) {
                    if (images.get(j).attr("src") != null && images.get(j).attr("src").contains("http")) {
                        String a = "imageView" + i;
                        int emptyimageid = getResources().getIdentifier(a, "id", getPackageName());
                        ImageView emptyimage = findViewById(emptyimageid);
                        InsertImages(images, i, j, emptyimage);
                        setClickTrackerUsingMainThread(emptyimage);
                        i++;
                    }
                    j++;
                }
            }
        }).start();
    }

    public void setClickTrackerUsingMainThread(ImageView iv) {
        runOnUiThread(() ->
                iv.setOnClickListener(view ->
                        clickImage(iv)));
    }

    public void clickImage(ImageView iv) {

        if (selectedImages.size() == 0) {
            selectedImages.add(iv);
            iv.setForeground(getDrawable(R.drawable.chosen));
        } else if (!selectedImages.contains(iv) && selectedImages.size() < 6) {
            selectedImages.add(iv);
            iv.setForeground(getDrawable(R.drawable.chosen));
            if (selectedImages.size() == 6) {
                DownloadProgressText.setText("Proceed to game");
                DownloadProgressText.setOnClickListener(view -> GoToGame(selectedImages));
            }
        } else if (selectedImages.contains(iv)) {
            selectedImages.remove(iv);
            iv.setForeground(null);
            if (selectedImages.size() < 6) {
                DownloadProgressText.setText("Download completed. Please select 6 images");
                if (DownloadProgressText.hasOnClickListeners()) {
                    DownloadProgressText.setOnClickListener(null);
                }
            }
        }
    }

    public void InsertImages(final Elements imgs, final int z, final int y,
                             final ImageView emptyimg) {
        runOnUiThread(() -> {
            DownloadProgressBar.incrementProgressBy(1);
            Picasso.get().load(imgs.get(y).attr("src")).fit().placeholder(R.drawable.x).into(emptyimg);
            emptyimg.setContentDescription(imgs.get(y).attr("src"));
            if (z >= 20) {
                DownloadProgressText.setText("Download completed. Please select 6 images");
            } else {
                DownloadProgressText.setText("Downloading images " + z + " of 20...");
            }
        });
    }

    public void GoToGame(Collection<ImageView> selectedImages) {
        ArrayList<String> selectedImagesUrls = new ArrayList<>();
        for (ImageView image : selectedImages) {
            selectedImagesUrls.add((String) image.getContentDescription());
        }
        Intent intent = new Intent(this, GameActivity.class);
        intent.putStringArrayListExtra("SelectedImagesUrls", selectedImagesUrls);
        startActivity(intent);
    }

    Runnable ScrapeWebsite = () -> {
        EditText EnteredUrlRaw = findViewById(R.id.EnteredUrl);
        String EnteredUrl = EnteredUrlRaw.getText().toString();

        try {
            website = Jsoup.connect(EnteredUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };
}