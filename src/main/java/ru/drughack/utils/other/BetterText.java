package ru.drughack.utils.other;

import lombok.Getter;

import java.util.List;

@Getter
public class BetterText {
    private final List<String> texts;
    private final String text;
    private final int delay;
    private String output = "";
    private boolean running = false;

    public BetterText(List<String> texts, int delay) {
        this.texts = texts;
        this.delay = delay;
        this.text = null;
    }

    public BetterText(String text, int delay) {
        this.texts = null;
        this.delay = delay;
        this.text = text;
    }

    public void update() {
        if (running) return;
        running = true;
        if (text == null) {
            new Thread(() -> {
                try {
                    int index = 0;
                    while (true) {
                        String currentText = texts.get(index);
                        for (int i = 0; i < currentText.length(); i++) {
                            output += currentText.charAt(i);
                            Thread.sleep(100);
                        }
                        Thread.sleep(delay);
                        for (int i = output.length(); i >= 0; i--) {
                            output = output.substring(0, i);
                            Thread.sleep(60);
                        }
                        if (index >= texts.size() - 1) index = 0;
                        else index += 1;
                        Thread.sleep(400);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        } else if (texts == null) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < text.length(); i++) {
                        output += text.charAt(i);
                        Thread.sleep(delay);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}