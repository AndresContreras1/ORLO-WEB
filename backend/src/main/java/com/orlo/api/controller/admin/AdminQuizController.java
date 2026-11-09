package com.orlo.api.controller.admin;

import com.orlo.api.dto.admin.AdminQuestionDto;
import com.orlo.api.dto.admin.QuestionRequest;
import com.orlo.api.service.QuizService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/** Preguntas del cuestionario con su tabla de puntos por estilo. */
@RestController
@RequestMapping("/api/v1/admin/quiz")
@Tag(name = "Panel · Cuestionario")
@SecurityRequirement(name = "admin")
public class AdminQuizController {

    private final QuizService quizService;

    public AdminQuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public List<AdminQuestionDto> findAll() {
        return quizService.findAllQuestions();
    }

    @GetMapping("/{id}")
    public AdminQuestionDto findById(@PathVariable Long id) {
        return quizService.findQuestion(id);
    }

    @PostMapping
    public ResponseEntity<AdminQuestionDto> create(@Valid @RequestBody QuestionRequest request) {
        AdminQuestionDto created = quizService.createQuestion(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AdminQuestionDto update(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return quizService.updateQuestion(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        quizService.deleteQuestion(id);
    }
}
