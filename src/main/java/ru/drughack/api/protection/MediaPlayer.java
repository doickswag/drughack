package ru.drughack.api.protection;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter @Setter
public class MediaPlayer {

    private BufferedImage image;
    private String title = "", artist = "", owner = "", lastTitle = "";
    private long duration = 0, position = 0;
    private boolean changeTrack;
    private IMediaSession session;
    private List<IMediaSession> sessions;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void onTick() {
        executor.submit(() -> {
            sessions = MediaPlayerInfo.Instance.getMediaSessions();
            session = sessions.stream()
                    .filter(s -> (!s.getMedia().getArtist().isEmpty() || !s.getMedia().getTitle().isEmpty()))
                    .findFirst()
                    .orElse(null);
            if (session == null) return;

            MediaInfo info = session.getMedia();

            title = info.getTitle();
            artist = info.getArtist();
            duration = info.getDuration();
            position = info.getPosition();
            image = info.getArtwork();
            owner = session.getOwner();
            if (lastTitle == null || !lastTitle.equals(title)) {
                changeTrack = true;
                lastTitle = title;
            }
        });
    }

    private String Formatted(long duration) {
        long minutes = duration / 60;
        long seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getCurrentPosition() {
        return Formatted(position);
    }

    public String getCurrentDuration() {
        return Formatted(duration);
    }

    public float getWidth() {
        if (duration <= 0 || position <= 0) return 0;
        return Math.min(1f, (float) position / duration);
    }

    public boolean fullNullCheck() {
        return title.isEmpty() || artist.isEmpty() || owner.isEmpty();
    }
}