package com.amsavarthan.social.hify.ui.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amsavarthan.social.hify.R;
import com.amsavarthan.social.hify.adapters.DrawerAdapter;
import com.amsavarthan.social.hify.models.DrawerItem;
import com.amsavarthan.social.hify.models.Images;
import com.amsavarthan.social.hify.models.SimpleItem;
import com.amsavarthan.social.hify.ui.activities.account.LoginActivity;
import com.amsavarthan.social.hify.ui.activities.account.UpdateAvailable;
import com.amsavarthan.social.hify.ui.activities.forum.AddQuestion;
import com.amsavarthan.social.hify.ui.activities.forum.AnswersActivity;
import com.amsavarthan.social.hify.ui.activities.friends.FriendProfile;
import com.amsavarthan.social.hify.ui.activities.post.CommentsActivity;
import com.amsavarthan.social.hify.ui.activities.post.PostImage;
import com.amsavarthan.social.hify.ui.activities.post.PostText;
import com.amsavarthan.social.hify.ui.activities.post.SinglePostView;
import com.amsavarthan.social.hify.ui.fragment.Dashboard;
import com.amsavarthan.social.hify.ui.activities.notification.NotificationActivity;
import com.amsavarthan.social.hify.ui.activities.notification.NotificationImage;
import com.amsavarthan.social.hify.ui.activities.notification.NotificationImageReply;
import com.amsavarthan.social.hify.ui.activities.notification.NotificationReplyActivity;
import com.amsavarthan.social.hify.ui.fragment.About;
import com.amsavarthan.social.hify.ui.fragment.FlashMessage;
import com.amsavarthan.social.hify.ui.fragment.Forum;
import com.amsavarthan.social.hify.ui.fragment.FriendsFragment;
import com.amsavarthan.social.hify.ui.activities.account.Notifications;
import com.amsavarthan.social.hify.ui.fragment.ProfileFragment;
import com.amsavarthan.social.hify.utils.Config;
import com.amsavarthan.social.hify.utils.NetworkUtil;
import com.amsavarthan.social.hify.utils.database.UserHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.marcoscg.dialogsheet.DialogSheet;
import com.tapadoo.alerter.Alerter;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.amsavarthan.social.hify.MIRES.Tokens.SendToken.sendToken;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.activateRecovery;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.activateSnapshot;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.getFlag;
import static com.amsavarthan.social.hify.MIRES.Transaction.Configuration.transactionInitiated;
import static com.amsavarthan.social.hify.MIRES.Utils.Utils.MIRES_users_flags;


/**
 * Created by amsavarthan on 29/3/18.
 */

public class MainActivity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener {

