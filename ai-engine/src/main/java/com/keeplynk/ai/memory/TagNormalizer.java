package com.keeplynk.ai.memory;

public class TagNormalizer {

    public static String normalize(String tag) {
        return tag.toLowerCase()
                  .trim()
                  .replaceAll("[^a-z0-9 ]", "")
                  .replaceAll("\\s+", "-");
    }
}
