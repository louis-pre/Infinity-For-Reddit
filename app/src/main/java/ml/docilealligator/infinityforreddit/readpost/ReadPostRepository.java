package ml.docilealligator.infinityforreddit.readpost;


import android.content.SharedPreferences;
import android.os.Handler;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.synccit.Readdit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

@Singleton
public class ReadPostRepository {
    private final ReadPostDao mReadPostDao;
    private final Readdit mReaddit;
    private final SharedPreferences mCurrentAccountSharedPreferences;
    private final Executor mExecutor;

    @Inject
    public ReadPostRepository(RedditDataRoomDatabase redditDataRoomDatabase,
                              Readdit readdit,
                              @Named("current_account") SharedPreferences currentAccountSharedPreferences,
                              Executor executor) {
        mReadPostDao = redditDataRoomDatabase.readPostDao();
        mReaddit = readdit;
        mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
        mExecutor = executor;
    }

    public List<ReadPost> getAllBeforeReference(String referencePostId) {
        String username = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return new ArrayList<>();
        }

        ReadPost referenceReadPost = mReadPostDao.getReadPost(username, referencePostId);
        if (referenceReadPost == null) {
            return new ArrayList<>();
        }
        return mReadPostDao.getAllReadPosts(username, referenceReadPost.getTime());
    }

    public ListenableFuture<List<ReadPost>> getAllListenableFuture(Long before) {
        String username = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return Futures.immediateFuture(new ArrayList<>());
        }

        return mReadPostDao.getAllReadPostsListenableFuture(username, before);
    }

    public List<String> filterOutUnread(List<String> postIds) {
        String username = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return new ArrayList<>();
        }

        if (useReadditBackend()) {
            return mReaddit.getReadIds(postIds);
        } else {
            List<ReadPost> readPosts = mReadPostDao.getReadPostByIds(username, postIds);
            List<String> readPostIds = new ArrayList<>();
            for (int i = 0; i < readPosts.size(); i++) {
                readPostIds.add(readPosts.get(i).getId());
            }
            return readPostIds;
        }
    }

    public void insertAsync(String postId) {
        String username = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return;
        }

        mExecutor.execute(() -> {
            if (useReadditBackend()) {
                mReaddit.insertRead(postId);
            }
            mReadPostDao.insert(new ReadPost(username, postId));
        });
    }

    public void deleteAllAsync(Handler handler, DeleteAllReadPostsAsyncTaskListener deleteAllReadPostsAsyncTaskListener) {
        String username = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return;
        }

        mExecutor.execute(() -> {
            mReadPostDao.deleteAllReadPosts(username);
            handler.post(deleteAllReadPostsAsyncTaskListener::success);
        });
    }

    public interface DeleteAllReadPostsAsyncTaskListener {
        void success();
    }

    private boolean useReadditBackend() {
        // TODO add preference to switch between local and Readdit
        return true;
    }
}
