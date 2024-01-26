package ml.docilealligator.infinityforreddit.readpost;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

@Singleton
public class ReadPosts {

    private final RedditDataRoomDatabase redditDataRoomDatabase;
    private final SharedPreferences currentAccountSharedPreferences;
    private final Executor executor;

    @Inject
    public ReadPosts(RedditDataRoomDatabase redditDataRoomDatabase,
                     @Named("current_account") SharedPreferences currentAccountSharedPreferences,
                     Executor executor) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.currentAccountSharedPreferences = currentAccountSharedPreferences;
        this.executor = executor;
    }

    public List<String> filterOutUnread(List<String> postIds) {
        String username = currentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return postIds;
        }

        List<ReadPost> readPosts = this.redditDataRoomDatabase.readPostDao().getReadPostByIds(username, postIds);

        List<String> readPostIds = new ArrayList<>();
        for (int i = 0; i < readPosts.size(); i++) {
            readPostIds.add(readPosts.get(i).getId());
        }
        return readPostIds;
    }

    public void insertAsync(String postId) {
        String username = currentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT) || username.equals("")) {
            return;
        }

        this.executor.execute(() -> {
            this.redditDataRoomDatabase.readPostDao().insert(new ReadPost(username, postId));
        });
    }
}
