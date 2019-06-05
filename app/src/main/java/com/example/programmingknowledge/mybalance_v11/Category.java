package com.example.programmingknowledge.mybalance_v11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Category {
    public static String[] s = {"library", "school", "university"};
    public static String[] e = {"gym", "park"};
    public static String[] l = {"amusement_park",
            "aquarium",
            "art_gallery",
            "bar",
            "beauty_salon",
            "bowling_alley",
            "cafe",
            "campground",
            "casino",
            "clothing_store",
            "department_store",
            "hair_care",
            "liquor_store",
            "movie_theater",
            "museum",
            "night_club",
            "rv_park",
            "shopping_mall",
            "spa",
            "stadium",
            "zoo",
            "book_store"};

    public static ArrayList<String> c_study = new ArrayList<>(Arrays.asList(s));
    public static ArrayList<String> c_exercise = new ArrayList<>(Arrays.asList(e));
    public static ArrayList<String> c_leisure = new ArrayList<>(Arrays.asList(l));


}
