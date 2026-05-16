package com.gradingsystem.controller;

import com.gradingsystem.dto.enrollment.EnrollmentRequest;
import com.gradingsystem.dto.enrollment.EnrollmentResponse;
import com.gradingsystem.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Student enrollment management")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // -----------------------------------------------------------------------
    // POST /api/enrollments
    // -----------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ENROLLMENTS_CREATE')")
    @Operation(
        summary = "Enroll a student in a schedule",
        description = "Student must have role STUDENT. Previously DROPPED students may re-enroll."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Student enrolled"),
        @ApiResponse(responseCode = "404", description = "Student or schedule not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Student is already enrolled",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    // -----------------------------------------------------------------------
    // GET /api/enrollments/{id}
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENTS_READ')")
    @Operation(summary = "Get enrollment by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollment found"),
        @ApiResponse(responseCode = "404", description = "Enrollment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(
        @Parameter(description = "Enrollment ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(id));
    }

    // -----------------------------------------------------------------------
    // GET /api/enrollments/by-student/{studentId}
    // -----------------------------------------------------------------------

    @GetMapping("/by-student/{studentId}")
    @PreAuthorize("hasAuthority('ENROLLMENTS_READ')")
    @Operation(summary = "Get all enrollments for a student")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollments returned"),
        @ApiResponse(responseCode = "404", description = "Student not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(
        @Parameter(description = "Student UUID") @PathVariable String studentId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    // -----------------------------------------------------------------------
    // GET /api/enrollments/by-schedule/{scheduleId}
    // -----------------------------------------------------------------------

    @GetMapping("/by-schedule/{scheduleId}")
    @PreAuthorize("hasAuthority('ENROLLMENTS_READ')")
    @Operation(summary = "Get all enrollments for a schedule (roster)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollments returned"),
        @ApiResponse(responseCode = "404", description = "Schedule not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsBySchedule(
        @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsBySchedule(scheduleId));
    }

    // -----------------------------------------------------------------------
    // GET /api/enrollments/my-subjects  — student's own active enrollments
    // -----------------------------------------------------------------------

    @GetMapping("/my-subjects")
    @PreAuthorize("hasAuthority('ENROLLMENTS_READ')")
    @Operation(summary = "Get the current student's active enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getMySubjects() {
        return ResponseEntity.ok(enrollmentService.getMySubjects());
    }

    // -----------------------------------------------------------------------
    // DELETE /api/enrollments/{id}  — drop enrollment
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENTS_UPDATE')")
    @Operation(summary = "Drop a student from a schedule (sets status to DROPPED)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Enrollment dropped"),
        @ApiResponse(responseCode = "404", description = "Enrollment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<EnrollmentResponse> dropEnrollment(
        @Parameter(description = "Enrollment ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(enrollmentService.dropEnrollment(id));
    }
}
