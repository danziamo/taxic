package taxi.city.citytaxiclient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import taxi.city.citytaxiclient.core.User;
import taxi.city.citytaxiclient.service.ApiService;
import taxi.city.citytaxiclient.utils.Helper;


public class ConfirmSignUpActivity extends Activity {

    private EditText mActivationCode;
    private EditText mPasswordField;
    private ActivateTask task = null;
    SweetAlertDialog pDialog;
    private boolean isSignUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_sign_up);
        Initialize();
    }

    private void Initialize() {
        mActivationCode = (EditText) findViewById(R.id.etActivationCode);
        mPasswordField = (EditText) findViewById(R.id.etActivationPassword);

        isSignUp = getIntent().getBooleanExtra("SIGNUP", true);
        if (isSignUp) mPasswordField.setVisibility(View.INVISIBLE);
        Button btn = (Button) findViewById(R.id.btnActivate);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activate();
            }
        });
    }

    private void activate() {
        if (task != null) {
            return;
        }

        String mCode = mActivationCode.getText().toString();
        String mPassword = mPasswordField.getText().toString();
        View focusView = null;
        boolean cancel = false;

        if (mCode.length() > 5) {
            mActivationCode.setError("Код не более 5 символов");
            focusView = mActivationCode;
            cancel = true;
        }

        if (!isSignUp && mPassword.length() < 5) {
            mPasswordField.setError("Пароль минимум 5 символа");
            focusView = mPasswordField;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            task = new ActivateTask(mCode, mPassword);
            task.execute((Void) null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (show) {
            pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper()
                    .setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Активация");
            pDialog.setCancelable(true);
            pDialog.show();
        } else {
            pDialog.dismissWithAnimation();
        }
    }

    private class ActivateTask extends AsyncTask<Void, Void, JSONObject> {

        private final String mCode;
        private final String mPassword;
        private JSONObject json = new JSONObject();

        ActivateTask(String code, String password) {
            mCode = code;
            mPassword = password;

            try {
                json.put("phone", User.getInstance().phone);
                json.put("password", isSignUp ? User.getInstance().password : mPassword);
                json.put("activation_code", mCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            if (isSignUp) {
                return ApiService.getInstance().activateRequest(json, "activate/");
            } else {
                return ApiService.getInstance().putRequest(json, "reset_password/");
            }
        }

        @Override
        protected void onPostExecute(final JSONObject result) {
            task = null;
            showProgress(false);
            try {
                if (Helper.isSuccess(result)) {
                    User.getInstance().setUser(result);
                    Finish();
                } else {
                    Toast.makeText(ConfirmSignUpActivity.this, "Не удалось активировать пользователя", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            task = null;
            showProgress(false);
        }
    }

    private void Finish() {
        /*if (isSignUp) {
            Intent intent = new Intent(this, MapsActivity.class);
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(intent);
        }*/
        new SweetAlertDialog(ConfirmSignUpActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Успешно")
                .setContentText(null)
                .setConfirmText("Ок")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        finish();
                    }
                })
                .show();
    }
}
