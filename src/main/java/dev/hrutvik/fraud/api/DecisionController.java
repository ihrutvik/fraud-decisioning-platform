package dev.hrutvik.fraud.api;

import dev.hrutvik.fraud.decision.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/v1/decisions")
public class DecisionController {
    private final DecisionService service;
    public DecisionController(DecisionService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public DecisionResponse decide(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant,
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max=100) String key, @Valid @RequestBody DecisionRequest request) {
        return service.decide(tenant,key,request);
    }
    @GetMapping("/{id}")
    public DecisionResponse get(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant, @PathVariable UUID id) { return service.get(tenant,id); }
}
