package com.example.travel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;

import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;

import java.util.Arrays;


public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private SignInButton signInButton;
    private GoogleSignInClient mgoogleSignInClient;
    private String TAG = "SignUpActivity";
    private Button btnSignOut;
    private int RC_SIGN_IN =1;
    EditText emailText,passwordText;

    private CallbackManager callbackManager;
    private TextView TextViewUser;
    private ImageView mlogo;
    private LoginButton loginButton;
    private FirebaseAuth.AuthStateListener authStateListener;
    private AccessTokenTracker accessTokenTracker;
    private static final String EMAIL = "email";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        emailText=findViewById(R.id.emailText);
        passwordText=findViewById(R.id.passwordText);


        TextViewUser=findViewById(R.id.text_user);
        mlogo =findViewById(R.id.image_logo);
        loginButton=findViewById(R.id.login_button);

        loginButton.setReadPermissions("email","public_profile");
        callbackManager = CallbackManager.Factory.create();
        FacebookSdk.sdkInitialize(getApplicationContext());

        FirebaseUser firebaseUser =firebaseAuth.getCurrentUser();


        if(firebaseUser !=null) {
            Intent intent = new Intent(SignUpActivity.this, FeedActivity.class);
            startActivity(intent);
            finish();

        }

        signInButton =findViewById(R.id.signInButton);
        btnSignOut =findViewById(R.id.button4);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mgoogleSignInClient =GoogleSignIn.getClient(this,gso);


       signInButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               signIn();
           }
       });

btnSignOut.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        mgoogleSignInClient.signOut();
        Toast.makeText(SignUpActivity.this,"signed In Successful",Toast.LENGTH_SHORT).show();
        btnSignOut.setVisibility(v.INVISIBLE);
    }
});







        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"onSuccess"+ loginResult);
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"onError",error);
            }
        });

       authStateListener = new FirebaseAuth.AuthStateListener() {
          @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    updateU(user);

                }
                else{
                    updateU(null);
                }
            }
        };
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken !=null){
                    firebaseAuth.signOut();
                }
            }
        };



















    }
    public void  signIn(){
        Intent signInIntent=mgoogleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent,RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private  void handleSignInResult(Task<GoogleSignInAccount> completeedTask){
        try{
            GoogleSignInAccount acc =completeedTask.getResult(ApiException.class);
            Toast.makeText(SignUpActivity.this,"signed In Successful",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(acc);
        }
        catch (ApiException e){
            Toast.makeText(SignUpActivity.this,"signed In Unsuccessful",Toast.LENGTH_SHORT).show();
            FirebaseGoogleAuth(null);
        }

    }
private void FirebaseGoogleAuth(GoogleSignInAccount acct){

        AuthCredential authCredential =GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(SignUpActivity.this,FeedActivity.class);
                    startActivity(intent);
                    Toast.makeText(SignUpActivity.this," Successful",Toast.LENGTH_SHORT).show();
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    updateUI(user);
                }
                else{
                    Toast.makeText(SignUpActivity.this,"failed",Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        });
}

private void updateUI(FirebaseUser fUser){
        btnSignOut.setVisibility(View.VISIBLE);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if(account != null){
            String personName =account.getDisplayName();
            String personGivenName =account.getGivenName();
            String personFamilyName = account.getFamilyName();
            String personEmail =account.getEmail();
            String personId =account.getId();
            Uri personPhoto = account.getPhotoUrl();

            Toast.makeText(SignUpActivity.this,personName + personEmail,Toast.LENGTH_SHORT).show();
        }
}





    public void signInClicked(View view){
        String email =emailText.getText().toString();
        String password=passwordText.getText().toString();


        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent(SignUpActivity.this,FeedActivity.class);
                startActivity(intent);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();

            }
        });


    }





    public void signUpClicked(View view){
        String email =emailText.getText().toString();
        String password=passwordText.getText().toString();


        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Toast.makeText(SignUpActivity.this,"user Created",Toast.LENGTH_LONG).show();


             /*   Intent intent = new Intent(SignUpActivity.this,FeedActivity.class);
                startActivity(intent);*/

            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
            }
        });

    }



    private void handleFacebookToken(AccessToken token){
        Log.d(TAG,"handleFacebookToken"+ token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"sign in with credential");

                    FirebaseUser user =firebaseAuth.getCurrentUser();
                    updateU(user);

                }else{
                    Log.d(TAG,"sign in with credential:failure",task.getException());
                    Toast.makeText(SignUpActivity.this,"Auth failed",Toast.LENGTH_SHORT).show();
                    updateU(null);
                }
            }
        });
    }


    private void updateU(FirebaseUser user){

        if(user != null){

            TextViewUser.setText(user.getDisplayName());

            if(user.getPhotoUrl() !=null){
                String photoUrl =user.getPhotoUrl().toString();
                photoUrl=photoUrl +" ?type=large";
                Picasso.get().load(photoUrl).into(mlogo);
            }

        }
        else{
            TextViewUser.setText(" ");
            mlogo.setImageResource(R.drawable.indir);
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authStateListener);
        FirebaseUser  user = firebaseAuth.getCurrentUser();
        if(user != null){
            updateU(user);
        }
    }


  protected void onStop() {
        super.onStop();
        if(authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

}
