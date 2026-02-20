package ml.docilealligator.infinityforreddit.readpost;

import android.content.SharedPreferences;

import androidx.annotation.WorkerThread;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.synccit.Readdit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

@Singleton
public class ReadPostRepository {
    private final ReadPostDao mReadPostDao;
    private final Readdit mReaddit;
    private final SharedPreferences mCurrentAccountSharedPreferences;
    private final SharedPreferences mPostHistorySharedPreferences;
    private final Executor mExecutor;

    @Inject
    public ReadPostRepository(RedditDataRoomDatabase redditDataRoomDatabase,
                              Readdit readdit,
                              @Named("current_account") SharedPreferences currentAccountSharedPreferences,
                              @Named("post_history") SharedPreferences postHistorySharedPreferences,
                              Executor executor) {
        mReadPostDao = redditDataRoomDatabase.readPostDao();
        mReaddit = readdit;
        mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
        mPostHistorySharedPreferences = postHistorySharedPreferences;
        mExecutor = executor;
    }

    private String getUsername() {
        return mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "");
    }

    private int getReadPostsLimit() {
        String username = getUsername();
        if (mPostHistorySharedPreferences.getBoolean(username + SharedPreferencesUtils.READ_POSTS_LIMIT_ENABLED, true)) {
            return mPostHistorySharedPreferences.getInt(username + SharedPreferencesUtils.READ_POSTS_LIMIT, 500);
        } else {
            return -1;
        }
    }

    @WorkerThread
    public Set<String> filterOutUnread(List<String> postIds) {
        String username = getUsername();
        if (username.isEmpty()) {
            return Collections.emptySet();
        }
        if (useReadditBackend()) {
            return new HashSet<>(mReaddit.getReadIds(postIds));
        }
        return new HashSet<>(mReadPostDao.getReadPostsIdsByIds(postIds, username));
    }

    public void insertAsync(String postId) {
        mExecutor.execute(() -> {
            String username = getUsername();
            if (username != null && !username.isEmpty()) {
                if (useReadditBackend()) {
                    mReaddit.insertRead(postId);
                }
                int readPostsLimit = getReadPostsLimit();
                int limit = Math.max(readPostsLimit, 100);
                boolean isReadPostLimit = readPostsLimit != -1;
                while (mReadPostDao.getReadPostsCount(username) > limit && isReadPostLimit) {
                    mReadPostDao.deleteOldestReadPosts(username);
                }
                mReadPostDao.insert(new ReadPost(username, postId));
            }
        });
    }

    public List<ReadPost> getAllBeforeReference(String referencePostId) {
        String username = getUsername();
        if (username.isEmpty()) {
            return new ArrayList<>();
        }
        ReadPost referenceReadPost = mReadPostDao.getReadPost(username, referencePostId);
        if (referenceReadPost == null) {
            return new ArrayList<>();
        }
        return mReadPostDao.getAllReadPosts(username, referenceReadPost.getTime());
    }

    public ListenableFuture<List<ReadPost>> getAllListenableFuture(Long before) {
        String username = getUsername();
        if (username.isEmpty()) {
            return Futures.immediateFuture(new ArrayList<>());
        }
        return mReadPostDao.getAllReadPostsListenableFuture(username, before);
    }

    public void deleteAllAsync(Handler handler, DeleteAllReadPostsAsyncTaskListener listener) {
        mExecutor.execute(() -> {
            String username = getUsername();
            if (username != null && !username.isEmpty()) {
                mReadPostDao.deleteAllReadPosts(username);
            }
            handler.post(listener::success);
        });
    }

    public interface DeleteAllReadPostsAsyncTaskListener {
        void success();
    }

    private boolean useReadditBackend() {
        String username = getUsername();
        return mPostHistorySharedPreferences.getBoolean(
                username + SharedPreferencesUtils.USE_READDIT_BACKEND, false);
    }
}
