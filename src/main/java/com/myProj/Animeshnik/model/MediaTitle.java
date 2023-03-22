package com.myProj.Animeshnik.model;

public class MediaTitle {
    private String romaji;
    private String english;
    private String nativeL;

    public MediaTitle(String romaji, String english, String nativeL) {
        this.romaji = romaji;
        this.english = english;
        this.nativeL = nativeL;
    }

    public String getRomaji() {
        return romaji;
    }

    public String getEnglish() {
        return english;
    }

    public String getNative() {
        return nativeL;
    }
}

