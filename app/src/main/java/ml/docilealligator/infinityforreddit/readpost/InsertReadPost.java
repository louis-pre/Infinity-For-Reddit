package ml.docilealligator.infinityforreddit.readpost;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class InsertReadPost {
    public static void insertReadPost(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                      String username, String postId) {
        executor.execute(() -> {
            if (username != null && !username.equals("")) {
                ReadPostDao readPostDao = redditDataRoomDatabase.readPostDao();
                readPostDao.insert(new ReadPost(username, postId));
            }
        });
    }
}
