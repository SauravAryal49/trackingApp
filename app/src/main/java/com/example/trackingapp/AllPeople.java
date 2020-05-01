package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackingapp.Interface.iRecyclerItemClickListener;
import com.example.trackingapp.Model.MyResponse;
import com.example.trackingapp.Model.Request;
import com.example.trackingapp.Model.User;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.Toast;

import com.example.trackingapp.Interface.IFirebaseLoadDone;
import com.example.trackingapp.Remote.IFCMService;
import com.example.trackingapp.Utils.Common;
import com.example.trackingapp.ViewHolder.UserViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class AllPeople extends AppCompatActivity implements IFirebaseLoadDone {
    FirebaseRecyclerAdapter<User, UserViewHolder> adapter,searchAdapter;
    RecyclerView recyclerView;
    IFirebaseLoadDone firebaseLoadDone;

    MaterialSearchBar materialSearchBar;
    List<String> suggestList=new ArrayList<>();

    IFCMService ifcmService;

    //CompositeDisposable is a convenient class for bundling up multiple Disposables,
    // so that you can dispose them all with one method call on CompositeDisposable.
    CompositeDisposable compositeDisposable=new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);

        //init Api
        ifcmService=Common.getFCMService();

        //init view
        materialSearchBar=findViewById(R.id.search_bar);
        materialSearchBar.setCardViewElevation(20);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest=new ArrayList<>();
                for(String search:suggestList){
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())) {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){
                    if (adapter!=null){
                        //if close search, restore default
                        recyclerView.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        firebaseLoadDone=this;

        loadUserlist();
        loadsearchData();


    }

    private void loadUserlist(){
        Query query= FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull User user) {
                if(user.getEmail().equals(Common.loggedUser.getEmail())){
                    userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail())
                        .append("(me)"));
                    userViewHolder.textUserEmail.setTypeface(userViewHolder.textUserEmail.getTypeface(), Typeface.ITALIC);
                }else{
                    userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail()));

                }

                //event
                userViewHolder.setIrecyclerItemClickListener(new iRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                      //send friend request
                        showDialogRequest(user);
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_user,parent,false);
                return new UserViewHolder(itemView);
            }
        };

        //Don't forget this line if you don't want to load your all user blank
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    private void showDialogRequest(User user) {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(this,R.style.MyStyleRequest);
        alertDialog.setTitle("Request Friend").setMessage("Do you want to send request friend to"+user.getEmail());
        alertDialog.setIcon(R.drawable.ic_person_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Add to accept list
                DatabaseReference acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
                        .child(Common.loggedUser.getUid())
                        .child(Common.ACCEPT_LIST);

                acceptList.orderByKey().equalTo(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()==null) //if not in the friend list before
                                    sendFriendRequest(user);
                                else
                                    Toast.makeText(AllPeople.this, "You and "+user.getEmail()+"are already friends.", Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {


                            }
                        });
            }
        });

        alertDialog.show();

    }

    private void sendFriendRequest(User user) {
        //Get Token to be sent

        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference(Common.TOKENS);

        tokens.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null)
                    Toast.makeText(AllPeople.this, "Token error", Toast.LENGTH_SHORT).show();
                else{
                    //create Request
                    Request request=new Request();
                    request.setTo(dataSnapshot.child(user.getUid()).getValue(String.class));

                    //Create Data
                    Map<String,String> dataSend=new HashMap<>();
                    dataSend.put(Common.FROM_UID,Common.loggedUser.getUid());
                    dataSend.put(Common.FROM_NAME,Common.loggedUser.getEmail());
                    dataSend.put(Common.TO_UID,user.getUid());
                    dataSend.put(Common.TO_NAME,user.getEmail());

                    request.setTo(dataSnapshot.child(user.getUid()).getValue(String.class));
                    request.setData(dataSend);

                    Log.d("MyActivity", "checkpoint three ");
                    //send
                    compositeDisposable.add(ifcmService.sendFriendRequestToUser(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<MyResponse>() {
                        @Override
                        public void accept(MyResponse myResponse) throws Exception {
                            if(myResponse.success == 1) {
                                Log.d("MyActivity", "checkpoint one ");
                                Toast.makeText(AllPeople.this, "Request sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            Toast.makeText(AllPeople.this, "this is the error" +throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            Log.e("MyActivity", "checkpoint two");
                        }
                    }));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null)
            searchAdapter.stopListening();
        compositeDisposable.dispose();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(adapter!=null)
            adapter.startListening();
        if (searchAdapter!=null)
            searchAdapter.startListening();
    }

    private void loadsearchData(){
        List<String> lstUserEmail=new ArrayList<>();

        DatabaseReference userList=FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot:dataSnapshot.getChildren()){
                    User user=userSnapshot.getValue(User.class);
                    lstUserEmail.add(user.getEmail());
                }

                firebaseLoadDone.onFirebaseLoadUserNameDone(lstUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    public void startSearch(String text){
        Query query=FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .startAt(text);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        searchAdapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull User user) {
                if(user.getEmail().equals(Common.loggedUser.getEmail())){
                    userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail())
                            .append("(me)"));
                    userViewHolder.textUserEmail.setTypeface(userViewHolder.textUserEmail.getTypeface(), Typeface.ITALIC);
                }else{
                    userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail()));

                }
                //event
                userViewHolder.setIrecyclerItemClickListener(new iRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        //implement late
                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_user,parent,false);
                return new UserViewHolder(itemView);
            }
        };

        //Don't forget this line if you don't want to load your all user blank
        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);


    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {
        materialSearchBar.setLastSuggestions(lstEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, "error!"+message, Toast.LENGTH_SHORT).show();
    }
}
