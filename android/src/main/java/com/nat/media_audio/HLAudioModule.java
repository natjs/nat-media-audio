package com.nat.media_audio;

import android.media.MediaPlayer;
import android.os.Environment;

/**
 * Created by xuqinchao on 17/1/7.
 *  Copyright (c) 2017 Nat. All rights reserved.
 */

public class HLAudioModule {

    private MediaPlayer mediaPlayer;
    private boolean mIsPausing;
    private boolean mIsPlaying;
    String mCurrentPath;

    private static volatile HLAudioModule instance = null;

    private HLAudioModule(){
    }

    public static HLAudioModule getInstance() {
        if (instance == null) {
            synchronized (HLAudioModule.class) {
                if (instance == null) {
                    instance = new HLAudioModule();
                }
            }
        }

        return instance;
    }
    
    public void play(String path, final HLModuleResultListener listener) {
        if (listener == null)return;
        try {
            boolean vilaid = path.startsWith("http://") || path.startsWith("https://") || path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (path == null || path.equals("") || !vilaid){
                listener.onResult(HLUtil.getError(HLConstant.MEDIA_SRC_NOT_SUPPORTED, HLConstant.MEDIA_SRC_NOT_SUPPORTED_CODE));
                return;
            }

            if (!mIsPausing && mIsPlaying) {
                if (!mCurrentPath.equals(path)) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                            switch (i1) {
                                case MediaPlayer.MEDIA_ERROR_IO:
                                    listener.onResult(HLUtil.getError(HLConstant.MEDIA_FILE_TYPE_NOT_SUPPORTED, HLConstant.MEDIA_FILE_TYPE_NOT_SUPPORTED_CODE));
                                    break;
                                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                    listener.onResult(HLUtil.getError(HLConstant.MEDIA_DECODE_ERROR, HLConstant.MEDIA_DECODE_ERROR_CODE));
                                    break;
                            }
                            return false;
                        }
                    });
                    mIsPlaying = true;
                    mIsPausing = false;
                    listener.onResult(null);
                } else {
                    listener.onResult(null);
                }
            } else {
                if (mIsPausing) {
                    if (mediaPlayer != null) {
                        mediaPlayer.start();
                        mIsPausing = false;
                        listener.onResult(null);
                    } else {
                        listener.onResult(HLUtil.getError("播放器异常", 1));
                    }
                } else {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.prepareAsync();
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                        @Override
                        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                            switch (i1) {
                                case MediaPlayer.MEDIA_ERROR_IO:
                                    listener.onResult(HLUtil.getError(HLConstant.MEDIA_FILE_TYPE_NOT_SUPPORTED, HLConstant.MEDIA_FILE_TYPE_NOT_SUPPORTED_CODE));
                                    break;
                                case MediaPlayer.MEDIA_ERROR_MALFORMED:
                                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                    listener.onResult(HLUtil.getError(HLConstant.MEDIA_DECODE_ERROR, HLConstant.MEDIA_DECODE_ERROR_CODE));
                                    break;
                            }
                            return false;
                        }
                    });
                    mIsPlaying = true;
                    mIsPausing = false;
                    listener.onResult(null);
                }
            }
            mCurrentPath = path;
        } catch (Exception e) {
            e.printStackTrace();
            listener.onResult(HLUtil.getError(e.getMessage(), 1));
        }
    }

    public void pause(HLModuleResultListener listener) {
        if (!mIsPlaying){
            listener.onResult(HLUtil.getError(HLConstant.MEDIA_PLAYER_NOT_STARTED, 1));
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

    public void stop(HLModuleResultListener listener) {
        if (listener == null)return;
        if (!mIsPlaying) {
            listener.onResult(HLUtil.getError(HLConstant.MEDIA_PLAYER_NOT_STARTED, 1));
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            mIsPlaying = false;
            mIsPausing = false;
            listener.onResult(null);
        }else {
            listener.onResult(HLUtil.getError(HLConstant.MEDIA_PLAYER_NOT_STARTED, 1));
        }

    }
}
