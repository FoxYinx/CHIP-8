package fr.yinxfox.emulator;

import fr.yinxfox.Launcher;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundMaker extends Thread {

    //fixme: sound is not being played if sound timer is too small

    private int pitch;
    private float freq;
    private byte[] buffer;

    private volatile int prevAudioTone;
    private volatile int prevSoundDelay;

    public SoundMaker() {
        this.pitch = 64;
        this.freq = pitchToFreq(this.pitch);

        this.start();
    }

    @Override
    public void run() {
        while (true) {
            if (Launcher.isAudioPlaying()) {
                if (ExecutionWorker.getSoundTimer() == 0) prevSoundDelay = 0;
                if (prevSoundDelay > ExecutionWorker.getSoundTimer()) {
                    if (prevAudioTone == pitch) continue;
                }
                try {
                    int timer = Math.max(1, Byte.toUnsignedInt((byte) ExecutionWorker.getSoundTimer()));
                    tone(this.buffer);
                    prevAudioTone = pitch;
                    prevSoundDelay = timer;
                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void tone(byte[] buffer) throws LineUnavailableException {
        AudioFormat af = new AudioFormat(freq, 8, 1, true, false);
        SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
        sdl.open(af);
        sdl.start();
        for (int i = 0; i < 16; i++) {
            if (buffer[i] == (byte) 255) {
                buffer[i] = (byte) 0x38;
            }
            sdl.write(buffer, 0, 16);
        }
        sdl.drain();
        sdl.stop();
        sdl.close();
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    private float pitchToFreq(int pitch) {
        return (float) (4000 * Math.pow(2, (pitch - 64f) / 48f));
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
