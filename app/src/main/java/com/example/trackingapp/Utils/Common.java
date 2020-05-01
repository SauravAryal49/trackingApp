package com.example.trackingapp.Utils;

import com.example.trackingapp.Model.User;
import com.example.trackingapp.Remote.IFCMService;

import com.example.trackingapp.Remote.RetrofitClient;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Common {
    public static final String USER_INFORMATION = "UsersInformation";
    public static final String USER_UID_SAVE_KEY = "SaveUID";
    public static final String TOKENS ="TOKENS" ;
    public static final String FROM_NAME = "FromName";
    public static final String ACCEPT_LIST = "acceptList";
    public static final String FROM_UID ="FromUid";
    public static final String TO_UID ="ToUid" ;
    public static final String TO_NAME ="ToName" ;
    public static final String FRIEND_REQUEST ="FriendRequest";
    public static final String PUBLIC_LOCATION ="PublicLocation";

    public static User loggedUser;
    public static User trackingUser;

    public static IFCMService getFCMService(){
        return RetrofitClient.getClient("https://fcm.googleapis.com/")
                .create(IFCMService.class);
    }

    public static Date convertTimeStampToDate(long time) {
        return new Date(new Timestamp(time).getTime());
    }

    public static String getDataFormatted(Date date) {
        return new SimpleDateFormat("dd-mm-yyyy HH:mm").format(date).toString();
    }
}
