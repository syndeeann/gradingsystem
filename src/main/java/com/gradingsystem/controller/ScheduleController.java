package com.gradingsystem.controller;

import com.gradingsystem.dto.common.PageResponse;
import com.gradingsystem.dto.schedule.ScheduleRequest;
import com.gradingsystem.dto.schedule.ScheduleResponse;
import com.gradingsystem.service.ScheduleService;
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
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedules", description = "Class schedule management")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // -----------------------------------------------------------------------
    // POST /api/schedules
    // -----------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SCHEDULES_CREATE')")
    @Operation(
        summary = "Create a new schedule",
        description = "Teacher must have role TEACHER. Returns 409 if teacher has a time conflict."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Schedule created"),
        @ApiResponse(responseCode = "404", description = "Subject or teacher not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Teacher has a conflicting schedule",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(request));
    }

    // -----------------------------------------------------------------------
    // GET /api/schedules
    // -----------------------------------------------------------------------

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULES_READ')")
    @Operation(summary = "List active schedules with optional filters and pagination")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Schedules returned"),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PageResponse<ScheduleResponse>> listSchedules(
        @Parameter(description = "Filter by teacher UUID")
        @RequestParam(required = false) String teacherId,

        @Parameter(description = "Filter by subject ID")
        @RequestParam(required = false) Integer subjectId,

        @Parameter(description = "Filter by semester (1, 2, or 3)")
        @RequestParam(required = false) Integer semester,

        @Parameter(description = "Filter by school year, e.g. 2024-2025")
        @RequestParam(required = false) String schoolYear,

        @Parameter(description = "Page index (0-based)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
            scheduleService.listSchedules(teacherId, subjectId, semester, schoolYear, page, size));
    }

    // -----------------------------------------------------------------------
    // GET /api/schedules/my-schedule  — teacher's own schedules
    // -----------------------------------------------------------------------

    @GetMapping("/my-schedule")
    @PreAuthorize("hasAuthority('SCHEDULES_READ')")
    @Operation(summary = "Get the schedules assigned to the currently authenticated teacher")
    public ResponseEntity<List<ScheduleResponse>> getMySchedule() {
        return ResponseEntity.ok(scheduleService.getMySchedule());
    }

    // -----------------------------------------------------------------------
    // GET /api/schedules/{id}
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULES_READ')")
    @Operation(summary = "Get a schedule by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Schedule found"),
        @ApiResponse(responseCode = "404", description = "Schedule not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScheduleResponse> getScheduleById(
        @Parameter(description = "Schedule ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scheduleService.getScheduleById(id));
    }

    // -----------------------------------------------------------------------
    // PUT /api/schedules/{id}
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULES_UPDATE')")
    @Operation(
        summary = "Update a schedule",
        description = "Teacher validation and conflict detection apply to updates as well."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Schedule updated"),
        @ApiResponse(responseCode = "404", description = "Schedule, subject, or teacher not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Teacher has a conflicting schedule",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "422", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScheduleResponse> updateSchedule(
        @Parameter(description = "Schedule ID") @PathVariable Integer id,
        @Valid @RequestBody ScheduleRequest request
    ) {
        return ResponseEntity.ok(scheduleService.updateSchedule(id, request));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/schedules/{id}/deactivate
    // -----------------------------------------------------------------------

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('SCHEDULES_UPDATE')")
    @Operation(summary = "Deactivate a schedule (soft delete)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Schedule deactivated"),
        @ApiResponse(responseCode = "404", description = "Schedule not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ScheduleResponse> deactivateSchedule(
        @Parameter(description = "Schedule ID") @PathVariable Integer id
    ) {
        return ResponseEntity.ok(scheduleService.deactivateSchedule(id));
    }
}
