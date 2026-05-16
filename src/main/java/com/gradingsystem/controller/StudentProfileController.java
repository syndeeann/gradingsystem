package com.gradingsystem.controller;

import com.gradingsystem.dto.student.StudentProfileResponse;
import com.gradingsystem.service.StudentProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student Profile", description = "Student academic profile — enrollments, grades, GPA, and honor status")
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    // -----------------------------------------------------------------------
    // GET /api/students/{studentId}/profile
    // -----------------------------------------------------------------------

    @GetMapping("/{studentId}/profile")
    @PreAuthorize("hasAuthority('STUDENTS_READ')")
    @Operation(
        summary = "Get a student's full academic profile",
        description = "Returns user info, all enrollments with subject/teacher/grade details, "
                    + "overall weighted GPA (by subject units), and honor classification."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned"),
        @ApiResponse(responseCode = "404", description = "Student not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<StudentProfileResponse> getProfile(
        @Parameter(description = "Student UUID") @PathVariable String studentId
    ) {
        return ResponseEntity.ok(studentProfileService.getProfile(studentId));
    }
}
