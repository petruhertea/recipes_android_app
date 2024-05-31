package com.recipes.retrofit;

import android.content.Context;

import androidx.annotation.NonNull;

import com.recipes.BuildConfig;

import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = BuildConfig.BASE_URL;

    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB cache size


    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


            Cache cache = new Cache(context.getCacheDir(), CACHE_SIZE);
            httpClient.cache(cache).addInterceptor(new Interceptor() {
                @NonNull
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {

                    Request request = chain.request();

                    if (NetworkCheck.hasNetwork(context)) {

                        request = request.newBuilder()
                                .header("Cache-Control", "public, max-age=" + 5)
                                .build();
                    } else {

                        request = request.newBuilder()
                                .header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                                .build();
                    }

                    // Add the modified request to the chain.
                    return chain.proceed(request);
                }
            });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(httpClient.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}