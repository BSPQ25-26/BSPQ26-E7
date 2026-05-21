package com.lablend.backend.controller;

import com.lablend.backend.entity.WaitingList;
import com.lablend.backend.service.WaitingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waiting-list")
@Tag(name = "Waiting List Management", description = "Endpoints for students to join and manage equipment queues.")
public class WaitingListController {

    @Autowired
    private WaitingListService waitingListService;

    @PostMapping("/join")
    @Operation(summary = "Join an equipment waiting list", description = "Places a student into the queue for an item that is currently loaned or under maintenance.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Successfully joined the waiting list",
                     content = @Content(schema = @Schema(implementation = WaitingList.class))),
        @ApiResponse(responseCode = "400", description = "Cannot join the waiting list for an equipment you currently borrow",
                     content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<WaitingList> joinQueue(@RequestBody WaitingListPayload payload) {
        WaitingList entry = waitingListService.addToQueue(payload.getUserId(), payload.getEquipmentId());
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @GetMapping("/queue/{equipmentId}")
    @Operation(summary = "Get queue for an equipment", description = "Fetches the list of students waiting for a specific piece of equipment.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of students in queue successfully retrieved")
    })
    public ResponseEntity<List<WaitingList>> getQueueForEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(waitingListService.getQueueForEquipment(equipmentId));
    }

    @Schema(description = "Payload required to request joining a waiting list")
    public static class WaitingListPayload {
        
        @Schema(description = "Unique identifier of the requesting user", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long userId;
        
        @Schema(description = "Unique identifier of the target equipment", example = "105", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long equipmentId;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getEquipmentId() { return equipmentId; }
        public void setEquipmentId(Long equipmentId) { this.equipmentId = equipmentId; }
    }
}