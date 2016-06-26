package com.danielkim.soundrecorder;

import android.media.MediaMetadataRetriever;

public class MediaHelper {

    public static long calculateAudiobookDuration(String filePath) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(filePath);
        long durationInMilliseconds = Long.parseLong(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        metaRetriever.release();
        return durationInMilliseconds;
    }
}
