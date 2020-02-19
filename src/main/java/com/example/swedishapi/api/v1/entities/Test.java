package com.example.swedishapi.api.v1.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tests")
public class Test{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToMany(mappedBy = "test", cascade = CascadeType.REMOVE)
    List<Question> questions;

    @Column(name = "result")
    private int result;

    @ManyToOne
    @JoinColumn(name = "testing_user", nullable = false)
    private User testingUser;

    public Test(){}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question){
        if(questions == null){
            questions = new ArrayList<>();
        }

        questions.add(question);
        question.setTest(this);
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public User getTestingUser() {
        return testingUser;
    }

    public void setTestingUser(User user) {
        this.testingUser = user;
    }
}
