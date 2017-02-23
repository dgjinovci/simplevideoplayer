package com.dardangjinovci.simplevideoplayer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * Created by Dardan on 23-Jan-17.
 */

public class SimpleVideoPlayer extends RelativeLayout
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener,
        TextureView.SurfaceTextureListener {

    private boolean autoPlayOnLoad = false;
    private String videoUrl = "";
    private Drawable thumbnailDrawable;

    private View view;
    private ProgressBar progressBar;


    private Controls controls;


    private TextureView textureView;
    private MediaPlayer mediaPlayer;


    public void setControlsBackgroundColor(int controlsBackgroundColor) {
        if (controls != null)
            controls.setBackgroundColor(controlsBackgroundColor);
    }


    public void setAutoPlayOnLoad(boolean autoPlayOnLoad) {
        this.autoPlayOnLoad = autoPlayOnLoad;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public SimpleVideoPlayer(Context context) {
        super(context);
        init(context, null);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(final Context ctx, AttributeSet attrs) {


        view = LayoutInflater.from(ctx).inflate(R.layout.simple_video_player, null, false);
        addView(view);

        textureView = (TextureView) view.findViewById(R.id.videoView);
        textureView.setSurfaceTextureListener(this);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        controls = new Controls(ctx, textureView, (ViewGroup) view);

        if (attrs == null) return;

        TypedArray types = null;

        try {

            types = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.SimpleVideoPlayer, 0, 0);

            controls.initWithStyles(types);

            autoPlayOnLoad = types.getBoolean(R.styleable.SimpleVideoPlayer_autoPlayOnLoad, false);
            videoUrl = types.getString(R.styleable.SimpleVideoPlayer_videoUrl);

            thumbnailDrawable = types.getDrawable(R.styleable.SimpleVideoPlayer_thumbnailDrawable);


        } finally {
            if (types != null) types.recycle();
        }


    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        if (!hasWindowFocus && mediaPlayer != null && !controls.isFullscreen()) {
            controls.getBtnPlay().setBackgroundResource(R.drawable.play);
            controls.showControls();
            mediaPlayer.pause();
        }
    }


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        this.mediaPlayer = mediaPlayer;

        mediaPlayer.setOnBufferingUpdateListener(this);

        controls.onPrepared(mediaPlayer);

        if (autoPlayOnLoad && !videoUrl.equals("")) {
            mediaPlayer.start();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
//        if (controls.getSeekBar().getProgress() == controls.getSeekBar().getMax()) {
//            controls.getBtnPlay().setBackgroundResource(R.drawable.replay);
//        } else {
//            controls.getBtnPlay().setBackgroundResource(R.drawable.play);
//        }
//
//        controls.showControls();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

        String msg = "Unknown";
        switch (extra){
            case MediaPlayer.MEDIA_ERROR_IO: msg = "Input"; break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED: msg = "Malformed"; break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED: msg = "Unsupported"; break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT: msg = "Time out"; break;
        }

        Toast.makeText(getContext(), "Error playing video (" + msg + ")", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        controls.getSeekBar().setSecondaryProgress((mediaPlayer.getDuration() * percent / 100) / 1000);
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                progressBar.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                progressBar.setVisibility(View.GONE);
                break;
        }
        return false;
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        if (mediaPlayer != null) {
            mediaPlayer.setSurface(new Surface(surfaceTexture));
            return;
        }


        try {

            mediaPlayer = new MediaPlayer();

            if (!videoUrl.equals("")) {
                mediaPlayer.setDataSource(videoUrl);
            }

            mediaPlayer.setSurface(new Surface(surfaceTexture));
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mediaPlayer.prepareAsync();

        } catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Error", ex);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }



}
