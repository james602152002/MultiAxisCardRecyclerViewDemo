package com.james602152002.multiaxiscardlayoutmanagerdemo.ui;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;
import com.hluhovskyi.camerabutton.CameraButton;
import com.james602152002.multiaxiscardlayoutmanagerdemo.R;
import com.james602152002.multiaxiscardlayoutmanagerdemo.adapter.CameraGalleryAdapter;
import com.james602152002.multiaxiscardlayoutmanagerdemo.recyclerview.item_decoration.CameraGalleryDecoration;
import com.james602152002.multiaxiscardlayoutmanagerdemo.util.IPhone6ScreenResizeUtil;

import java.io.File;
import java.io.FileNotFoundException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityCamera extends ActivityTranslucent implements View.OnClickListener {

    private CameraFragment cameraFragment;
    @BindView(R.id.appbar)
    AppBarLayout appBarLayout;
    @BindView(R.id.camera_header)
    View cameraHeader;
    @BindView(R.id.photo)
    SimpleDraweeView photo;
    @BindView(R.id.btn_group)
    View btnGroup;
    @BindView(R.id.camera_btn)
    CameraButton cameraButton;
    @BindView(R.id.content)
    FrameLayout content;
    @BindView(R.id.crop)
    View crop;
    @BindView(R.id.sure)
    View sure;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private boolean usingCamera = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraFragment = CameraFragment.newInstance(new Configuration.Builder().build());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, cameraFragment, null)
                    .commit();
        }

        initToolBar();
        initView();
    }

    private void initToolBar() {
        CollapsingToolbarLayout mCToolbarLayout = findViewById(R.id.collapsing_toolbar_layout);
        mCToolbarLayout.setCollapsedTitleTextColor(0);
        mCToolbarLayout.setExpandedTitleColor(0);
        // Set the support action bar
        initToolBar(mToolbar);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ((FrameLayout.LayoutParams) mToolbar.getLayoutParams()).topMargin = getStatusBarHeight();

        LinearLayout view = new LinearLayout(this);
        view.setGravity(Gravity.CENTER_VERTICAL);
        TextView text_view = new TextView(this);
        text_view.setTextColor(Color.WHITE);
        text_view.setText("title");
        text_view.setGravity(Gravity.CENTER_VERTICAL);
        IPhone6ScreenResizeUtil.adjustTextSize(text_view, 34);
        view.addView(text_view);
        mToolbar.addView(view);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (usingCamera) {
                    return;
                }

                final int total_scroll_range = appBarLayout.getTotalScrollRange();
                final int dy = verticalOffset + total_scroll_range;
                int alpha = ((int) (0xFF * (-(float) verticalOffset / total_scroll_range)) << 24) + 0xFFFFFF;
                int color = ContextCompat.getColor(appBarLayout.getContext(), R.color.colorPrimary);
                color = color & alpha;
                appBarLayout.setBackgroundColor(color);
                content.setAlpha(dy / total_scroll_range);
//                mToolbar.setBackgroundColor(color);
                if (dy == 0) {
                    if (content.getVisibility() == View.VISIBLE) {
                        content.setVisibility(View.GONE);
                        cameraButton.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().detach(cameraFragment).commit();
                    }
                } else if (verticalOffset == 0 && content.getVisibility() == View.GONE) {
                    content.setVisibility(View.VISIBLE);
                    cameraButton.setVisibility(View.VISIBLE);
                    getSupportFragmentManager().beginTransaction().attach(cameraFragment).commit();
                }
            }
        });
    }


    private void initView() {
        CollapsingToolbarLayout.LayoutParams cameraHeaderParams = (CollapsingToolbarLayout.LayoutParams) cameraHeader.getLayoutParams();
        cameraHeaderParams.width = IPhone6ScreenResizeUtil.getCurrentScreenWidth();
        cameraHeaderParams.height = IPhone6ScreenResizeUtil.getCurrentScreenHeight();

        ConstraintLayout.LayoutParams photoParams = (ConstraintLayout.LayoutParams) photo.getLayoutParams();
        photoParams.width = IPhone6ScreenResizeUtil.getCurrentScreenWidth();
        photoParams.height = IPhone6ScreenResizeUtil.getCurrentScreenHeight();

        cameraButton.setOnPhotoEventListener(new CameraButton.OnPhotoEventListener() {
            @Override
            public void onClick() {
                cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultListener() {
                    @Override
                    public void onVideoRecorded(String filePath) {
                        usingCamera = true;
                    }

                    @Override
                    public void onPhotoTaken(byte[] bytes, String filePath) {
                        usingCamera = true;
                        content.setVisibility(View.GONE);
                        cameraButton.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().detach(cameraFragment).commit();
                        File file = new File(filePath);
                        Uri uri = Uri.fromFile(file);
                        try {
                            MediaStore.Images.Media.insertImage(getContentResolver(), filePath, file.getName(), "crop_photos");
                        } catch (FileNotFoundException exception) {

                        }
                        photo.setImageURI(uri);
                        photo.setTag(uri);
                        showPhoto();
                    }
                }, null, null);
            }
        });

        cameraButton.setProgressArcColors(new int[]{ContextCompat.getColor(this, R.color.colorPrimary), Color.BLUE});

        final CompositeDisposable disposable = new CompositeDisposable();
        Observable<Cursor> observable = Observable.create(new ObservableOnSubscribe<Cursor>() {
            @Override
            public void subscribe(ObservableEmitter<Cursor> emitter) {
                try {
                    Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver mContentResolver = getContentResolver();

                    String[] projection = new String[]{MediaStore.Images.Media.MIME_TYPE,
                            MediaStore.Images.Media.DATE_MODIFIED, MediaStore.Images.Media.DATA};
                    // 只查询jpeg和png的图片
                    Cursor mCursor = mContentResolver.query(mImageUri, projection,
                            MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                            new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_ADDED + " DESC");
//                    MediaStore.Images.Media.DATE_ADDED + " DESC"
                    if (mCursor != null) {
                        emitter.onNext(mCursor);
                        emitter.onComplete();
                    } else {
                        emitter.onError(new Throwable());
                    }
                } catch (Exception e) {

                }
            }
        });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(new CameraGalleryDecoration());
        observable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Cursor>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Cursor cursor) {
                        CameraGalleryAdapter adapter = new CameraGalleryAdapter(ActivityCamera.this, cursor);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        disposable.dispose();
                        disposable.clear();
                    }

                    @Override
                    public void onComplete() {
                        disposable.dispose();
                        disposable.clear();
                    }
                });
    }

    private void showPhoto() {
        final int duration = 500;
//        ScaleAnimation anim = new ScaleAnimation(0, 1, 0, 1,
//                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
//        anim.setDuration(duration);
//        photo.startAnimation(anim);

        btnGroup.setVisibility(View.VISIBLE);
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        translateAnimation.setDuration(duration);
        btnGroup.startAnimation(translateAnimation);
    }

    @OnClick({R.id.crop, R.id.sure})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crop:
                Intent uriIntent = new Intent();
                uriIntent.putExtra("uri", (Uri) photo.getTag());
                setResult(RESULT_OK, uriIntent);
                finish();
                break;
            case R.id.sure:
                break;
        }
    }
}