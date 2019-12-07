package com.thanthi.dtnext.dtnextapplication.network;

import com.thanthi.dtnext.dtnextapplication.model.*;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface RetrofitInterface {

    @GET("api/menu/getmenu/")
    Call<List<MenuApi>> getMenuApi();

    @GET("api/article/GetHomePageDetails?topNewsSectionID=6&topNewsSectionCount=5&parentSectionId=1&count=1")
    Call<News> getHomeApi();

    @GET("api/article/GetHomePageDetails?topNewsSectionID=17&topNewsSectionCount=5&parentSectionId=11&count=1")
    Call<News> getLifeStyleApi();

    @GET("api/article/GetHomePageDetails?topNewsSectionID=17&topNewsSectionCount=5&parentSectionId=11&count=2")
    Call<News> getInnerLifeStyleApi();

    @GET("api/article/GetHomePageDetails?topNewsSectionID=6&topNewsSectionCount=5&parentSectionId=1&count=3")
    Call<News> getInnerNewsApi();

    @GET
    Call<News> getNewsForNewsFragment(@Url String url);

    @GET
    Call<News> getNewsForNotificationHubFragment(@Url String url);

    @GET("api/Article/GetSettings")
    Call<List<Settings>> getDataForSettingsFragment();

    @GET("api/Article/GetCategoryListForFeedback")
    Call<List<Settings>> getCategoryList();

    @POST("api/Article/UpdateFeedback")
    Call<ResponseBody> feedback(@Query(value = "name", encoded =  true) String name,
                                @Query(value = "email", encoded = true) String email,
                                @Query(value = "categoryType", encoded = true) String category,
                                @Query(value = "message", encoded = true) String comments);

    @GET
    Call<ResponseBody> toGetEndPoint(@Url String url);

    @GET("api/Article/GetAndroidVersion")
    Call<List<VersionUpdate>> getVersionCode();

    @GET
    Call<ResponseBody> toGetPushOnOff(@Url String url);

    @GET
    Call<Article> getArticleByArticleId(@Url String url);

    @GET("api/Article/Static")
    Call<List<NotificationHub>> getNotificationHubFeed();


}
