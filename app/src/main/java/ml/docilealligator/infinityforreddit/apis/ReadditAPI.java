package ml.docilealligator.infinityforreddit.apis;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;



public interface ReadditAPI {
    class Read {

        @SerializedName("reddit_id")
        String redditId;

        public Read(String redditId) {
            this.redditId = redditId;
        }

        public String getRedditId() {
            return this.redditId;
        }
    }

    @POST("/reads")
    Call<String> insertReads(@Header(APIUtils.AUTHORIZATION_KEY) String header, @Body List<Read> reads);

    @GET("/reads")
    Call<List<Read>> getReads(@Header(APIUtils.AUTHORIZATION_KEY) String header,
                              @Query("reddit_ids") String redditIds,
                              @Query("read_at_after") String readAtAfter);
}
