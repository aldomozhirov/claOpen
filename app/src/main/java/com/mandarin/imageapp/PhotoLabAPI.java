package com.mandarin.imageapp;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PhotoLabAPI {

    @Multipart
    @POST("upload.php")
    Call<String> uploadPhoto(@Part() MultipartBody.Part file);

    @FormUrlEncoded
    @POST("template_process.php")
    Call<String> processWithTemplate(@Field("image_url") String imageUrl, @Field("template_name") String templateName);

}
