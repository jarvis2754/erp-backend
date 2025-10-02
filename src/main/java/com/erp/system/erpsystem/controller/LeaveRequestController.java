package com.erp.system.erpsystem.controller;

import com.erp.system.erpsystem.dto.leave.CreateLeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.LeaveRequestDto;
import com.erp.system.erpsystem.dto.leave.UpdateLeaveStatusDto;
import com.erp.system.erpsystem.service.LeaveRequestService;
import com.erp.system.erpsystem.utils.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService,JwtUtil jwtUtil) {
        this.leaveRequestService = leaveRequestService;
        this.jwtUtil =jwtUtil;
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
    public ResponseEntity<Page<LeaveRequestDto>> listByOrg(@PathVariable String orgId,
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
    public ResponseEntity<?> listByUser(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(leaveRequestService.listByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }

    @GetMapping("/users/history")
    public ResponseEntity<?> listCurrentUserAttendance(@RequestHeader("Authorization") String authHeader) {
        try {
            String token =authHeader.substring(7);
            return ResponseEntity.ok(leaveRequestService.listByCurrentUser(jwtUtil.extractUserId(token)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/leaves/{id}")
    public ResponseEntity<LeaveRequestDto> update(@PathVariable Integer id,
                                                  @Valid @RequestBody CreateLeaveRequestDto dto) {
        return ResponseEntity.ok(leaveRequestService.update(id, dto));
    }

    @PatchMapping("/leaves/{id}/status")
    public ResponseEntity<?> changeStatus(@RequestHeader("Authorization") String authHeader,
                                                        @PathVariable Integer id,
                                                        @Valid @RequestBody UpdateLeaveStatusDto dto) {
        try {
            String token = authHeader.substring(7);
            return ResponseEntity.ok(leaveRequestService.changeStatus(token, id, dto));
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
