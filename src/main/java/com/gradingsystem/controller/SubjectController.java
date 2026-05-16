package com.gradingsystem.controller;

import com.gradingsystem.dto.subject.SubjectRequest;
import com.gradingsystem.dto.subject.SubjectResponse;
import com.gradingsystem.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject catalogue management")
public class SubjectController {

    private final SubjectService subjectService;

    // -----------------------------------------------------------------------
    // POST /api/subjects
    // -----------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SUBJECTS_CREATE')")
    @Operation(summary = "Create a new subject")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subject created"),
        @ApiResponse(responseCode = "409", description = "Subject code already exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SubjectResponse> createSubject(@Valid @RequestBody SubjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subjectService.createSubject(request));
    }

    // -----------------------------------------------------------------------
    // GET /api/subjects
    // -----------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('SUBJECTS_READ')")
    @Operation(summary = "List active subjects with optional search")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subjects returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<SubjectResponse>> listSubjects(
        @Parameter(description = "Search by code or name (case-insensitive)")
        @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(subjectService.listSubjects(search));
    }

    // -----------------------------------------------------------------------
    // GET /api/subjects/{id}
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECTS_READ')")
    @Operation(summary = "Get a subject by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subject found"),
        @ApiResponse(responseCode = "404", description = "Subject not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SubjectResponse> getSubjectById(
        @Parameter(description = "Subject ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(subjectService.getSubjectById(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/subjects/{id}
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUBJECTS_UPDATE')")
    @Operation(summary = "Update a subject")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subject updated"),
        @ApiResponse(responseCode = "404", description = "Subject not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Subject code already taken",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SubjectResponse> updateSubject(
        @Parameter(description = "Subject ID") @PathVariable Integer id,
        @Valid @RequestBody SubjectRequest request
    ) {
        return ResponseEntity.ok(subjectService.updateSubject(id, request));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/subjects/{id}/deactivate
    // -----------------------------------------------------------------------

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SUBJECTS_UPDATE')")
    @Operation(summary = "Deactivate a subject (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subject deactivated"),
        @ApiResponse(responseCode = "404", description = "Subject not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SubjectResponse> deactivateSubject(
        @Parameter(description = "Subject ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(subjectService.deactivateSubject(id));
    }
}
