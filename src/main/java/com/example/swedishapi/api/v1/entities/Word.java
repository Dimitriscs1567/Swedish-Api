package com.example.swedishapi.api.v1.entities;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    
    @Column(name = "word", nullable = false, unique = true)
    private String word;
    
    @Column(name = "translation", nullable = false)
    private String translation;

    @Column(name = "notes")
    private String notes;

    public Word(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("word", word);
        result.put("translation", translation);
        result.put("notes", notes);

        return result;
    } 
}