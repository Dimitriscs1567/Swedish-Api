package com.example.swedishapi.api.v1.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "questions")
public class Question{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Lob
    @Column(name = "words", nullable = false)
    private List<Word> words;

    @Column(name = "right_word", nullable = false)
    private String rightWord;

    @Column(name = "answer", nullable = false)
    private String answer;

    @ManyToOne
    @JoinColumn(name = "test", nullable = false)
    private Test test;

    public Question(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public void addWord(Word word){
        if(words == null){
            words = new ArrayList<>();
        }

        words.add(word);
    }

    public String getRightWord() {
        return rightWord;
    }

    public void setRightWord(String rightWord) {
        this.rightWord = rightWord;
    }

    public String isAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Map<String, Object> toMap(){
        Map<String, Object> result = new HashMap<>();

        result.put("id", id);

        List<Map<String, Object>> wordsMap = new ArrayList<>();
        words.forEach(word -> {
            wordsMap.add(word.toMap());
        });

        result.put("words", wordsMap);
        result.put("rightWord", rightWord);
        result.put("answer", answer);

        return result;
    }
}