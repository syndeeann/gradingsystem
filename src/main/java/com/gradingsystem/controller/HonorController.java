package com.gradingsystem.controller;

import com.gradingsystem.dto.honor.HonorStudentDto;
import com.gradingsystem.dto.honor.HonorThresholdDto;
import com.gradingsystem.dto.honor.HonorThresholdRequest;
import com.gradingsystem.service.HonorService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/honors")
@RequiredArgsConstructor
@Tag(name = "Honors", description = "Honor threshold management and honor roll queries")
public class HonorController {

    private final HonorService honorService;

    // -----------------------------------------------------------------------
    // GET /api/honors/thresholds  — readable by any authenticated user
    // -----------------------------------------------------------------------

    @GetMapping("/thresholds")
    @Operation(summary = "List all active honor thresholds ordered by minimum grade")
    public ResponseEntity<List<HonorThresholdDto>> getThresholds() {
        return ResponseEntity.ok(honorService.getThresholds());
    }

    // -----------------------------------------------------------------------
    // POST /api/honors/thresholds
    // -----------------------------------------------------------------------

    @PostMapping("/thresholds")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('HONORS_CREATE')")
    @Operation(summary = "Create a new honor threshold")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Threshold created"),
        @ApiResponse(responseCode = "409", description = "Label already exists",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<HonorThresholdDto> createThreshold(
        @Valid @RequestBody HonorThresholdRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(honorService.createThreshold(request));
    }

    // -----------------------------------------------------------------------
    // PUT /api/honors/thresholds/{id}
    // -----------------------------------------------------------------------

    @PutMapping("/thresholds/{id}")
    @PreAuthorize("hasAuthority('HONORS_UPDATE')")
    @Operation(summary = "Update an honor threshold")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Threshold updated"),
        @ApiResponse(responseCode = "404", description = "Threshold not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Label already taken by another threshold",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<HonorThresholdDto> updateThreshold(
        @Parameter(description = "Threshold ID") @PathVariable Integer id,
        @Valid @RequestBody HonorThresholdRequest request
    ) {
        return ResponseEntity.ok(honorService.updateThreshold(id, request));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/honors/thresholds/{id}  — soft delete (deactivate)
    // -----------------------------------------------------------------------

    @DeleteMapping("/thresholds/{id}")
    @PreAuthorize("hasAuthority('HONORS_UPDATE')")
    @Operation(summary = "Deactivate an honor threshold (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Threshold deactivated"),
        @ApiResponse(responseCode = "404", description = "Threshold not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<HonorThresholdDto> deactivateThreshold(
        @Parameter(description = "Threshold ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(honorService.deactivateThreshold(id));
    }

    // -----------------------------------------------------------------------
    // GET /api/honors/students
    // -----------------------------------------------------------------------

    @GetMapping("/students")
    @PreAuthorize("hasAuthority('HONORS_READ')")
    @Operation(
        summary = "List honor students for a given term",
        description = "Returns students whose weighted GPA qualifies under an active honor threshold, "
                    + "ranked by GPA descending. All query params are optional."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Honor roll returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<HonorStudentDto>> getHonorStudents(
        @Parameter(description = "Semester (1, 2, or 3)")
        @RequestParam(required = false) Integer semester,

        @Parameter(description = "School year, e.g. 2024-2025")
        @RequestParam(required = false) String schoolYear,

        @Parameter(description = "Minimum GPA filter")
        @RequestParam(required = false) BigDecimal minGrade,

        @Parameter(description = "Filter by honor label, e.g. WITH HONORS")
        @RequestParam(required = false) String honorLabel
    ) {
        return ResponseEntity.ok(
            honorService.getHonorStudents(semester, schoolYear, minGrade, honorLabel));
    }
}
