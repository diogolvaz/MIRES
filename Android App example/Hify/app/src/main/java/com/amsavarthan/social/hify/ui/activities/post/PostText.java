package com.amsavarthan.social.hify.ui.activities.post;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.social.hify.R;
import com.amsavarthan.social.hify.utils.AnimationUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import me.grantland.widget.AutofitTextView;

import static android.content.res.Configuration.UI_MODE_NIGHT_NO;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.activateRecovery;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.activateUserRecovery;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.getFlag;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.getVersionFromDocument;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.setDataRead;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.transactionInitiated;
import static com.amsavarthan.social.hify.MIRES.UserRecoveryMechanism.UserRecoveryNotification.createRecoveryNotification;
import static com.amsavarthan.social.hify.MIRES.Utils.Utils.MIRES_users_flags;

public class PostText extends AppCompatActivity {

    AutofitTextView preview_text;
    EditText text;
    FirebaseFirestore mFirestore;
    FirebaseUser mCurrentUser;
    String color="7";
    private FrameLayout mImageholder;
    private FirebaseAuth mAuth;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PostText.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context,String preText) {
        Intent intent = new Intent(context, PostText.class).putExtra("preText",preText);
        context.startActivity(intent);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(this)
                .title("Discard")
                .content("Are you sure do you want to go back?")
                .positiveText("Yes")
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .onPositive((dialog, which) -> finish())
                .negativeText("No")
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        setContentView(R.layout.activity_post_text);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("New Text Post");

        int nightModeFlags=getResources().getConfiguration().uiMode& Configuration.UI_MODE_NIGHT_MASK;
        if(nightModeFlags==UI_MODE_NIGHT_NO){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int flags=getWindow().getDecorView().getSystemUiVisibility();
                flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                getWindow().getDecorView().setSystemUiVisibility(flags);
            }
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDarkk));
        }
        try {
            getSupportActionBar().setTitle("New Text Post");
        } catch (Exception e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        text = findViewById(R.id.text);
        preview_text = findViewById(R.id.text_preview);
        mImageholder = findViewById(R.id.image_holder);

        if(StringUtils.isNotEmpty(getIntent().getStringExtra("preText"))){
            text.setText(getIntent().getStringExtra("preText"));
            preview_text.setText(getIntent().getStringExtra("preText"));
        }

        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                preview_text.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_text_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_post:
                if (!TextUtils.isEmpty(text.getText().toString()))
                    sendPost();
                else
                    AnimationUtil.shakeView(text, PostText.this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendPost() {

        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setMessage("Posting...");
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                mDialog.dismiss();

                // Operation data
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("userId", documentSnapshot.getString("id"));
                postMap.put("username", documentSnapshot.getString("username"));
                postMap.put("name", documentSnapshot.getString("name"));
                postMap.put("userimage", documentSnapshot.getString("image"));
                postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                postMap.put("image_count",0);
                postMap.put("description", text.getText().toString());
                postMap.put("color", color);
                postMap.put("counter", FieldValue.increment(1));

                // Begin MIRES transaction
                Map<String, Object> transaction_state = transactionInitiated(FirebaseAuth.getInstance().getUid(), null);

                WriteBatch batch = FirebaseFirestore.getInstance().batch();

                // Initiate Flag
                DocumentReference flag = FirebaseFirestore.getInstance().collection(MIRES_users_flags).document();

                // Activate main recovery
                activateRecovery(postMap, flag.getId());

                // Operation to perform
                DocumentReference post = FirebaseFirestore.getInstance().collection("Posts").document();

                // Activate User Recovery
                activateUserRecovery(postMap,"Posts/"+post.getId(),transaction_state);

                // Read dependency
                ArrayList read = new ArrayList();
                read.add("userId");
                read.add("username");
                read.add("name");
                read.add("userimage");
                ArrayList all_reads = new ArrayList();
                all_reads.add(setDataRead(getVersionFromDocument(documentSnapshot),"Posts/"+post.getId(),read));

                // Post
                batch.set(post, postMap);
                // Add content to flag
                batch.set(flag, getFlag(postMap, "Posts/" + post.getId(), "create", all_reads,transaction_state));

                // Commit transaction
                batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        // Create User Recovery notification
                        createRecoveryNotification(getApplicationContext(),"Undo last action?",transaction_state);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mDialog.dismiss();
                Log.e("Error getting user", e.getMessage());
            }
        });

    }

    public void onFabClicked(View view) {

        switch (view.getId()) {

            case R.id.fab1:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_1));
                color = "1";
                return;

            case R.id.fab2:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_2));
                color = "2";
                return;

            case R.id.fab3:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_3));
                color = "3";
                return;

            case R.id.fab4:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_4));
                color = "4";
                return;

            case R.id.fab5:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_5));
                color = "5";
                return;

            case R.id.fab6:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_6));
                color = "6";
                return;

            case R.id.fab7:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_7));
                color = "7";
                return;

            case R.id.fab8:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_8));
                color = "8";
                return;

            case R.id.fab9:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_9));
                color = "9";
                return;

            case R.id.fab10:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_10));
                color = "10";
                return;

            case R.id.fab11:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_11));
                color = "11";
                return;

            case R.id.fab12:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_12));
                color = "12";
                return;

            case R.id.fab13:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_13));
                color = "13";
                return;

            case R.id.fab14:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_14));
                color = "14";
                return;

            case R.id.fab15:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_15));
                color = "15";
                return;

            case R.id.fab16:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_16));
                color = "16";
                return;

            case R.id.fab17:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_17));
                color = "17";
                return;

            case R.id.fab18:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_18));
                color = "18";
                return;

            case R.id.fab19:
                mImageholder.setBackground(getResources().getDrawable(R.drawable.gradient_19));
                color = "19";

        }

    }


}
