package ml.docilealligator.infinityforreddit.synccit;

import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.concurrent.TimeUnit;

import ml.docilealligator.infinityforreddit.apis.ReadditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
public class Readdit {

    private final ReadditAPI mReadditAPI;

    @Inject
    public Readdit(@Named("base") Retrofit retrofit, @Named("base") OkHttpClient okHttpClient) {
        OkHttpClient readditClient = okHttpClient.newBuilder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .build();

        mReadditAPI = retrofit.newBuilder()
                .baseUrl(APIUtils.READDIT_API_BASE_URI)
                .client(readditClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ReadditAPI.class);
    }

    public void insertRead(String postId) {
        insertReads(Collections.singletonList(postId));
    }

    public void insertReads(List<String> postIds) {
        List<ReadditAPI.Read> insertReads = new ArrayList<>();
        for (int i = 0; i < postIds.size(); i++) {
            insertReads.add(new ReadditAPI.Read(postIds.get(i)));
        }

        String header = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE5NzExMTYxOTYsImlhdCI6MTcwODIwMDk5Niwic3ViIjoiZGphZ2F0YWhlbCJ9.xKnXMU0Jk28Gra9cbtsJ7_6NFtVceZEbo5bm3lF_M1I";
        Call<String> result = mReadditAPI.insertReads(header, insertReads);

        try {
            Response<String> response = result.execute();
            if (!response.isSuccessful()) {
                Log.e("Readdit", "Failed call to Readdit API with code: " + response.code());
                return;
            }
            Log.i("Readdit", "Inserted " + postIds);
        } catch (IOException e) {
            Log.e("Readdit", "Could not call Readdit API " + e.getMessage());
        }
    }

    public List<String> getReadIds(List<String> postIds) {
        return getReadIds(postIds, null);
    }

    public List<String> getReadIds(List<String> postIds, Long readAtAfter) {
        List<String> readIds = new ArrayList<>();
        String postIdsString = String.join(",", postIds);
        String header = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE5NzExMTYxOTYsImlhdCI6MTcwODIwMDk5Niwic3ViIjoiZGphZ2F0YWhlbCJ9.xKnXMU0Jk28Gra9cbtsJ7_6NFtVceZEbo5bm3lF_M1I";
        Call<List<ReadditAPI.Read>> resultCall = mReadditAPI.getReads(header, postIdsString,
                readAtAfter != null ? convertLongToNaiveDateTimeString(readAtAfter) : null);

        try {
            Response<List<ReadditAPI.Read>> response = resultCall.execute();
            if (!response.isSuccessful()) {
                Log.e("Readdit", "Failed call to Readdit API with code: " + response.code());
                return null;
            }

            List<ReadditAPI.Read> reads = response.body();
            reads = reads != null ? reads : new ArrayList<>();

            for (int i = 0; i < reads.size(); i++) {
                readIds.add(reads.get(i).getRedditId());
            }
            Log.i("Readdit", "Found: " + readIds);
            return readIds;
        } catch (IOException e) {
            Log.e("Readdit", "Could not call Readdit API " + e.getMessage());
            return null;
        }
    }

    public String convertLongToNaiveDateTimeString(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
