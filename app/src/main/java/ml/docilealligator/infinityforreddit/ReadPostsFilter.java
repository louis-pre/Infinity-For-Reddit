package ml.docilealligator.infinityforreddit;
import android.content.SharedPreferences;

import java.util.ArrayList;
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

        List<ReadPost> readPosts = this.redditDataRoomDatabase.readPostDao().getReadPostByIds(postIds);

        List<String> readPostIds = new ArrayList<>();
        for (int i = 0; i < readPosts.size(); i++) {
            readPostIds.add(readPosts.get(i).getId());
        }
        return readPostIds;
    }
}
