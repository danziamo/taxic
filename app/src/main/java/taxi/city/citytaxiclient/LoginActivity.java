package taxi.city.citytaxiclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxiclient.core.Order;
import taxi.city.citytaxiclient.core.User;
import taxi.city.citytaxiclient.service.ApiService;
import taxi.city.citytaxiclient.utils.Helper;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private static final String url = "http://81.88.192.37/api/v1/";
    private static final String PREFS_NAME = "MyPrefsFile";
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mPhoneView;
    private TextView mPhoneExtraView;
    private EditText mPasswordView;
    private User user = User.getInstance();
    private ApiService api = ApiService.getInstance();
    private int statusCode;
    private SweetAlertDialog pDialog;

    private SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mPhoneView = (EditText) findViewById(R.id.login_phone);
        mPhoneExtraView = (TextView) findViewById(R.id.textViewPhoneExtra);
        //MaskedWatcher maskedWatcher = new MaskedWatcher("+996 (###) ###-###", mPhoneView);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login_phone || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        setPreferences();

        Button mPhoneSignInButton = (Button) findViewById(R.id.btnSignIn);
        Button mSignUpButton = (Button) findViewById(R.id.btnSignUp);
        mPhoneSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mSignUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }

    private void setPreferences() {
        settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.contains("phoneKey")) {
            String phone = settings.getString("phoneKey", "");
            mPhoneView.setText(phone.substring(4, phone.length()));
            if (settings.contains("passwordKey")) {
                mPasswordView.setText(settings.getString("passwordKey", ""));
            }
        }
        if (settings.contains("orderIdKey")) {
            Order.getInstance().id = Integer.valueOf(settings.getString("orderIdKey", ""));
        }
    }

    private void savePreferences(User user) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("phoneKey", mPhoneExtraView.getText().toString() + mPhoneView.getText().toString());
        editor.putString("passwordKey", mPasswordView.getText().toString());
        editor.putString("tokenKey", user.getToken());
        api.setToken(user.getToken());

        editor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Остутствует интернет", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reset errors.
        mPhoneView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String phone = mPhoneExtraView.getText().toString() + mPhoneView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(phone)) {
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        } else if (!isEmailValid(phone)) {
            mPhoneView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(phone, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        //return email.contains("@");
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */

    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Авторизация");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhone;
        private final String mPassword;

        UserLoginTask(String phone, String password) {
            mPhone = phone;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            // Simulate network access.
            boolean res = false;
            try {
                JSONObject json = new JSONObject();
                user.phone = mPhone;
                user.password = mPassword;
                json.put("phone", mPhone);
                json.put("password", mPassword);
                JSONObject object = api.loginRequest(json, "login/");
                if (Helper.isSuccess(object)) {
                    user.setUser(object);
                    statusCode = 200;
                    res = true;
                    JSONObject onlineStatus = new JSONObject();
                    onlineStatus.put("online_status", "online");
                    onlineStatus = api.patchRequest(onlineStatus, "users/" + String.valueOf(user.id)+ "/");
                    JSONObject orderResult = api.getArrayRequest("orders/?status=new&ordering=-id&limit=1&client=" + String.valueOf(user.id));
                    if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                        Order.getInstance().id = orderResult.getJSONArray("result").getJSONObject(0).getInt("id");
                    }
                    orderResult = api.getArrayRequest("orders/?status=accepted&ordering=-id&limit=1&client=" + String.valueOf(user.id));
                    if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                        Order.getInstance().id = orderResult.getJSONArray("result").getJSONObject(0).getInt("id");
                    }
                    orderResult = api.getArrayRequest("orders/?status=waiting&ordering=-id&limit=1&client=" + String.valueOf(user.id));
                    if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                        Order.getInstance().id = orderResult.getJSONArray("result").getJSONObject(0).getInt("id");
                    }
                    orderResult = api.getArrayRequest("orders/?status=ontheway&ordering=-id&limit=1&client=" + String.valueOf(user.id));
                    if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                        Order.getInstance().id = orderResult.getJSONArray("result").getJSONObject(0).getInt("id");
                    }
                    orderResult = api.getArrayRequest("orders/?status=pending&ordering=-id&limit=1&client=" + String.valueOf(user.id));
                    if (Helper.isSuccess(orderResult) && orderResult.getJSONArray("result").length() > 0) {
                        Order.getInstance().id = orderResult.getJSONArray("result").getJSONObject(0).getInt("id");
                    }
                } else if (Helper.isBadRequest(object)) {
                    res = true;
                    statusCode = 403;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("LoginActivity", Arrays.toString(e.getStackTrace()));
                res = false;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("LoginActivity", Arrays.toString(e.getStackTrace()));
                res = false;
            }
            return res;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success && statusCode == HttpStatus.SC_OK) {
                savePreferences(user);
                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                startActivity(intent);
                finish();
            } else  if (statusCode == 403) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            else {
                Toast.makeText(getApplicationContext(), "Сервис недоступен", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void signUp() {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra("NEW", true);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (data != null && data.getExtras() != null)
                Toast.makeText(getApplicationContext(), data.getExtras().getString("MESSAGE"), Toast.LENGTH_LONG).show();
        }
    }
}



