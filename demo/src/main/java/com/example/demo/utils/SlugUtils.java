package com.example.demo.utils;

import com.github.slugify.Slugify;

public class SlugUtils {
    private static final Slugify slugify = Slugify.builder().build();
    public static String makeSlug(String input){
        return input == null ? "" : slugify.slugify(input);
    }
}
