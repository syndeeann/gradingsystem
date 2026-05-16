package com.gradingsystem.controller;

import com.gradingsystem.dto.grade.GradeRequest;
import com.gradingsystem.dto.grade.GradeResponse;
import com.gradingsystem.service.GradeService;
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
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade recording and retrieval")
public class GradeController {

    private final GradeService gradeService;

    // -----------------------------------------------------------------------
    // POST /api/grades
    // -----------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('GRADES_CREATE')")
    @Operation(
        summary = "Record a grade for an enrollment",
        description = "Only the teacher assigned to that schedule may record grades. "
                    + "Admins and Registrars may also record grades for any schedule."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Grade recorded"),
        @ApiResponse(responseCode = "403", description = "Caller is not the assigned teacher, Admin, or Registrar",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Enrollment not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Grade already recorded for this enrollment",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<GradeResponse> recordGrade(@Valid @RequestBody GradeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gradeService.recordGrade(request));
    }

    // -----------------------------------------------------------------------
    // PUT /api/grades/{id}
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('GRADES_UPDATE')")
    @Operation(
        summary = "Update a recorded grade",
        description = "Updates grade value, remarks, and resets recorded_by to the current caller."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Grade updated"),
        @ApiResponse(responseCode = "404", description = "Grade not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<GradeResponse> updateGrade(
        @Parameter(description = "Grade ID") @PathVariable Integer id,
        @Valid @RequestBody GradeRequest request
    ) {
        return ResponseEntity.ok(gradeService.updateGrade(id, request));
    }

    // -----------------------------------------------------------------------
    // GET /api/grades/by-schedule/{scheduleId}
    // -----------------------------------------------------------------------

    @GetMapping("/by-schedule/{scheduleId}")
    @PreAuthorize("hasAuthority('GRADES_READ')")
    @Operation(summary = "Get all student grades for a schedule (teacher grade sheet view)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Grades returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<GradeResponse>> getGradesBySchedule(
        @Parameter(description = "Schedule ID") @PathVariable Integer scheduleId
    ) {
        return ResponseEntity.ok(gradeService.getGradesBySchedule(scheduleId));
    }

    // -----------------------------------------------------------------------
    // GET /api/grades/by-student/{studentId}
    // -----------------------------------------------------------------------

    @GetMapping("/by-student/{studentId}")
    @PreAuthorize("hasAuthority('GRADES_READ')")
    @Operation(summary = "Get all grades across all subjects for a student")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Grades returned"),
        @ApiResponse(responseCode = "404", description = "Student not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<GradeResponse>> getGradesByStudent(
        @Parameter(description = "Student UUID") @PathVariable String studentId
    ) {
        return ResponseEntity.ok(gradeService.getGradesByStudent(studentId));
    }
}
