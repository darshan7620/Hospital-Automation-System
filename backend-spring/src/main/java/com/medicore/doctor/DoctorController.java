package com.medicore.doctor;

import com.medicore.common.dto.PageResponse;
import com.medicore.doctor.dto.DoctorRequest;
import com.medicore.doctor.dto.DoctorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctors")
public class DoctorController {

    private static final int MAX_PAGE_SIZE = 100;

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a doctor")
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return doctorService.create(request);
    }

    @GetMapping
    @Operation(summary = "List doctors with optional filters")
    public PageResponse<DoctorResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        int boundedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                boundedSize,
                Sort.by(Sort.Direction.fromString(direction), sort));
        return doctorService.list(name, specialization, departmentId, active, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a doctor by id")
    public DoctorResponse getById(@PathVariable Long id) {
        return doctorService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a doctor")
    public DoctorResponse update(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate a doctor (soft delete)")
    public void deactivate(@PathVariable Long id) {
        doctorService.deactivate(id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Reactivate a doctor")
    public DoctorResponse activate(@PathVariable Long id) {
        return doctorService.activate(id);
    }
}
