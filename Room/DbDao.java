package com.thanthi.dtnext.dtnextapplication.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.thanthi.dtnext.dtnextapplication.model.*;

import java.util.List;

@Dao
public interface DbDao {

    // Db for Menu Api
    @Insert
    void insertMenuData(List<MenuApi> menuApi);

    @Query("SELECT * FROM tableMenu")
    List<MenuApi> getMenu();

    @Query("DELETE FROM tableMenu")
    void deleteMenu();



    //Db for Home fragment
    @Insert
    void insertApiData(List<Article> articleList);

    @Query("SELECT * FROM tableArticle WHERE titleOfAccess LIKE :title")
    List<Article> getArticleList(String title);

    @Query("DELETE FROM tableArticle WHERE titleOfAccess = :title")
    void deleteByTitleOfAccess(String title);



    // db for favourites
    @Insert
    void insertFavouriteArticle(Favourites favourites);

    @Query("SELECT COUNT(*) FROM tableFavourite WHERE articleId LIKE :articleId")
    int isInFavourites(String articleId);

    @Query("SELECT * FROM tableFavourite")
    List<Favourites> getFavouriteArticleList();

    @Query("DELETE FROM tableFavourite WHERE articleId LIKE :articleId")
    void deleteFavourites(String articleId);

    @Query("SELECT COUNT(*) FROM tableFavourite")
    int totalCountInFavourites();

    @Query("SELECT articleId FROM tableFavourite")
    List<String> allFavouriteId();



    //db for news fragment
    @Insert
    void insertNewsList(List<Article> articleList);

    @Query("SELECT * FROM tableArticle WHERE childSectionName LIKE :subTitle AND titleOfAccess LIKE :titleOfAccess")
    List<Article> getNewsArticleListSelection(String subTitle, String titleOfAccess);

    @Query("DELETE FROM tablearticle WHERE childSectionName LIKE :subTitle AND titleOfAccess LIKE :titleOfAccess")
    void deleteNewsArticle(String subTitle, String titleOfAccess);



    //db for notification hub pager
    @Insert
    void insertHubList(List<Hub> hubList);

    @Query("SELECT * FROM tableHub WHERE titleOfAccess LIKE :titleOfAccess")
    List<Hub> getNotificationHubArticleList(String titleOfAccess);

    @Query("DELETE FROM tableHub WHERE titleOfAccess LIKE :titleOfAccess")
    void deleteNotificationHubArticle(String titleOfAccess);



    //db for notification hub fragment
    @Insert
    void insertNotificationHubFeed(List<NotificationHub> notificationHubList);

    @Query("SELECT * FROM tableNotificationHub")
    List<NotificationHub> getNotificationHubList();

    @Query("DELETE FROM tableNotificationHub")
    void deleteNotificationHubFeed();

    // db for support settings
    @Insert
    void insertSupport(List<Settings> settingsList);

    @Query("SELECT * FROM tableSupport")
    List<Settings> getSupportList();

    @Query("DELETE FROM tableSupport")
    void deleteTableSupport();

}
