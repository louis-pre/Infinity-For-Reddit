package ml.docilealligator.infinityforreddit;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditData;

public class FetchPostFilterAndConcatenatedSubredditNames {

    public interface FetchPostFilterListener {
        void success(PostFilter postFilter);
    }

    public interface FetchPostFilterAndConcatenatedSubredditNamesListener {
        void success(PostFilter postFilter, String concatenatedSubredditNames);
    }

    public static void fetchPostFilter(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                       Handler handler, int postFilterUsage,
                                       String nameOfUsage, FetchPostFilterListener fetchPostFilterListener) {
        executor.execute(() -> {
            List<PostFilter> postFilters = redditDataRoomDatabase.postFilterDao().getValidPostFilters(postFilterUsage, nameOfUsage);
            PostFilter mergedPostFilter = PostFilter.mergePostFilter(postFilters);
            handler.post(() -> fetchPostFilterListener.success(mergedPostFilter));
        });
    }

    public static void fetchPostFilterAndConcatenatedSubredditNames(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                                   Handler handler, int postFilterUsage, String nameOfUsage,
                                                   FetchPostFilterAndConcatenatedSubredditNamesListener fetchPostFilterAndConcatenatedSubredditNamesListener) {
        executor.execute(() -> {
            List<PostFilter> postFilters = redditDataRoomDatabase.postFilterDao().getValidPostFilters(postFilterUsage, nameOfUsage);
            PostFilter mergedPostFilter = PostFilter.mergePostFilter(postFilters);
            List<SubscribedSubredditData> anonymousSubscribedSubreddits = redditDataRoomDatabase.subscribedSubredditDao().getAllSubscribedSubredditsList(Account.ANONYMOUS_ACCOUNT);
            if (anonymousSubscribedSubreddits != null && !anonymousSubscribedSubreddits.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (SubscribedSubredditData s : anonymousSubscribedSubreddits) {
                    stringBuilder.append(s.getName()).append("+");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                handler.post(() -> fetchPostFilterAndConcatenatedSubredditNamesListener.success(mergedPostFilter, stringBuilder.toString()));
            } else {
                handler.post(() -> fetchPostFilterAndConcatenatedSubredditNamesListener.success(mergedPostFilter, null));
            }
        });
    }

    public static void fetchPostFilterAndConcatenatedSubredditNames(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                                                    Handler handler, String multipath, int postFilterUsage, String nameOfUsage,
                                                                    FetchPostFilterAndConcatenatedSubredditNamesListener fetchPostFilterAndConcatenatedSubredditNamesListener) {
        executor.execute(() -> {
            List<PostFilter> postFilters = redditDataRoomDatabase.postFilterDao().getValidPostFilters(postFilterUsage, nameOfUsage);
            PostFilter mergedPostFilter = PostFilter.mergePostFilter(postFilters);
            List<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits = redditDataRoomDatabase.anonymousMultiredditSubredditDao().getAllAnonymousMultiRedditSubreddits(multipath);
            if (anonymousMultiredditSubreddits != null && !anonymousMultiredditSubreddits.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder();
                for (AnonymousMultiredditSubreddit s : anonymousMultiredditSubreddits) {
                    stringBuilder.append(s.getSubredditName()).append("+");
                }
                if (stringBuilder.length() > 0) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                handler.post(() -> fetchPostFilterAndConcatenatedSubredditNamesListener.success(mergedPostFilter, stringBuilder.toString()));
            } else {
                handler.post(() -> fetchPostFilterAndConcatenatedSubredditNamesListener.success(mergedPostFilter, null));
            }
        });
    }
}
