package com.nat.media_audio;

import android.media.MediaPlayer;
import android.os.Environment;

/**
 * Created by xuqinchao on 17/1/7.
 *  Copyright (c) 2017 Instapp. All rights reserved.
 */

public class AudioModule {

    private MediaPlayer mediaPlayer;
    private boolean mIsPausing;
    private boolean mIsPlaying;
    String mCurrentPath;

    private static volatile AudioModule instance = null;

    private AudioModule(){
    }

    public static AudioModule getInstance() {
        if (instance == null) {
            synchronized (AudioModule.class) {
                if (instance == null) {
                    instance = new AudioModule();
                }
            }
        }

        return instance;
    }
    
    public void play(String path, final ModuleResultListener listener) {
        if (listener == null)return;
        try {
            boolean valid = path.startsWith("http://") || path.startsWith("https://") || path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());

            if (path == null || path.equals("") || !valid){
                listener.onResult(Util.getError(Constant.MEDIA_SRC_NOT_SUPPORTED, Constant.MEDIA_SRC_NOT_SUPPORTED_CODE));
                return;
            }

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepareAsync();
            } else {
                if (mCurrentPath.equals(path)) {
                    if (mIsPausing) {
                        mediaPlayer.start();

                        mIsPlaying = true;
                        mIsPausing = false;

                        listener.onResult(null);
                    } else {
                        //listener.onResult(null);
                    }

                    return;
                } else {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepareAsync();
                }
            }

            mCurrentPath = path;

            // listener
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    mIsPlaying = true;
                    mIsPausing = false;

                    listener.onResult(null);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    switch (i1) {
                        case MediaPlayer.MEDIA_ERROR_IO:
                            listener.onResult(Util.getError(Constant.MEDIA_FILE_TYPE_NOT_SUPPORTED, Constant.MEDIA_FILE_TYPE_NOT_SUPPORTED_CODE));
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            listener.onResult(Util.getError(Constant.MEDIA_DECODE_ERROR, Constant.MEDIA_DECODE_ERROR_CODE));
                            break;
                    }
                    return false;
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();

                    mediaPlayer = null;
                    mIsPlaying = false;
                    mIsPausing = false;

                    //listener.onResult(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            listener.onResult(Util.getError(e.getMessage(), 1));
        }
    }

    public void pause(ModuleResultListener listener) {
        if (!mIsPlaying) {
            listener.onResult(Util.getError(Constant.MEDIA_PLAYER_NOT_STARTED, 1));
            return;
        }

        if (mIsPausing) {
            listener.onResult(null);
            return;
        }

        mediaPlayer.pause();
        mIsPausing = true;
        listener.onResult(null);
    }

    public void stop(ModuleResultListener listener) {
        if (listener == null)return;
        if (!mIsPlaying) {
            listener.onResult(Util.getError(Constant.MEDIA_PLAYER_NOT_STARTED, 1));
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();

            mediaPlayer = null;
            mIsPlaying = false;
            mIsPausing = false;

            listener.onResult(null);
        } else {
            listener.onResult(Util.getError(Constant.MEDIA_PLAYER_NOT_STARTED, 1));
        }
    }
}
