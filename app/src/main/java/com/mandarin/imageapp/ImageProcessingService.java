package com.mandarin.imageapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ImageProcessingService {

    public static final String UPLOAD_URL = "http://upload-soft.photolab.me/";
    public static final String API_URL = "http://api-soft.photolab.me/";

    private static final String[] ELEMENTS_TEMPLATES = {
            "2949", // Butterfly Landing
            "2958", // Cartoon Pom Pom Hat
            "3022", // Rainbow Laser Eyes
            "3272", // Happy PETsgiving
            "2966", // Monarch Pet
            "2952", // Cosmic Vibes
            "2948", // Aristocrat Pets
            "2950", // La Vie en Rose
            "2953", // Pirate Pet
            "3255", // My DEERest Pet
            "2951", // Cloud Glasses
            "2956"  // CartoonEYES My Pet
    };

    private static final String[] CAPTIONS = {
            // Butterfly Landing
            "What a wonderful world",
            // Cartoon Pom Pom Hat
            "A daily dose of cuteness.",
            // Rainbow Laser Eyes
            "Stop hounding me!",
            // Happy PETsgiving
            "Thank you fur being a friend.",
            // Monarch Pet
            "The king of the house",
            // Cosmic Vibes
            "Look at me, baby",
            // Aristocrat Pets
            "Oh yeah, that's the spot!",
            // La Vie en Rose
            "I have just met you, and I love you.",
            // Pirate Pet
            "I steal your side of the bed.",
            // My DEERest Pet
            "Trans-fur-mation Day",
            // Cloud Glasses
            "Living my best life!",
            // CartoonEYES My Pet
            "Wait a minute... this isn't the park."
    };

    private static final String[] FRAMES_TEMPLATES = {
            "2492", // Meme Frame
    };

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static Future<Map<String, String>> process(String filePath) {
        Random rand = new Random();
        int randomElementIndex = rand.nextInt(ELEMENTS_TEMPLATES.length);
        return process(filePath, randomElementIndex);
    }

    public static Future<Map<String, String>> process(String filePath, int elementNumber) {
        String elementTemplateName = ELEMENTS_TEMPLATES[elementNumber];
        String frameTemplateName = FRAMES_TEMPLATES[0];
        Map<String, String> result = new HashMap<>();
        result.put("caption", CAPTIONS[elementNumber]);
        return executor.submit(() -> {
            try {
                String sourceImageUrl = submitImage(filePath).body();
                String imageWithElementUrl = processWithTemplate(sourceImageUrl, elementTemplateName).body();
                String imageWithFrameUrl = processWithTemplate(imageWithElementUrl, frameTemplateName).body();
                System.out.println(imageWithFrameUrl);
                result.put("imageUrl", imageWithFrameUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        });
    }

    public static Response<String> submitImage(String filePath) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UPLOAD_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        PhotoLabAPI photolabAPI = retrofit.create(PhotoLabAPI.class);

        Bitmap source = BitmapFactory.decodeFile(filePath);
        Bitmap cropped;
        if (source.getWidth() >= source.getHeight()) {
            cropped = Bitmap.createBitmap(
                    source,
                    source.getWidth() / 2 - source.getHeight() / 2,
                    0,
                    source.getHeight(),
                    source.getHeight()
            );
        } else {
            cropped = Bitmap.createBitmap(
                    source,
                    0,
                    source.getHeight() / 2 - source.getWidth() / 2,
                    source.getWidth(),
                    source.getWidth()
            );
        }
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CroppedImages";
        File dir = new File(file_path);
        if(!dir.exists()) dir.mkdirs();
        File file = new File(dir, "temp.jpg");
        FileOutputStream fOut = new FileOutputStream(file);
        cropped.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        Call<String> call = photolabAPI.uploadPhoto(body);
        return call.execute();
    }

    public static Response<String> processWithTemplate(String sourceImageUrl, String templateName) throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        PhotoLabAPI photolabAPI = retrofit.create(PhotoLabAPI.class);
        Call<String> call = photolabAPI.processWithTemplate(sourceImageUrl, templateName);
        return call.execute();
    }

}
