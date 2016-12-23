package com.anudeesh.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private LoginButton mFacebookLoginButton;
    private SignInButton mGoogleLoginButton;
    private Button mPasswordLoginButton, mSignupButton;
    private EditText emailField, pwdField;

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private GoogleApiClient mGoogleApiClient;
    private boolean mGoogleIntentInProgress;
    private boolean mGoogleLoginClicked;
    private ConnectionResult mGoogleConnectionResult;

    private AuthData mAuthData;
    private Firebase mFirebaseRef;
    private Firebase.AuthStateListener mAuthStateListener;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UploadTask uploadTask;

    private CallbackManager mCallbackManager;
    private AccessTokenTracker mFacebookAccessTokenTracker;

    private ProgressDialog mProgressDialog;
    boolean flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        mFacebookLoginButton = (LoginButton) findViewById(R.id.fb_login_button);
        mGoogleLoginButton = (SignInButton) findViewById(R.id.google_login_button);
        mPasswordLoginButton = (Button) findViewById(R.id.email_login_button);
        mSignupButton = (Button) findViewById(R.id.buttonCreate);
        emailField = (EditText) findViewById(R.id.editTextEmailVal);
        pwdField = (EditText) findViewById(R.id.editTextPwdVal);
        String onlogout="";

        mGoogleLoginButton.setOnClickListener(this);
        mSignupButton.setOnClickListener(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //mFacebookLoginButton.setOnClickListener(this);

        onlogout = getIntent().getStringExtra("logout");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("google", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d("google", "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton.setReadPermissions("email", "public_profile");
        mFacebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("fb", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("fb", "facebook:onCancel");
                updateUI(null);
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("fb", "facebook:onError", error);
                updateUI(null);
            }
        });

        mPasswordLoginButton.setOnClickListener(this);

        if(onlogout!=null && onlogout!="") {

        }

        String x = getIntent().getStringExtra("PROVIDER");
        if (x!=null && x!="") {
           if(x.equals("facebook")) {
                LoginManager.getInstance().logOut();
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_login_button:
                googleSignIn();
                break;
            case R.id.email_login_button:
                String eval = emailField.getText().toString();
                String pval = pwdField.getText().toString();
                emailSignIn(eval,pval);
                break;
            case R.id.buttonCreate:
                Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void emailSignIn(String email, String password) {
        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Enter both email and password",
                    Toast.LENGTH_SHORT).show();
        } else {
            showProgressDialog();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                //Log.w(TAG, "signInWithEmail:failed", task.getException());
                                Toast.makeText(MainActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                onLogin();
                            }
                            hideProgressDialog();

                        }
                    });
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("login", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                updateUI(null);
            }
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("google", "firebaseAuthWithGoogle:" + acct.getId());
        //Log.d("google", "firebaseAuthWithGoogle:" + acct.getIdToken());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("google", "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("login", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            User googleUser = new User(acct.getGivenName(),acct.getFamilyName(),"",acct.getPhotoUrl().toString(),acct.getEmail());
                            String uid = getUid();
                            String key = mDatabase.child("users").push().getKey();
                            Map<String, Object> userValues = googleUser.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + uid, userValues);
                            mDatabase.updateChildren(childUpdates);

                        }
                        hideProgressDialog();
                        onLogin();
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("fb", "handleFacebookAccessToken:" + token);
        showProgressDialog();
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("fb", "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("fb", "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Profile profile = Profile.getCurrentProfile();
                            //Profile fbProfile = Profile.getCurrentProfile();
                            String fbemail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                            final User fbUser = new User();
                            fbUser.setDp(profile.getProfilePictureUri(80, 80).toString());
                            fbUser.setFname(profile.getFirstName());
                            fbUser.setLname(profile.getLastName());
                            fbUser.setEmail(fbemail);
                            fbUser.setGender("");

                            final String uid = getUid();
                            String key = mDatabase.child("users").push().getKey();

                            Map<String, Object> userValues = fbUser.toMap();
                            Map<String, Object> childUpdates = new HashMap<>();
                            childUpdates.put("/users/" + uid, userValues);
                            mDatabase.updateChildren(childUpdates);
                        }
                        hideProgressDialog();
                        onLogin();
                    }
                });
    }


    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void onLogin() {
        Intent intent = new Intent(MainActivity.this,UserActivity.class);
        startActivity(intent);
    }
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        String x = UserActivity.provider;
        if (x!=null && x!="") {
           /* if (x.equals("google")) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
            } else*/ if(x.equals("facebook")) {
                LoginManager.getInstance().logOut();
            }
        }
    }
}
