package com.example.fred_liu.pulsr.Me;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fred_liu.pulsr.MainActivity;
import com.example.fred_liu.pulsr.R;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.fred_liu.pulsr.model.Response;
import com.example.fred_liu.pulsr.network.NetworkUtil;
import com.example.fred_liu.pulsr.utils.Constants;
import com.example.fred_liu.pulsr.model.User;
import java.io.IOException;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.example.fred_liu.pulsr.utils.Validation.validateFields;


public class ChangepasswordFragment extends DialogFragment {
    public interface Listener {

        void onPasswordChanged();
    }

    public static final String TAG = ChangepasswordFragment.class.getSimpleName();

    private EditText mEtOldPassword;
    private EditText mEtNewPassword;
    private Button mBtChangePassword;
    private Button mBtCancel;
    private TextView mTvMessage;
    private TextInputLayout mTiOldPassword;
    private TextInputLayout mTiNewPassword;
    private ProgressBar mProgressBar;

    private CompositeSubscription mSubscriptions;

    private String mToken;
    private String mEmail;

    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_changepassword,container,false);
        mSubscriptions = new CompositeSubscription();
        getData();
        initViews(view);
        return view;
    }

    private void getData() {

        Bundle bundle = getArguments();

        mToken = bundle.getString(Constants.TOKEN);
        mEmail = bundle.getString(Constants.EMAIL);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainActivity)context;
    }

    private void initViews(View v) {

        mEtOldPassword = (EditText) v.findViewById(R.id.et_old_password);
        mEtNewPassword = (EditText) v.findViewById(R.id.et_new_password);
        mTiOldPassword = (TextInputLayout) v.findViewById(R.id.ti_old_password);
        mTiNewPassword = (TextInputLayout) v.findViewById(R.id.ti_new_password);
        mTvMessage = (TextView) v.findViewById(R.id.tv_message);
        mBtChangePassword = (Button) v.findViewById(R.id.btn_change_password);
        mBtCancel = (Button) v.findViewById(R.id.btn_cancel);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progress);

        mBtChangePassword.setOnClickListener(view -> changePassword());
        mBtCancel.setOnClickListener(view -> dismiss());
    }

    private void changePassword() {

        setError();

        String oldPassword = mEtOldPassword.getText().toString();
        String newPassword = mEtNewPassword.getText().toString();

        int err = 0;

        if (!validateFields(oldPassword)) {

            err++;
            mTiOldPassword.setError("Password should not be empty !");
        }

        if (!validateFields(newPassword)) {

            err++;
            mTiNewPassword.setError("Password should not be empty !");
        }

        if (err == 0) {

            User user = new User();
            user.setPassword(oldPassword);
            user.setNewPassword(newPassword);
            changePasswordProgress(user);
            mProgressBar.setVisibility(View.VISIBLE);

        }
    }

    private void setError() {

        mTiOldPassword.setError(null);
        mTiNewPassword.setError(null);
    }

    private void changePasswordProgress(User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit(mToken).changePassword(mEmail,user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {

        mProgressBar.setVisibility(View.GONE);
        mListener.onPasswordChanged();
        dismiss();
    }

    private void handleError(Throwable error) {

        mProgressBar.setVisibility(View.GONE);

        if (error instanceof HttpException) {

            Gson gson = new GsonBuilder().create();

            try {

                String errorBody = ((HttpException) error).response().errorBody().string();
                Response response = gson.fromJson(errorBody,Response.class);
                showMessage(response.getMessage());

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {

            showMessage("Network Error !");
        }
    }

    private void showMessage(String message) {

        mTvMessage.setVisibility(View.VISIBLE);
        mTvMessage.setText(message);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }
}
