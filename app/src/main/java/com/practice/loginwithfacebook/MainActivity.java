package com.practice.loginwithfacebook;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private TextView tvResult, tvFirstName, tvLastName, tvEmail;
    private ImageView imgViewProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        /*LoginManager.getInstance().logOut();*/

        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));

        checkLoggedInStatus();

        printKeyHash();

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                tvResult.setText(
                        getString(R.string.user_id)
                                + loginResult.getAccessToken().getUserId()
                                + "\n" +
                                getString(R.string.auth_token)
                                + loginResult.getAccessToken().getToken()
                );
            }

            @Override
            public void onCancel() {
                tvResult.setText(R.string.login_attempt_canceled);
            }

            @Override
            public void onError(FacebookException e) {
                tvResult.setText(R.string.login_failed);
                Log.d("MyTag", e.toString());
            }
        });
    }

    /********************  onActivityResult  ***********************/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /********************  init views  ***********************/
    private void initViews() {
        loginButton = findViewById(R.id.login_button);
        tvResult = findViewById(R.id.tv_result);

        tvFirstName = findViewById(R.id.tv_first_name);
        tvLastName = findViewById(R.id.tv_last_name);
        tvEmail = findViewById(R.id.tv_email);

        imgViewProfileImage = findViewById(R.id.profile_image);
    }

    /********************  AccessTokenTacker ***********************/
    AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null) {
                tvFirstName.setText("");
                tvLastName.setText(null);
                tvEmail.setText(null);

                imgViewProfileImage.setImageResource(0);

                tvResult.setText(R.string.please_login);
            } else {
                loadUserProfile(currentAccessToken);
            }
        }
    };

    /******************** Load User Proile ***********************/
    private void loadUserProfile(AccessToken accessToken) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String firstName = object.getString("first_name");
                    String lastName = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");

                    String imageUrl = "https://graph.facebook.com/" + id + "/picture?type=large";
                    /*
                     * type=normal
                     *  type=large
                     * */

                    tvFirstName.setText(firstName);
                    tvLastName.setText(lastName);
                    tvEmail.setText(email);

                    Picasso.with(MainActivity.this)
                            .load(imageUrl)
                            .into(imgViewProfileImage);

                } catch (JSONException ignored) {

                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "first_name, last_name, email, id");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }

    /******************** onDestroy ***********************/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    /******************** onDestroy ***********************/
    private void checkLoggedInStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }

    private void printKeyHash() {
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.practice.loginwithfacebook", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("KeyHash:", e.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("KeyHash:", e.toString());
        }
    }
}

/*

C:\Program Files\Java\jdk1.8.0_40\bin>
*   (1)
keytool -exportcert -alias androiddebugkey -keystore "C:\Users\Let\.android\debug.keystore" | "E:\Android_Setups\openssl\bin\openssl" sha1 -binary | "E:\Android_Setups\openssl\bin\openssl" base64



   (2)
keytool -exportcert -alias YOUR_RELEASE_KEY_ALIAS -keystore YOUR_RELEASE_KEY_PATH | openssl sha1 -binary | openssl base64

keytool -exportcert -alias androiddebugkey -keystore C:\Users\Let\.android\debug.keystore | "E:\Android_Setups\openssl\bin\openssl" sha1 -binary | "E:\Android_Setups\openssl\bin\openssl" base64

Generated Key  : q1J9IkvaVrdPFD9UzXLxcybXtKY=
*/
