package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.leave.CreateLeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.LeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.UpdateLeaveStatusDto;
import com.erp.system.erpsystem.service.LeaveRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping("/orgs/leaves")
    public ResponseEntity<?> createLeave(@RequestHeader("Authorization") String authHeader,
                                                       @Valid @RequestBody CreateLeaveRequestDto dto) {
        String token = authHeader.substring(7);
        try{
            LeaveRequestDto created = leaveRequestService.create(dto, token);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/orgs/{orgId}/leaves")
    public ResponseEntity<Page<LeaveRequestDto>> listByOrg(@PathVariable Integer orgId,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveRequestDto> result = leaveRequestService.listByOrganization(orgId, pageable, status);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/leaves/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(leaveRequestService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/leaves")
    public ResponseEntity<List<LeaveRequestDto>> listByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(leaveRequestService.listByUser(userId));
    }

    @PutMapping("/leaves/{id}")
    public ResponseEntity<LeaveRequestDto> update(@PathVariable Integer id,
                                                  @Valid @RequestBody CreateLeaveRequestDto dto) {
        return ResponseEntity.ok(leaveRequestService.update(id, dto));
    }

    @PatchMapping("/leaves/{id}/status")
    public ResponseEntity<LeaveRequestDto> changeStatus(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable Integer id,
                                                        @Valid @RequestBody UpdateLeaveStatusDto dto) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(leaveRequestService.changeStatus(token, id, dto));
    }

    @GetMapping("/leaves/pending")
    public ResponseEntity<List<LeaveRequestDto>> pendingApprovals(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<LeaveRequestDto> pending = leaveRequestService.pendingApprovals(token);
        return ResponseEntity.ok(pending);
    }

    @DeleteMapping("/leaves/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try{
            leaveRequestService.delete(id);
            return ResponseEntity.ok().build();
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
