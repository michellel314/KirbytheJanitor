import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class Music {
    private Clip clip;
    private String soundURL;

    public Music(String filePath) {
        soundURL = filePath;
        loadSound();
    }

    private void loadSound() {
        try {
            File musicFile = new File(soundURL);
            if (musicFile.exists()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(musicFile);
                clip = AudioSystem.getClip();
                clip.open(ais);
            } else {
                System.out.println("File not found: " + soundURL);
            }
        } catch (Exception e) {
            System.out.println("Error loading sound: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void playOnce() {
        if (clip != null) {
            clip.setFramePosition(0); // rewind
            clip.start();
        }
    }

    public void playLoop() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    public void close() {
        if (clip != null) {
            clip.close();
        }
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }
}
