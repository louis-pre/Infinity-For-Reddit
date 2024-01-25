package ml.docilealligator.infinityforreddit;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

@Singleton
public class ReadPostsFilter {

    private final SharedPreferences currentAccountSharedPreferences;

    private final RedditDataRoomDatabase redditDataRoomDatabase;

    @Inject
    public ReadPostsFilter(RedditDataRoomDatabase redditDataRoomDatabase,
                           @Named("current_account") SharedPreferences currentAccountSharedPreferences) {
        this.currentAccountSharedPreferences = currentAccountSharedPreferences;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
    }

    public List<String> filter(List<String> postIds) {
        String username = currentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT);
        if (username.equals(Account.ANONYMOUS_ACCOUNT)) {
            return postIds;
        }

        List<ReadPost> allReadPosts = this.redditDataRoomDatabase.readPostDao().getAllReadPosts(username);
        HashSet<String> allReadPostIds = new HashSet<>();
        for (int i = 0; i < allReadPosts.size(); i++) {
            allReadPostIds.add(allReadPosts.get(i).getId());
        }

        List<String> readPostIds = new ArrayList<>();
        for (int i = 0; i < postIds.size(); i++) {
            String postId = postIds.get(i);
            if (allReadPostIds.contains(postId)) {
                readPostIds.add(postId);
            }
        }
        return readPostIds;
    }
}
