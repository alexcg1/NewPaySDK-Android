package com.newmall;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.newtonproject.newpay.android.sdk.NewPaySDK;
import org.newtonproject.newpay.android.sdk.bean.Currency;
import org.newtonproject.newpay.android.sdk.bean.Order;
import org.newtonproject.newpay.android.sdk.bean.ProfileInfo;
import org.newtonproject.newpay.android.sdk.bean.SigMessage;
import org.newtonproject.newpay.android.sdk.constant.Environment;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout profileLinearLayout;
    private TextView nameTextView;
    private TextView cellphoneTextView;
    private TextView newidTextView;
    private ImageView imageView;
    private String TAG = "Activity";

    private static final String ERROR_CODE = "ERROR_CODE";
    private static final String ERROR_MESSAGE = "ERROR_MESSAGE";


    TextView dev;
    TextView beta;
    TextView testnet;
    TextView mainnet;
    TextView evn;


    private static final int REQUEST_CODE_NEWPAY = 1000;
    private static final String privateKey = "0x298b9bee0a0431e8f1a81323df6810b72467db21f9c252cb6d134d149005a386";
    private static final String publicKey = "0xe5f001b70a3911c1b6dcb857add080beab3ceff7278a012678dbb7e869c787cfd5739cf980adebd59a826eb78eb4d10e746a1fadb49f0245a87270d5a817cda0";
    Gson gson = new Gson();
    private Button request20Bt;
    private Button single;
    private Button multiple;
    private ProfileInfo profileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        NewPaySDK.init(getApplication(), "9a674d65c945569a9071b31b07f3bc52");
    }

    private void initView() {
        profileLinearLayout = findViewById(R.id.profileLayout);
        nameTextView = findViewById(R.id.nameTextView);
        cellphoneTextView = findViewById(R.id.cellphoneTextView);
        newidTextView = findViewById(R.id.newidTextView);
        imageView = findViewById(R.id.avatarImageView);
        request20Bt = findViewById(R.id.request20Bt);
        dev = findViewById(R.id.dev);
        beta = findViewById(R.id.beta);
        testnet = findViewById(R.id.testnet);
        mainnet = findViewById(R.id.mainnet);
        evn = findViewById(R.id.env);
        profileLinearLayout.setOnClickListener(this);
        request20Bt.setOnClickListener(this);
        dev.setOnClickListener(this);
        beta.setOnClickListener(this);
        mainnet.setOnClickListener(this);
        testnet.setOnClickListener(this);
        single = findViewById(R.id.pushSingle);
        multiple = findViewById(R.id.pushMultiple);
        single.setOnClickListener(this);
        multiple.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profileLayout:
                NewPaySDK.requestProfile(this, getSigMessage(privateKey, privateKey));
                break;
            case R.id.request20Bt:
                NewPaySDK.pay(this,"0x920bc30537e3ea976fea09b8a4f025d20e4c674a",BigInteger.valueOf(100));
                break;
            case R.id.pushMultiple:
                pushMultiple();
                break;
            case R.id.pushSingle:
                pushSingle();
                break;
            case R.id.dev:
                evn.setText("Dev");
                NewPaySDK.init(getApplication(), "9a674d65c945569a9071b31b07f3bc52", Environment.DEVNET);

                break;
            case R.id.beta:
                evn.setText("Beta");
                NewPaySDK.init(getApplication(), "9a674d65c945569a9071b31b07f3bc52", Environment.BETANET);

                break;
            case R.id.testnet:
                evn.setText("testnet");
                NewPaySDK.init(getApplication(), "9a674d65c945569a9071b31b07f3bc52", Environment.TESTNET);

                break;
            case R.id.mainnet:
                evn.setText("main");
                NewPaySDK.init(getApplication(), "9a674d65c945569a9071b31b07f3bc52", Environment.MAINNET);
                break;
        }
    }

    private void pushSingle() {
        if(profileInfo == null) {
            Toast.makeText(this, "Profile newid is null", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.currency = Currency.CNY;
        order.orderNumber = "orderSingle" + System.currentTimeMillis();
        order.price = 10.2f;
        order.sellerNewid = "NEWID1ab6wnXrhpEbRtH44zrs3wcjqxmbeqU28Zpv64dzahfRvvJq6JRQ";
        order.buyerNewid = profileInfo.newid;
        orders.add(order);
        String message = gson.toJson(orders);
        Log.e(TAG, "Single order:" + message);
        NewPaySDK.placeOrder(this , getSigMessage(privateKey, message));
    }

    private void pushMultiple() {
        ArrayList<Order> datas = new ArrayList<>();
        if(profileInfo == null) {
            Toast.makeText(this, "Profile newid is null", Toast.LENGTH_SHORT).show();
            return;
        }
        for(int i = 0; i < 2; i++) {
            Order order = new Order();
            order.currency = Currency.CNY;
            order.orderNumber = "order" + i + System.currentTimeMillis();
            order.price = 10.2f + i;
            order.sellerNewid = "NEWID1ab6wnXrhpEbRtH44zrs3wcjqxmbeqU28Zpv64dzahfRvvJq6JRQ";
            order.buyerNewid = profileInfo.newid;
            datas.add(order);
        }
        String message = gson.toJson(datas);
        SigMessage sigMessage = getSigMessage(privateKey, message);
        Log.e(TAG, "Multiple:" + sigMessage.toString());
        NewPaySDK.placeOrder(this , sigMessage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data == null) return;

        if(resultCode == RESULT_OK) {
            int errorCode = data.getIntExtra(ERROR_CODE, 0);
            String errorMessage = data.getStringExtra(ERROR_MESSAGE);
            if(errorCode != 1) {
                Log.e(TAG, "error_code is: " + errorCode);
                Log.e(TAG, "ErrorMessage is:" + errorMessage);
                return;
            }
            if(requestCode == NewPaySDK.REQUEST_CODE_NEWPAY) {

                String profile = data.getStringExtra("profile");
                String sigMessage = data.getStringExtra("signature");

                if(!TextUtils.isEmpty(profile)){
                    profileInfo = gson.fromJson(profile, ProfileInfo.class);
                    cellphoneTextView.setText(profileInfo.cellphone);
                    nameTextView.setText(profileInfo.name);
                    newidTextView.setText(profileInfo.newid);
                    Log.e(TAG, "Profile:" + profile);
                    if(!TextUtils.isEmpty(profileInfo.avatarPath)) {
                        Picasso.get().load(profileInfo.avatarPath).into(imageView);
                    }
                }
                if(!TextUtils.isEmpty(sigMessage)) {
                    SigMessage sig = gson.fromJson(sigMessage, SigMessage.class);
                    Log.e(TAG, "onActivityResult: " + sig.toString());
                }
            }

            // pay result
            if(requestCode == NewPaySDK.REQUEST_CODE_NEWPAY_PAY){
                String txid = data.getStringExtra("txid");
                Toast.makeText(this, "txid is:" + txid, Toast.LENGTH_SHORT).show();
            }

            if(requestCode == NewPaySDK.REQUEST_CODE_PUSH_ORDER) {
                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private static SigMessage getSigMessage(String privateKey, String message) {
        Sign.SignatureData sig = Sign.signMessage(message.getBytes(), ECKeyPair.create(Numeric.toBigInt(privateKey)));
        return new SigMessage(Numeric.toHexString(sig.getR()), Numeric.toHexString(sig.getS()), message);
    }

    public class ErrorCode {
        static final int SUCCESS = 1;

        static final int CANCEL = 2;

        static final int NO_NEWPAY = 100;
        static final int NO_PROFILE = 101;
        static final int NO_BUNDLE_SOURCE = 102;
        static final int SIGNATURE_ERROR = 103;
        static final int SELLER_NEWID_ERROR = 104;
        static final int PROTOCOL_VERSION_LOW = 105;
        static final int NO_ACTION = 106;
        static final int APPID_ERROR = 107;
        static final int NO_ORDER_INFO = 108;
        static final int NEWID_ERROR = 109;
        static final int NO_WALLET = 110;
        static final int UNKNOWN_ERROR = 1000;
    }
}
