package com.liu.qqssologin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    @Bind(R.id.iv_icon1)
    ImageView mIvIcon1;
    @Bind(R.id.iv_icon2)
    ImageView mIvIcon2;
    @Bind(R.id.iv_icon3)
    ImageView mIvIcon3;
    @Bind(R.id.iv_icon4)
    ImageView mIvIcon4;
    private String mAppid = "222222";
    ;
    private Tencent mTencent;

    private LoginListener mLoginListener;
    private UserInfo      mInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initTencent();
        /**
         * 第三方登录四步曲
         * 1. 发起授权请求,用户点击登录
         * 2.获取Token
         * 3.向自己的服务器发送Token
         * 4.缓存tokenKey/sessionKey到SharedPrerence
         * //tokenKey/sessionKey
         *
         */
    }

    private void initTencent() {

        mTencent = Tencent.createInstance(mAppid, this);
        if (mLoginListener == null) {
            mLoginListener = new LoginListener();
        }
    }

    /**
     * QQ客户端登录
     *
     * @param v
     */
    public void qqssologin(View v) {

        mTencent.login(this, "all", mLoginListener);

    }

    private class LoginListener implements IUiListener {

        /**
         * 完成登录
         *
         * @param obj fsd
         */
        @Override
        public void onComplete(Object obj) {


            JSONObject jsonObject = (JSONObject) obj;

            String token   = jsonObject.optString(Constants.PARAM_ACCESS_TOKEN);
            String expires = jsonObject.optString(Constants.PARAM_EXPIRES_IN);
            String openId  = jsonObject.optString(Constants.PARAM_OPEN_ID);

            show("登录成功:" + obj.toString());

            //3这里要向服务发送Token

            mTencent.setAccessToken(token, expires);
            mTencent.setOpenId(openId);


            mInfo = new UserInfo(MainActivity.this, mTencent.getQQToken());

            mInfo.getUserInfo(new IUiListener() {
                @Override
                public void onComplete(Object obj) {

                    Log.d(TAG, "登录成功用户信息: " + obj.toString());

                    Gson         gson         = new Gson();
                    UserInfoBean userInfoBean = gson.fromJson(obj.toString(), UserInfoBean.class);


                    display(userInfoBean.figureurl_1,mIvIcon1);
                    display(userInfoBean.figureurl_2,mIvIcon2);
                    display(userInfoBean.figureurl_qq_1,mIvIcon3);
                    display(userInfoBean.figureurl_qq_2,mIvIcon4);




                    show("登录成功:" + obj.toString());
                }

                @Override
                public void onError(UiError uiError) {
                    show("登录成功:" + uiError.errorMessage);
                }

                @Override
                public void onCancel() {
                    show("用户取消登录");
                }
            });

        }

        /**
         * 登录错误
         *
         * @param uiError
         */
        @Override
        public void onError(UiError uiError) {

            show("登录错误:" + uiError.errorMessage);
        }

        /**
         * 取消登录
         */
        @Override
        public void onCancel() {

            show("用户取消");
        }
    }


    private void show(final String result) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("登录结果");

        builder.setMessage(result);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Log.d(TAG, "show: " + result);
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        Toast.makeText(MainActivity.this, "result: " + result, Toast.LENGTH_SHORT).show();

                    }
                });

            }
        }).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "-->onActivityResult " + requestCode + " resultCode=" + resultCode);
        if (requestCode == Constants.REQUEST_LOGIN ||
                requestCode == Constants.REQUEST_APPBAR) {
            Tencent.onActivityResultData(requestCode, resultCode, data, mLoginListener);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    private void display(String url,ImageView iv){
        Glide.with(getApplicationContext()).load(url)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(iv);
    }
}
