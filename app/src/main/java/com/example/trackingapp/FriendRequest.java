package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.trackingapp.Interface.IFirebaseLoadDone;
import com.example.trackingapp.Model.User;
import com.example.trackingapp.Utils.Common;
import com.example.trackingapp.ViewHolder.FriendRequestViewHolder;
import com.example.trackingapp.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class FriendRequest extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, FriendRequestViewHolder> adapter,searchAdapter;
    RecyclerView recyclerView;
    IFirebaseLoadDone firebaseLoadDone;

    MaterialSearchBar materialSearchBar;
    List<String> suggestList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);

        recyclerView=findViewById(R.id.recycler_view2);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        materialSearchBar=findViewById(R.id.search_bar2);
        materialSearchBar.setCardViewElevation(20);

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


        firebaseLoadDone=this;

        loadFriendRequestList();
        loadSearchData();
    }

    private void startSearch(String toString){
        Query query= FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.FRIEND_REQUEST)
                .startAt(toString);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        searchAdapter=new FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendRequestViewHolder friendRequestViewHolder, int i, @NonNull User user) {
                friendRequestViewHolder.txt_user_Email.setText(user.getEmail());
                friendRequestViewHolder.btn_accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFriendRequest(user,false);
                        addToAcceptList(user);
                        addUserToFriendContact(user);

                    }
                });

                friendRequestViewHolder.btn_reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Delete the friend request
                        deleteFriendRequest(user,true);
                    }
                });
            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_friend_request,parent,false);

                return new FriendRequestViewHolder(itemView);
            }
        };

        searchAdapter.startListening();
        recyclerView.setAdapter(searchAdapter);

    }

    private void loadFriendRequestList(){
        Query query= FirebaseDatabase.getInstance().getReference().child(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.FRIEND_REQUEST);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendRequestViewHolder friendRequestViewHolder, int i, @NonNull User user) {
                friendRequestViewHolder.txt_user_Email.setText(user.getEmail());
                friendRequestViewHolder.btn_accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteFriendRequest(user,false);
                        addToAcceptList(user);
                        addUserToFriendContact(user);

                    }
                });

                friendRequestViewHolder.btn_reject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Delete the friend request
                        deleteFriendRequest(user,true);
                    }
                });
            }

            @NonNull
            @Override
            public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView= LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_friend_request,parent,false);

                return new FriendRequestViewHolder(itemView);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void addUserToFriendContact(User user) {
        DatabaseReference acceptList=FirebaseDatabase.getInstance().getReference()
                .child(Common.USER_INFORMATION)
                .child(user.getUid())
                .child(Common.ACCEPT_LIST);

        acceptList.child(Common.loggedUser.getUid()).setValue(Common.loggedUser);
    }

    private void addToAcceptList(User user) {
        DatabaseReference acceptList=FirebaseDatabase.getInstance().getReference()
                .child(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        acceptList.child(user.getUid()).setValue(user);
    }

    private void deleteFriendRequest(final User user,final boolean isShowmessage){
        DatabaseReference friendRequest=FirebaseDatabase.getInstance().getReference()
                .child(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.FRIEND_REQUEST);

        friendRequest.child(user.getUid()).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(isShowmessage)
                            Toast.makeText(FriendRequest.this, "Request Removed ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null)
            searchAdapter.stopListening();
        super.onStop();
    }

    private void loadSearchData() {
        List<String> lstUserEmail = new ArrayList<>();

        DatabaseReference userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.FRIEND_REQUEST);

        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
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

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) {
        materialSearchBar.setLastSuggestions(lstEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, "error! "+message, Toast.LENGTH_SHORT).show();

    }
}
