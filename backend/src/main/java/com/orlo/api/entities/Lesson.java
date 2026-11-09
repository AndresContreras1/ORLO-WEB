package com.orlo.api.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** Cápsula educativa de la sección Aprende. */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(name = "main_idea", nullable = false, length = 2000)
    private String mainIdea;

    @Column(nullable = false, length = 600)
    private String tip;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false)
    private int minutes;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "free_sample", nullable = false)
    private boolean freeSample;

    @Column(name = "uses_color_profile", nullable = false)
    private boolean usesColorProfile;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "review_question", length = 300)
    private String reviewQuestion;

    @ElementCollection
    @CollectionTable(name = "lesson_review_options", joinColumns = @JoinColumn(name = "lesson_id"))
    @OrderColumn(name = "position")
    @Column(name = "option_text", nullable = false, length = 200)
    private List<String> reviewOptions = new ArrayList<>();

    @Column(name = "review_correct_index")
    private Integer reviewCorrectIndex;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<LessonExample> examples = new ArrayList<>();

    public void replaceExamples(List<LessonExample> newExamples) {
        examples.clear();
        for (LessonExample example : newExamples) {
            example.setLesson(this);
            examples.add(example);
        }
    }
}
