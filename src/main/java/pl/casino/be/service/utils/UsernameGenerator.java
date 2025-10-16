package pl.casino.be.service.utils;

import java.util.List;
import java.util.Random;

public class UsernameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "Lucky", "Golden", "Cosmic", "Happy", "Clever", "Silent", "Shadow", "Royal", "Diamond", "Atomic"
    );
    private static final List<String> NOUNS = List.of(
            "Panda", "Lion", "Tiger", "Fox", "Shark", "Wolf", "Cobra", "Eagle", "Joker", "Phantom"
    );
    private static final Random RANDOM = new Random();

    // Private constructor to prevent instantiation
    private UsernameGenerator() {}

    public static String generateFakeUsername() {
        String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        int number = 1 + RANDOM.nextInt(99);
        return String.format("%s%s%02d", adjective, noun, number);
    }
}