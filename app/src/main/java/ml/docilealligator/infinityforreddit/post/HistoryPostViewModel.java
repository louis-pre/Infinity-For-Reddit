package ml.docilealligator.infinityforreddit.post;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPostRepository;
import retrofit2.Retrofit;

public class HistoryPostViewModel extends ViewModel {
    private final Executor executor;
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final int postType;
    private final PostFilter postFilter;
    private final ReadPostRepository readPostRepository;

    private final LiveData<PagingData<Post>> posts;

    private final MutableLiveData<PostFilter> postFilterLiveData;

    private final ParsePost parsePost;

    public HistoryPostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken,
                                @NonNull String accountName, int postType, PostFilter postFilter,
                                ParsePost parsePost, ReadPostRepository readPostRepository) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.postType = postType;
        this.postFilter = postFilter;
        this.parsePost = parsePost;
        this.readPostRepository = readPostRepository;

        postFilterLiveData = new MutableLiveData<>(postFilter);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(20, 10, false, 10), this::returnPagingSource);

        posts = Transformations.switchMap(postFilterLiveData, postFilterValue -> PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this)));
    }

    public LiveData<PagingData<Post>> getPosts() {
        return posts;
    }

    public HistoryPostPagingSource returnPagingSource() {
        HistoryPostPagingSource historyPostPagingSource;
        switch (postType) {
            case HistoryPostPagingSource.TYPE_READ_POSTS:
                historyPostPagingSource = new HistoryPostPagingSource(retrofit, executor,
                        accessToken, accountName, postType, postFilter, parsePost, readPostRepository);
                break;
            default:
                historyPostPagingSource = new HistoryPostPagingSource(retrofit, executor,
                        accessToken, accountName, postType, postFilter, parsePost, readPostRepository);
                break;
        }
        return historyPostPagingSource;
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Executor executor;
        private final Retrofit retrofit;
        private final String accessToken;
        private final String accountName;
        private final int postType;
        private final PostFilter postFilter;
        private final ParsePost parsePost;
        private final ReadPostRepository readPostRepository;

        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken,
                       @NonNull String accountName, int postType, PostFilter postFilter,
                       ParsePost parsePost, ReadPostRepository readPostRepository) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.postType = postType;
            this.postFilter = postFilter;
            this.parsePost = parsePost;
            this.readPostRepository = readPostRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == HistoryPostPagingSource.TYPE_READ_POSTS) {
                return (T) new HistoryPostViewModel(executor, retrofit, accessToken, accountName,
                        postType, postFilter, parsePost, readPostRepository);
            } else {
                return (T) new HistoryPostViewModel(executor, retrofit, accessToken, accountName,
                        postType, postFilter, parsePost, readPostRepository);
            }
        }
    }
}
