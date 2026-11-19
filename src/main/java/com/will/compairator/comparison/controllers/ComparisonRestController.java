package com.will.compairator.comparison.controllers;

import com.will.compairator.comparison.dto.FeedbackDTO;
import com.will.compairator.comparison.services.ComparisonService;
import com.will.compairator.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/comparisons")
public class ComparisonRestController {
    private final ComparisonService comparisons;
    private final UserService users;
    public ComparisonRestController(ComparisonService comparisons, UserService users) {
        this.comparisons = comparisons;
        this.users = users;
    }

    @PutMapping("/{id}/feedback")
    public FeedbackDTO feedback(@PathVariable UUID id, @Valid @RequestBody FeedbackDTO feedback, Principal principal) {
        return comparisons.evaluate(id, users.find(principal.getName()).getId(), feedback);
    }
}
