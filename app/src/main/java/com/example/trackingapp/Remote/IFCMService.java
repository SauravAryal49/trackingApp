package com.example.trackingapp.Remote;



import com.example.trackingapp.Model.MyResponse;
import com.example.trackingapp.Model.Request;


import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
                "Authorization:key=AAAAUIY1bcE:APA91bGlDKjHQRu4hzxLuzxcrWdQV4s_fE4VmlBsxPvXrXWy57ZWJz7WPlAh36WpcH2SzJiIrfjbG_cg2YooZtM6zoS8Dn8OO8rCAd-hHOmRQ1wyMdBlz77XtxMX1qpv88_w8eDM8pYD"


    })
    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser (@Body Request body);

}

