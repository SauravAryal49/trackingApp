package com.example.trackingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.trackingapp.Model.User;
import com.example.trackingapp.Utils.Common;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

import de.javakaffee.kryoserializers.ArraysAsListSerializer;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    DatabaseReference user_information;
    private static final int MY_REQUEST_CODE=12;
    List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);

        //init firebase
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        //init provider
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        //request permission location
        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        showSignInOption();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "you must grant the permission to continue", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError dexterError) {
                Log.d("Dexter","error!"+dexterError.toString());
            }
        }).check();
    }



    private void showSignInOption(){
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),MY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MY_REQUEST_CODE){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){
                final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
                //check if user is present or not in the database
                user_information.orderByKey()
                        .equalTo(firebaseUser.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()==null) {
                                    //means user is not present
                                    if (!dataSnapshot.child(firebaseUser.getUid()).exists()){
                                        //means keyID is also not present
                                        Common.loggedUser=new User(firebaseUser.getUid(),firebaseUser.getEmail());

                                        //Add to the database
                                        user_information.child(Common.loggedUser.getUid())
                                                .setValue(Common.loggedUser);
                                    }
                                } else{
                                        //if user is available
                                        Common.loggedUser=dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);
                                    }

                                    //save UID to storage to update location from background
                                    Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                    updateToken(firebaseUser);
                                    setUpUI();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
        }
    }

    private void setUpUI() {
        //Navigate home
        startActivity(new Intent(MainActivity.this,HomeActivity.class));
        finish();
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference tokens=FirebaseDatabase.getInstance()
                .getReference(Common.TOKENS);

        //Got token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        tokens.child(firebaseUser.getUid())
                                .setValue(instanceIdResult.getToken());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "error!"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
