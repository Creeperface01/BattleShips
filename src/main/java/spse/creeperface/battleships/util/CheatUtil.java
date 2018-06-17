package spse.creeperface.battleships.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import spse.creeperface.battleships.game.Game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author CreeperFace
 */
public class CheatUtil {

    private static final int CHAR_CACHE_DELAY = 20000;
    private static final int DELAY_BETWEEN_CHARS = 1000;

    private static final Map<String, Consumer<Game>> cheats = new HashMap<>();

    private final Deque<Entry> chars = new ArrayDeque<>();

    private final Game game;

    public CheatUtil(Game game) {
        this.game = game;
    }

    public void onCharTyped(char c) {
        String input = getCurrentInput(c);

        Consumer<Game> cheat = cheats.get(input);

        if (cheat != null) {
            cheat.accept(this.game);
            this.chars.clear();
            return;
        }

        this.chars.push(Entry.of(System.currentTimeMillis(), c));
    }

    private String getCurrentInput(char c) {
        long time = System.currentTimeMillis();

        Entry first = chars.getLast();

        if (time - first.getTime() > DELAY_BETWEEN_CHARS) {
            chars.clear();
            return String.valueOf(c);
        }

        StringBuilder input = new StringBuilder();

        while (!chars.isEmpty()) {
            Entry entry = chars.getLast();

            if (time - entry.getTime() > CHAR_CACHE_DELAY) {
                chars.removeLast();
                continue;
            }

            input.append(entry.getCharacter());
        }

        return input.toString();
    }

    public static void addCheat(String key, Consumer<Game> cheat) {
        cheats.put(key, cheat);
    }

    @AllArgsConstructor(staticName = "of")
    @Getter(value = AccessLevel.PRIVATE)
    private static class Entry {

        private final long time;
        private final char character;
    }
}