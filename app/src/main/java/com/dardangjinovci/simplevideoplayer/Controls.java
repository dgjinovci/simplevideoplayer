package com.dardangjinovci.simplevideoplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dardan on 25-Jan-17.
 */

public class Controls implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    //Controls
    private ViewGroup playerView, container;
    private View controlsView;
    private SeekBar seekBar;
    private Button btnPlay, btnFullScreen;
    private TextView txtTime, txtTotalTime;
    private TextureView textureView;
    private MediaPlayer mediaPlayer;


    private int hideControlsAtMillis = 2000;

    private int controlsBackgroundColor = Color.TRANSPARENT;
    private int controlsColor = Color.WHITE;

    private Handler updateSeekBarHandler;
    private Context context;
    private Dialog fullScreenDialog;

    private boolean isFullscreen = false;
    private SensorRotation rotationType = SensorRotation.LANDSCAPE_LEFT;
    private SensorRotation currentRotation = rotationType;

    private OrientationEventListener orientationEvent;

    public boolean isFullscreen() {
        return isFullscreen;
    }

    Handler hideControlsHandler;


    public View getControlsView() {
        return controlsView;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public Button getBtnPlay() {
        return btnPlay;
    }

    public Button getBtnFullScreen() {
        return btnFullScreen;
    }

    public TextView getTxtTime() {
        return txtTime;
    }

    public TextView getTxtTotalTime() {
        return txtTotalTime;
    }

    public void setTextColor(int color) {

        txtTime.setTextColor(color);
        txtTotalTime.setTextColor(color);
        //seekBar.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.ADD));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorStateList csl = new ColorStateList(new int[][]{{}}, new int[]{color});
            btnPlay.setBackgroundTintList(csl);
            btnFullScreen.setBackgroundTintList(csl);
        }
    }

    public void allowFullScreen(boolean allow) {
        btnFullScreen.setVisibility(allow ? View.VISIBLE : View.GONE);
    }

    public void setBackgroundColor(int color) {
        controlsView.setBackgroundColor(color);
    }

    public Controls(Context ctx, TextureView textureView, ViewGroup playerView) {


        this.context = ctx;

        this.playerView = playerView;
        this.container = (ViewGroup) playerView.findViewById(R.id.container);
        this.controlsView = playerView.findViewById(R.id.controls);
        this.controlsView.setVisibility(View.GONE);


        this.textureView = textureView;

        seekBar = (SeekBar) controlsView.findViewById(R.id.seekBar);
        btnPlay = (Button) controlsView.findViewById(R.id.btnPlay);
        btnFullScreen = (Button) controlsView.findViewById(R.id.btnFullScreen);
        txtTime = (TextView) controlsView.findViewById(R.id.txtTime);
        txtTotalTime = (TextView) controlsView.findViewById(R.id.txtTotalTime);


        seekBar.setOnSeekBarChangeListener(this);
        btnPlay.setOnClickListener(this);
        btnFullScreen.setOnClickListener(this);
        this.textureView.setOnClickListener(this);


        updateSeekBarHandler = new Handler();
        hideControlsHandler = new Handler();

    }

    @Override
    public void onClick(View view) {
        if (view == btnPlay) {
            if (mediaPlayer == null) return;

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlay.setBackgroundResource(R.drawable.play);
            } else {
                mediaPlayer.start();
                btnPlay.setBackgroundResource(R.drawable.pause);
            }
            hideControls();

        } else if (view == textureView) {
            showControls();
            hideControls();

        } else if (view == btnFullScreen) {

            hideControls();

            if (isFullscreen) {
                returnToNormalScreen();
            } else {
                goToFullScreen();
            }
        }
    }

    private void goToFullScreen() {
        playerView.removeView(container);


        fullScreenDialog = new Dialog(context, android.R.style.Theme_NoTitleBar_Fullscreen);
        fullScreenDialog.setContentView(container);

        fullScreenDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                isFullscreen = true;

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(container.getHeight(), container.getWidth());

                params.leftMargin = container.getWidth() / 2 - (container.getHeight() / 2);
                params.topMargin = container.getHeight() / 2 - (container.getWidth() / 2);

                container.setLayoutParams(params);

                container.animate().rotation(currentRotation.value).start();

                if (rotationType == SensorRotation.LANDSCAPE_SENSOR && orientationEvent != null)
                    orientationEvent.enable();
            }
        });

        fullScreenDialog.show();


        fullScreenDialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    returnToNormalScreen();
                }
                return true;
            }
        });
    }

    private void returnToNormalScreen() {

        isFullscreen = false;

        ViewGroup parent = (ViewGroup) container.getParent();
        if (parent != null) parent.removeView(container);

        container.setRotation(0);
        playerView.addView(container);
        fullScreenDialog.dismiss();
        fullScreenDialog = null;

        if (rotationType == SensorRotation.LANDSCAPE_SENSOR && orientationEvent != null)
            orientationEvent.disable();

        container.requestFocus();
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        calculateMillisToMMSS(progress * 1000);
//        if (fromUser) {
//            mediaPlayer.seekTo(progress * 1000);
////            int secondaryPosition = seekBar.getSecondaryProgress();
////            if (progress < secondaryPosition)
////                mediaPlayer.seekTo(progress * 1000);
//        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mediaPlayer.seekTo(seekBar.getProgress() * 1000);
    }


    public void onPrepared(MediaPlayer mediaPlayer) {


        this.mediaPlayer = mediaPlayer;
        int millis = mediaPlayer.getDuration();
        seekBar.setMax(millis / 1000);

        txtTime.setText("00:00");
        txtTotalTime.setText(String.format(Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));

        updateSeekBarHandler.postDelayed(updateSeekBarTimer, 1000);

        controlsView.setVisibility(View.VISIBLE);

    }


    private Runnable updateSeekBarTimer = new Runnable() {
        @Override
        public void run() {

            int millis = mediaPlayer.getCurrentPosition();

            seekBar.setProgress(millis / 1000);

            calculateMillisToMMSS(millis);

            updateSeekBarHandler.postDelayed(updateSeekBarTimer, 1000);
        }
    };

    private void calculateMillisToMMSS(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = ((millis / (1000 * 60)) % 60);

        txtTime.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
    }

    public void hideControls() {
//        Animation anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
//        anim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                    controlsView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        controlsView.startAnimation(anim);


        hideControlsHandler.removeCallbacks(hideControlsRunnable);

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            hideControlsHandler.postDelayed(hideControlsRunnable, (long) hideControlsAtMillis);
        }
    }

    private Runnable hideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            Animation a = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
            a.setFillAfter(true);
            controlsView.startAnimation(a);
        }
    };


    public void showControls() {

        Animation a = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        a.setFillAfter(true);
        controlsView.startAnimation(a);

    }


    public void initWithStyles(TypedArray types) {
        controlsBackgroundColor = types.getColor(R.styleable.SimpleVideoPlayer_controlsBackgroundColor, controlsBackgroundColor);
        controlsColor = types.getColor(R.styleable.SimpleVideoPlayer_controlsColor, controlsColor);
        hideControlsAtMillis = types.getInt(R.styleable.SimpleVideoPlayer_hideControlsAtMillis, 3000);

        int rot = types.getInt(R.styleable.SimpleVideoPlayer_fullScreenRotation, SensorRotation.LANDSCAPE_LEFT.value);

        rotationType = SensorRotation.fromValue(rot);


        if (rotationType == SensorRotation.LANDSCAPE_SENSOR) {

            initSensorRotation();
        }
//            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
//            sensorManager.registerListener(new SensorEventListener() {
//                int orientation = -1;
//
//                @Override
//                public void onSensorChanged(SensorEvent event) {
//                    if (event.values[1] < 6.5 && event.values[1] > -6.5) {
//                        if (orientation != 1) {
//                            Log.d("Sensor", String.format("Landscape: [%s][%s][%s]", event.values[0], event.values[1], event.values[2]));
//                        }
//                        orientation = 1;
//                    } else {
//                        if (orientation != 0) {
//                            Log.d("Sensor", String.format("Portrait: [%s][%s][%s]", event.values[0], event.values[1], event.values[2]));
//                        }
//                        orientation = 0;
//                    }
//                }
//
//                @Override
//                public void onAccuracyChanged(Sensor sensor, int accuracy) {
//                    // TODO Auto-generated method stub
//
//                }
//            }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);

        setTextColor(controlsColor);
        setBackgroundColor(controlsBackgroundColor);
    }

    private void initSensorRotation() {
        orientationEvent = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {

            int rotationMargin = 20;

            @Override
            public void onOrientationChanged(int orientation) {
                Log.d("Sensor", "Orientation: " + orientation);

                if (!isFullscreen) return;

                if (orientation > 90 - rotationMargin && orientation < 90 + rotationMargin && currentRotation != SensorRotation.LANDSCAPE_RIGHT) {

                    container.animate().rotation((currentRotation = SensorRotation.LANDSCAPE_RIGHT).value).start();

                } else if (orientation > 270 - rotationMargin && orientation < 270 + rotationMargin && currentRotation != SensorRotation.LANDSCAPE_LEFT) {

                    container.animate().rotation((currentRotation = SensorRotation.LANDSCAPE_LEFT).value).start();
                }
            }
        };
    }
}

