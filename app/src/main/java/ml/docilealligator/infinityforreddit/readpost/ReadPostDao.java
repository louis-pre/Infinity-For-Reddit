package ml.docilealligator.infinityforreddit.readpost;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface ReadPostDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReadPost readPost);

    @Query("SELECT * FROM read_posts WHERE username = :username AND (:before IS NULL OR time < :before) ORDER BY time DESC LIMIT 25")
    ListenableFuture<List<ReadPost>> getAllReadPostsListenableFuture(String username, Long before);

    @Query("SELECT * FROM read_posts WHERE username = :username AND (:before IS NULL OR time < :before) ORDER BY time DESC LIMIT 25")
    List<ReadPost> getAllReadPosts(String username, Long before);

    @Query("SELECT * FROM read_posts WHERE username = :username AND id IN (:ids)")
    List<ReadPost> getReadPostByIds(String username, List<String> ids);

    @Query("SELECT * FROM read_posts WHERE id = :id LIMIT 1")
    ReadPost getReadPost(String id);

    @Query("DELETE FROM read_posts")
    void deleteAllReadPosts();
}
