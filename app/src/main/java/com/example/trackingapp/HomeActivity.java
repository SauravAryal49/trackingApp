package com.example.trackingapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackingapp.Interface.IFirebaseLoadDone;
import com.example.trackingapp.Interface.iRecyclerItemClickListener;
import com.example.trackingapp.Model.User;
import com.example.trackingapp.Service.MyLocationReceiver;
import com.example.trackingapp.Utils.Common;
import com.example.trackingapp.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {
    FirebaseRecyclerAdapter<User, UserViewHolder> adapter,searchAdapter;
    RecyclerView recyclerView;
    IFirebaseLoadDone firebaseLoadDone;

    MaterialSearchBar materialSearchBar;
    List<String> suggestList=new ArrayList<>();
    DatabaseReference publicLocation;

    LocationRequest locationRequest;
    FusedLocationProviderClient mfusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.open,R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView=navigationView.getHeaderView(0);
        TextView loggedUserEmail=headerView.findViewById(R.id.logged_user_email);
        loggedUserEmail.setText(Common.loggedUser.getEmail());


        //View
        materialSearchBar=findViewById(R.id.search_bar3);
        materialSearchBar.setCardViewElevation(20);
        recyclerView=findViewById(R.id.recycler_friend_list);
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

        recyclerView=findViewById(R.id.recycler_friend_list);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        //Update Location
        publicLocation=FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
        updateLocation();

        firebaseLoadDone=this;

        loadFriendList();
        loadSearchData();

    }

    private void loadSearchData() {
        List<String> lstUserEmail = new ArrayList<>();

        DatabaseReference userList = FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

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

    private void loadFriendList() {
        Query query=FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        adapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull User user) {
                userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail()));

                userViewHolder.setIrecyclerItemClickListener(new iRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        //Show tracking
                        Toast.makeText(HomeActivity.this, "checkpoint", Toast.LENGTH_SHORT).show();
                        Common.trackingUser=user;
                        startActivity(new Intent(getApplicationContext(),MapsActivity.class));

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

        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null)
            searchAdapter.stopListening();
        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(adapter!=null)
            adapter.startListening();
        if(searchAdapter!=null)
            searchAdapter.startListening();

    }

    private void updateLocation() {
        BuildLocationRequest();
        mfusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "You must provide the permission to continue ", Toast.LENGTH_SHORT).show();
            return;
        }
        mfusedLocationProviderClient.requestLocationUpdates(locationRequest,getPendingIntent());

    }

    private PendingIntent getPendingIntent() {
        Intent intent =new Intent(HomeActivity.this, MyLocationReceiver.class);
        intent.setAction(MyLocationReceiver.ACTION);
        return PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void BuildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setFastestInterval(3000);
        locationRequest.setInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void startSearch(String toString) {
        Query query= FirebaseDatabase.getInstance()
                .getReference(Common.USER_INFORMATION)
                .child(Common.loggedUser.getUid())
                .child(Common.ACCEPT_LIST)
                .orderByChild("name")
                .startAt(toString);

        FirebaseRecyclerOptions<User> options=new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        searchAdapter=new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull User user) {
                userViewHolder.textUserEmail.setText(new StringBuilder(user.getEmail()));

                userViewHolder.setIrecyclerItemClickListener(new iRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        Common.trackingUser=user;
                        startActivity(new Intent(getApplicationContext(),MapsActivity.class));

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
    public void onBackPressed() {
        DrawerLayout drawer=findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
        super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        //Handle navigation view item click here
        int id = item.getItemId();
        if (id == R.id.nav_find_people) {
            Toast.makeText(this, "explore people is selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this,AllPeople.class));

        } else if (id == R.id.add_people) {
            startActivity(new Intent(getApplicationContext(),FriendRequest.class));
        } else if (id == R.id.sign_out) {
            Toast.makeText(this, "This service not available at the moment. Will be available soon", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
