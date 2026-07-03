package com.pipelinepro.adapter.in.web.v1;

import com.pipelinepro.adapter.in.web.mapper.AllocationWebMapper;
import com.pipelinepro.adapter.in.web.error.NotFoundWebException;
import com.pipelinepro.adapter.in.web.v1.dto.request.ExecuteAllocationRequest;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResponse;
import com.pipelinepro.adapter.in.web.v1.dto.response.AllocationResultResponse;
import com.pipelinepro.domain.PaymentAllocation;
import com.pipelinepro.domain.port.in.ExecuteAllocationUseCase;
import com.pipelinepro.domain.port.in.GetAllocationDetailUseCase;
import com.pipelinepro.domain.port.in.command.ExecuteAllocationCommand;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/allocations")
public class AllocationController {

    private static final Logger log = LoggerFactory.getLogger(AllocationController.class);

    private final ExecuteAllocationUseCase executeAllocationUseCase;
    private final GetAllocationDetailUseCase getAllocationDetailUseCase;
    private final AllocationWebMapper allocationWebMapper;

    public AllocationController(
            ExecuteAllocationUseCase executeAllocationUseCase,
            GetAllocationDetailUseCase getAllocationDetailUseCase,
            AllocationWebMapper allocationWebMapper) {
        this.executeAllocationUseCase = executeAllocationUseCase;
        this.getAllocationDetailUseCase = getAllocationDetailUseCase;
        this.allocationWebMapper = allocationWebMapper;
    }

    @PostMapping
    public ResponseEntity<AllocationResultResponse> createAllocation(@Valid @RequestBody ExecuteAllocationRequest request) {
        log.info("+++start createAllocation+++");
        try {
            Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();
            ExecuteAllocationCommand command = allocationWebMapper.toExecuteAllocationCommand(request, occurredAt);
            PaymentAllocation allocation = executeAllocationUseCase.executeAllocation(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(allocationWebMapper.toAllocationResultResponse(allocation));
        } finally {
            log.info("+++end createAllocation+++");
        }
    }

    @GetMapping("/{allocationId}")
    public ResponseEntity<AllocationResponse> getAllocation(@PathVariable UUID allocationId) {
        log.info("+++start getAllocation+++");
        try {
            PaymentAllocation allocation = getAllocationDetailUseCase.getAllocation(allocationId)
                    .orElseThrow(() -> new NotFoundWebException("Allocation not found: " + allocationId));
            return ResponseEntity.ok(allocationWebMapper.toAllocationResponse(allocation));
        } finally {
            log.info("+++end getAllocation+++");
        }
    }

}