    private static final int POS_DASHBOARD = 0;
    private static final int POS_FORUM = 1;
    private static final int POS_SEND_MESSAGE = 2;
    private static final int POS_FRIENDS = 3;
    private static final int POS_ABOUT = 4;
    private static final int POS_LOGOUT = 6;
    public static String userId;
    public static MainActivity activity;
    DrawerAdapter adapter;
    View sheetView;
    private String[] screenTitles;
    private Drawable[] screenIcons;
    private SlidingRootNav slidingRootNav;
    private FirebaseAuth mAuth;
    public static FirebaseUser currentuser;
    private FirebaseFirestore firestore;
    private UserHelper userHelper;
    private StorageReference storageReference;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Intent resultIntent;
    public static CircleImageView imageView;
    public static TextView username;
    private AuthCredential credential;
    public static Fragment mCurrentFragment;
    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive");
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    updateToken();
                    performUploadTask();
                    try {
                        Snackbar.make(findViewById(R.id.activity_main), "Syncing...", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        Snackbar.make(findViewById(R.id.activity_main), "No Internet Connection...", Snackbar.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    };
    private BottomSheetDialog mBottomSheetDialog;
    public static Toolbar toolbar;
    private boolean mState=true;
    private boolean mStateForum=false;
    private MenuItem add_question;
    private List<Images> imagesList=new ArrayList<>();
    private boolean validate;
    public static MenuItem add_post;
    private TextView badge_count;

    public static void startActivity(Context context) {
        Intent intent=new Intent(context,MainActivity.class);
        context.startActivity(intent);
    }

    public static void startActivity(Context context,boolean validate) {
        Intent intent=new Intent(context,MainActivity.class).putExtra("validate",validate);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        slidingRootNav.closeMenu(true);
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){

            if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
            }else{
                startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();
        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",false).apply();

    }

    @Override
    protected void onStart() {
        super.onStart();

        getSharedPreferences("fcm_activity",MODE_PRIVATE).edit().putBoolean("active",true).apply();

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);

        validate=getIntent().getBooleanExtra("validate",false);

        if(currentuser!=null)
        {
            try {
                performUploadTask();
            }catch (Exception e){
                Log.e("Error","."+e.getLocalizedMessage());
            }

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));


        }else{
            LoginActivity.startActivityy(this);
            finish();
        }
    }

    @Override
    public void onBackPressed() {

        if (!mState) {

            toolbar.setTitle("Dashboard");
            try {
                getSupportActionBar().setTitle("Dashboard");
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            this.invalidateOptionsMenu();
            mState = true;
            showFragment(new Dashboard());
            if (slidingRootNav.isMenuOpened()) {
                slidingRootNav.closeMenu(true);
            }
            adapter.setSelected(POS_DASHBOARD);

        } else if (slidingRootNav.isMenuOpened()) {
            slidingRootNav.closeMenu(true);
        } else {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());


        setContentView(R.layout.activity_main);
        activity=this;
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Dashboard");
        try {
            getSupportActionBar().setTitle("Dashboard");
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }

        userHelper = new UserHelper(this);
        firestore = FirebaseFirestore.getInstance();

        registerReceiver(NetworkChangeReceiver
                , new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        mAuth = FirebaseAuth.getInstance();
        currentuser = mAuth.getCurrentUser();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Config.createNotificationChannels(this);
        }

        if (currentuser == null) {

            LoginActivity.startActivityy(this);
            finish();

        } else {

            mCurrentFragment = new Dashboard();
            firebaseMessagingService();
            askPermission();

            updateToken();

            userId = currentuser.getUid();
            storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentuser.getUid() + ".jpg");

            slidingRootNav = new SlidingRootNavBuilder(this)
                    .withToolbarMenuToggle(toolbar)
                    .withMenuOpened(false)
                    .withContentClickableWhenMenuOpened(false)
                    .withSavedState(savedInstanceState)
                    .withMenuLayout(R.layout.activity_main_drawer)
                    .inject();

            screenIcons = loadScreenIcons();
            screenTitles = loadScreenTitles();

            adapter = new DrawerAdapter(Arrays.asList(
                    createItemFor(POS_DASHBOARD).setChecked(true),
                    createItemFor (POS_FORUM),
                    createItemFor (POS_SEND_MESSAGE),
                    createItemFor(POS_FRIENDS),
                    createItemFor(POS_ABOUT),
                    new SpaceItem(48),
                    createItemFor(POS_LOGOUT)));
            adapter.setListener(this);

            RecyclerView list = findViewById(R.id.list);
            list.setNestedScrollingEnabled(false);
            list.setLayoutManager(new LinearLayoutManager(this));
            list.setAdapter(adapter);

            adapter.setSelected(POS_DASHBOARD);
            setUserProfile();

            mBottomSheetDialog = new BottomSheetDialog(this);
            sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
            mBottomSheetDialog.setContentView(sheetView);

            LinearLayout text_post = sheetView.findViewById(R.id.text_post);
            LinearLayout photo_post = sheetView.findViewById(R.id.image_post);

            text_post.setOnClickListener(view -> {
                mBottomSheetDialog.dismiss();
                PostText.startActivity(MainActivity.this);

            });

            photo_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBottomSheetDialog.dismiss();
                    startPickImage();
                }
            });

            if(!TextUtils.isEmpty(getIntent().getStringExtra("openFragment"))){

                if(getIntent().getStringExtra("openFragment").equals("forLike")) {
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",false));
                }else{
                    startActivity(new Intent(this, SinglePostView.class).putExtra("post_id", getIntent().getStringExtra("post_id")).putExtra("forComment",true));
                }
            }
        }
    }

    private void updateToken() {

        final String token_id = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE).getString("regId","");
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("token_ids", FieldValue.arrayUnion(token_id));


        // Send token to MIRES
        sendToken(FirebaseAuth.getInstance().getUid(),token_id);

        // Begin MIRES transaction
        Map<String, Object> transaction_state = transactionInitiated(FirebaseAuth.getInstance().getUid(), null);

        // Firebase transaction
        WriteBatch batch = FirebaseFirestore.getInstance().batch();

        // Initiate Flag
        DocumentReference flag = FirebaseFirestore.getInstance().collection(MIRES_users_flags).document();

        // Store token
        DocumentReference post = FirebaseFirestore.getInstance().collection("Users").document(currentuser.getUid());

        // Recovery
        activateRecovery(tokenMap,flag.getId());

        // Activate Snaphost Creator
        activateSnapshot(tokenMap);

        // Add content to flag
        batch.set(flag, getFlag(tokenMap, "Users/" + post.getId(), "update", null,transaction_state));

        batch.update(post, tokenMap);

        if(isOnline()) {
            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("TOKEN", token_id);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Error Token", e.getMessage());
                        }
                    });

        }
    }

    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                         if(report.isAnyPermissionPermanentlyDenied()){
                            Toasty.info(MainActivity.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toasty.LENGTH_LONG,true).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }

    private void setUserProfile() {

        Cursor rs = userHelper.getData(1);
        rs.moveToFirst();

        String nam = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
        String imag = rs.getString(rs.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));

        if (!rs.isClosed()) {
            rs.close();
        }

        username = findViewById(R.id.username);
        imageView = findViewById(R.id.profile_image);
        username.setText(nam);
        Glide.with(this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                .load(imag)
                .into(imageView);

    }

    public void showDialog(){

        new DialogSheet(this)
                .setTitle("Information")
                .setMessage("Email has not been verified, please verify and continue. If you have verified we recommend you to logout and login again")
                .setPositiveButton("Send again", v -> mAuth.getCurrentUser().sendEmailVerification()
                        .addOnSuccessListener(aVoid -> Toasty.success(MainActivity.this, "Verification email sent", Toasty.LENGTH_SHORT,true).show())
                        .addOnFailureListener(e -> Log.e("Error", e.getMessage())))
                .setNegativeButton("Ok", new DialogSheet.OnNegativeClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .setCancelable(true)
                .setRoundedCorners(true)
                .setColoredNavigationBar(true)
                .show();

    }

    @Override
    public void onItemSelected(int position) {

        Fragment selectedScreen;
        switch (position) {

            case POS_DASHBOARD:
                toolbar.setTitle("Dashboard");
                try {
                    getSupportActionBar().setTitle("Dashboard");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=true;
                mStateForum=false;
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);

                slidingRootNav.closeMenu(true);

                return;

            case POS_FORUM:
                toolbar.setTitle("Forum");
                try {
                    getSupportActionBar().setTitle("Forum");
                }catch (Exception e){
                    Log.e("Error",e.getMessage());
                }
                this.invalidateOptionsMenu();
                mState=false;
                mStateForum=true;
                selectedScreen = new Forum();
                showFragment(selectedScreen);

                slidingRootNav.closeMenu(true);

                return;

            case POS_SEND_MESSAGE:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Flash Messages");
                    try {
                        getSupportActionBar().setTitle("Flash Messages");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new FlashMessage();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }

                return;

            case POS_FRIENDS:

                if(currentuser.isEmailVerified()) {
                    toolbar.setTitle("Manage Friends");
                    try {
                        getSupportActionBar().setTitle("Manage Friends");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new FriendsFragment();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }
                return;

            case POS_ABOUT:

                if(currentuser.isEmailVerified()) {

                    toolbar.setTitle("About");
                    try {
                        getSupportActionBar().setTitle("About");
                    } catch (Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                    this.invalidateOptionsMenu();
                    mState = false;
                    mStateForum=false;
                    selectedScreen = new About();
                    showFragment(selectedScreen);

                    slidingRootNav.closeMenu(true);
                }else{
                    showDialog();
                }
                return;


            case POS_LOGOUT:

                if (currentuser != null && isOnline()) {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("Are you sure do you want to logout from this account?")
                            .positiveText("Yes")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    logout();
                                    dialog.dismiss();
                                }
                            }).negativeText("No")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                } else {

                    new MaterialDialog.Builder(this)
                            .title("Logout")
                            .content("A technical occurred while logging you out, Check your network connection and try again.")
                            .positiveText("Done")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            }).show();

                }

                return;

            default:
                selectedScreen = new Dashboard();
                showFragment(selectedScreen);

        }

        slidingRootNav.closeMenu(true);

    }

    public void logout() {
        performUploadTask();
        final ProgressDialog mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setMessage("Logging you out...");
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, MODE_PRIVATE);

        Map<String, Object> tokenRemove = new HashMap<>();
        tokenRemove.put("token_ids", FieldValue.arrayRemove(pref.getString("regId","")));

        firestore.collection("Users").document(userId).update(tokenRemove).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                userHelper.deleteContact(1);
                mAuth.signOut();
                LoginActivity.startActivityy(MainActivity.this);
                mDialog.dismiss();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toasty.error(MainActivity.this, "Error logging out", Toasty.LENGTH_SHORT,true).show();
                mDialog.dismiss();
                Log.e("Logout Error", e.getMessage());
            }
        });

    }

    public static void showFragment(Fragment fragment) {
        activity.getFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        mCurrentFragment=fragment;
    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(Color.parseColor("#989898"))
                .withTextTint(Color.parseColor("#989898"))
                .withSelectedIconTint(color(R.color.colorAccentt))
                .withSelectedTextTint(color(R.color.colorAccentt));
    }

    @NonNull
    private String[] loadScreenTitles() {
        return getResources().getStringArray(R.array.ld_activityScreenTitles);
    }

    private Drawable[] loadScreenIcons() {
        TypedArray ta = getResources().obtainTypedArray(R.array.ld_activityScreenIcons);
        Drawable[] icons = new Drawable[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            if (id != 0) {
                icons[i] = ContextCompat.getDrawable(this, id);
            }
        }
        ta.recycle();
        return icons;
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    private void firebaseMessagingService() {

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("OnBroadcastReceiver", "received");

               if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    Log.i("OnBroadcastReceiver", "push_received");

                    String click_action = intent.getStringExtra("click_action");

                    switch (click_action) {
                        case "com.amsavarthan.social.hify.TARGETNOTIFICATION":

                            resultIntent = new Intent(MainActivity.this, NotificationActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGETNOTIFICATIONREPLY":

                            resultIntent = new Intent(MainActivity.this, NotificationReplyActivity.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGETNOTIFICATION_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImage.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.social.hify.TARGETNOTIFICATIONREPLY_IMAGE":

                            resultIntent = new Intent(MainActivity.this, NotificationImageReply.class);

                            showAlert(resultIntent, intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGET_FRIENDREQUEST":

                            resultIntent = new Intent(MainActivity.this, FriendProfile.class);

                            showAlert(resultIntent, intent);


                            break;
                        case "com.amsavarthan.social.hify.TARGET_COMMENT":

                            resultIntent = new Intent(MainActivity.this, CommentsActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGET_UPDATE":

                            resultIntent = new Intent(MainActivity.this, UpdateAvailable.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGET_LIKE":

                            resultIntent = new Intent(MainActivity.this, SinglePostView.class);

                            showAlert(resultIntent,intent);

                            break;
                        case "com.amsavarthan.social.hify.TARGET_FORUM":

                            resultIntent = new Intent(MainActivity.this, AnswersActivity.class);

                            showAlert(resultIntent,intent);

                            break;
                        default:

                            resultIntent = null;
                            break;
                    }

                }
            }
        };
    }

    private void showAlert(final Intent resultIntent, Intent intent) {

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String name = intent.getStringExtra("name");
        String from_image = intent.getStringExtra("from_image");
        String message = intent.getStringExtra("message");
        String from_id = intent.getStringExtra("from_id");
        String timestamp = intent.getStringExtra("timestamp");
        String reply_for = intent.getStringExtra("reply_for");
        String image = intent.getStringExtra("image");
        String reply_image = intent.getStringExtra("reply_image");

        String f_id = intent.getStringExtra("f_id");
        String f_name = intent.getStringExtra("f_name");
        String f_email = intent.getStringExtra("f_email");
        String f_token = intent.getStringExtra("f_token");
        String f_image = intent.getStringExtra("f_image");

        String user_id=intent.getStringExtra("user_id");
        String post_id=intent.getStringExtra("post_id");
        String post_desc=intent.getStringExtra("post_desc");
        String admin_id=intent.getStringExtra("admin_id");

        String channel=intent.getStringExtra("channel");
        String version=intent.getStringExtra("version");
        String improvements=intent.getStringExtra("improvements");
        String link=intent.getStringExtra("link");
        String question_id=intent.getStringExtra("question_id");
        String question_timestamp=intent.getStringExtra("question_timestamp");
        String notification_type=intent.getStringExtra("notification_type");

        resultIntent.putExtra("title", title);
        resultIntent.putExtra("body", body);
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("from_image", from_image);
        resultIntent.putExtra("message", message);
        resultIntent.putExtra("from_id", from_id);
        resultIntent.putExtra("timestamp", timestamp);
        resultIntent.putExtra("reply_for", reply_for);
        resultIntent.putExtra("image", image);
        resultIntent.putExtra("reply_image", reply_image);

        resultIntent.putExtra("f_id", f_id);
        resultIntent.putExtra("f_name", f_name);
        resultIntent.putExtra("f_email", f_email);
        resultIntent.putExtra("f_image", f_image);
        resultIntent.putExtra("f_token", f_token);

        resultIntent.putExtra("user_id", user_id);
        resultIntent.putExtra("post_id", post_id);
        resultIntent.putExtra("post_desc", post_desc);
        resultIntent.putExtra("admin_id", admin_id);

        resultIntent.putExtra("channel", channel);
        resultIntent.putExtra("version", version);
        resultIntent.putExtra("improvements", improvements);
        resultIntent.putExtra("link", link);
        resultIntent.putExtra("question_id", question_id);
        resultIntent.putExtra("question_timestamp", question_timestamp);
        resultIntent.putExtra("notification_type", notification_type);

        Alerter.create(MainActivity.this)
                .setTitle(title)
                .setText(body)
                .enableSwipeToDismiss()
                .setDuration(4000)//4sec
                .enableVibration(true)
                .setBackgroundColorRes(R.color.colorAccentt)
                .setProgressColorRes(R.color.colorPrimaryy)
                .setTitleAppearance(R.style.AlertTextAppearance_Title)
                .setTextAppearance(R.style.AlertTextAppearance_Text)
                .setOnClickListener(view -> startActivity(resultIntent)).show();


    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    public void performUploadTask(){

        if(isOnline()){

            Cursor rc =userHelper.getData(1);
            rc.moveToFirst();

            final String nam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_NAME));
            final String emai = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_EMAIL));
            final String imag = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_IMAGE));
            final String password = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_PASS));
            final String usernam = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_USERNAME));
            final String loc = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_LOCATION));
            final String bi = rc.getString(rc.getColumnIndex(UserHelper.CONTACTS_COLUMN_BIO));

            if(!rc.isClosed()){
                rc.close();
            }

            if(!validate){

                FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String name = documentSnapshot.getString("name");
                        String image = documentSnapshot.getString("image");
                        final String email = documentSnapshot.getString("email");
                        String bio = documentSnapshot.getString("bio");
                        String usrname = documentSnapshot.getString("username");
                        String location = documentSnapshot.getString("location");

                        username.setText(name);
                        Glide.with(MainActivity.this)
                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                .load(image)
                                .into(imageView);


                        if (!image.equals(imag)) {
                            storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {

                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(final Uri downloadUri) {
                                                Map<String, Object> userMap = new HashMap<>();
                                                userMap.put("image", downloadUri.toString());

                                                FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        userHelper.updateContactImage(1, downloadUri.toString());
                                                        Glide.with(MainActivity.this)
                                                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                                                .load(downloadUri)
                                                                .into(imageView);

                                                    }

                                                });
                                            }
                                        });

                                    }
                                }
                            });
                        }

                        if (!bio.equals(bi)) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("bio", bi);

                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    userHelper.updateContactBio(1, bi);

                                }

                            });
                        }

                        if (!location.equals(loc)) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("location", loc);

                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    userHelper.updateContactLocation(1, loc);

                                }

                            });
                        }

                        if (!name.equals(nam)) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", nam);

                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    userHelper.updateContactName(1, nam);
                                    username.setText(nam);

                                }

                            });
                        }

                        if (!currentuser.getEmail().equals(emai)) {


                            credential = EmailAuthProvider
                                    .getCredential(currentuser.getEmail(), password);

                            currentuser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            currentuser.updateEmail(emai).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {

                                                        if (!email.equals(emai)) {
                                                            Map<String, Object> userMap = new HashMap<>();
                                                            userMap.put("email", emai);

                                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {

                                                                    userHelper.updateContactEmail(1, emai);
                                                                }

                                                            });
                                                        }

                                                    } else {

                                                        Log.e("Update email error", task.getException().getMessage() + "..");

                                                    }

                                                }
                                            });

                                        }
                                    });
                        }
                    }
                });

            }else {

                FirebaseAuth.getInstance().signOut();
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emai, password)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                userHelper.deleteContact(1);
                                Toasty.error(MainActivity.this, "Authentication revoked", Toasty.LENGTH_SHORT, true).show();
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                FirebaseFirestore.getInstance().collection("Users").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                                        String name = documentSnapshot.getString("name");
                                        String image = documentSnapshot.getString("image");
                                        final String email = documentSnapshot.getString("email");
                                        String bio = documentSnapshot.getString("bio");
                                        String usrname = documentSnapshot.getString("username");
                                        String location = documentSnapshot.getString("location");

                                        username.setText(name);
                                        Glide.with(MainActivity.this)
                                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                                .load(image)
                                                .into(imageView);


                                        if (!image.equals(imag)) {
                                            storageReference.putFile(Uri.parse(imag)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {

                                                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(final Uri downloadUri) {
                                                                Map<String, Object> userMap = new HashMap<>();
                                                                userMap.put("image", downloadUri.toString());

                                                                FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        userHelper.updateContactImage(1, downloadUri.toString());
                                                                        Glide.with(MainActivity.this)
                                                                                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_profile_picture))
                                                                                .load(downloadUri)
                                                                                .into(imageView);

                                                                    }

                                                                });
                                                            }
                                                        });

                                                    }
                                                }
                                            });
                                        }

                                        if (!bio.equals(bi)) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("bio", bi);

                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    userHelper.updateContactBio(1, bi);

                                                }

                                            });
                                        }

                                        if (!location.equals(loc)) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("location", loc);

                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    userHelper.updateContactLocation(1, loc);

                                                }

                                            });
                                        }

                                        if (!name.equals(nam)) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("name", nam);

                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    userHelper.updateContactName(1, nam);
                                                    username.setText(nam);

                                                }

                                            });
                                        }

                                        if (!currentuser.getEmail().equals(emai)) {


                                            credential = EmailAuthProvider
                                                    .getCredential(currentuser.getEmail(), password);

                                            currentuser.reauthenticate(credential)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            currentuser.updateEmail(emai).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()) {

                                                                        if (!email.equals(emai)) {
                                                                            Map<String, Object> userMap = new HashMap<>();
                                                                            userMap.put("email", emai);

                                                                            FirebaseFirestore.getInstance().collection("Users").document(userId).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                @Override
                                                                                public void onSuccess(Void aVoid) {

                                                                                    userHelper.updateContactEmail(1, emai);
                                                                                }

                                                                            });
                                                                        }

                                                                    } else {

                                                                        Log.e("Update email error", task.getException().getMessage() + "..");

                                                                    }

                                                                }
                                                            });

                                                        }
                                                    });
                                        }
                                    }
                                });
                            }
                        });

            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(NetworkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onDestroy();

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void onViewProfileClicked(View view) {

        toolbar.setTitle("My Profile");
        try {
            getSupportActionBar().setTitle("My Profile");
        }catch (Exception e){
            Log.e("Error",e.getMessage());
        }
        this.invalidateOptionsMenu();
        mState=false;
        showFragment(new ProfileFragment());
        slidingRootNav.closeMenu(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_posts,menu);
        add_post=menu.findItem(R.id.action_new);
        add_question=menu.findItem(R.id.action_new_question);

        MenuItem notification=menu.findItem(R.id.action_notifications);
        View badge_action=notification.getActionView();

        badge_count=badge_action.findViewById(R.id.badge);

        badge_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(notification);
                badge_count.setVisibility(View.GONE);
            }
        });

        int count=getSharedPreferences("Notifications",MODE_PRIVATE).getInt("count",0);

        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Info_Notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(queryDocumentSnapshots.size()>count){
                        badge_count.setVisibility(View.VISIBLE);
                        badge_count.setText(String.valueOf(queryDocumentSnapshots.size()-count));
                    }else{
                        badge_count.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);

        if(mStateForum){
            add_question.setVisible(true);
        }else{
            add_question.setVisible(false);
        }

        if(mState){
            add_post.setVisible(true);
        }else {
            add_post.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.PICK_IMAGES ) {
            if (resultCode == RESULT_OK && data != null) {
                imagesList.clear();
                List<Image> pickedImages=ImagePicker.getImages(data);

                for(Image image:pickedImages){
                    imagesList.add(new Images(image.getName(),image.getPath(),image.getPath(),image.getId()));
                }

                new MaterialDialog.Builder(this)
                        .title("Confirmation")
                        .content("Are you sure do you want to continue?")
                        .positiveText("Yes")
                        .negativeText("No")
                        .cancelable(false)
                        .canceledOnTouchOutside(false)
                        .neutralText("Cancel")
                        .onPositive((dialog, which) -> {

                            Intent intent=new Intent(MainActivity.this,PostImage.class);
                            intent.putParcelableArrayListExtra("imagesList",(ArrayList<? extends Parcelable>) imagesList);
                            startActivity(intent);

                        })
                        .onNegative((dialog, which) -> ImagePicker.create(MainActivity.this)
                                .folderMode(true)
                                .toolbarFolderTitle("Select Image(s)")
                                .toolbarImageTitle("Tap to select")
                                .includeVideo(false)
                                .multi()
                                .limit(7)
                                .showCamera(true)
                                .enableLog(true)
                                .imageDirectory("Hify")
                                .start(Config.PICK_IMAGES)).show();

            }
        }

    }

    private void startPickImage() {

        ImagePicker.create(this)
                .folderMode(true)
                .toolbarFolderTitle("Folder")
                .toolbarImageTitle("Tap to select")
                .includeVideo(false)
                .multi()
                .limit(7)
                .showCamera(true)
                .enableLog(true)
                .imageDirectory("Hify")
                .start(Config.PICK_IMAGES);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_new:
                if(currentuser.isEmailVerified()) {
                    mBottomSheetDialog.show();
                }else{
                    showDialog();
                }
                return true;

            case R.id.action_notifications:
                startActivity(new Intent(MainActivity.this,Notifications.class));
                return true;
            case R.id.action_new_question:
                startActivity(new Intent(MainActivity.this, AddQuestion.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
