package dev.hrutvik.fraud.api;

import dev.hrutvik.fraud.rules.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@Validated @RestController @RequestMapping("/v1/rule-sets")
public class RuleSetController {
    private final RuleSetService service;
    public RuleSetController(RuleSetService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    RuleSetResponse create(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant,@Valid @RequestBody RuleSetDefinition definition){return service.createDraft(tenant,definition);}
    @PostMapping("/{id}/activation")
    RuleSetResponse activate(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant,@PathVariable UUID id){return service.activate(tenant,id);}
    @GetMapping("/active")
    RuleSetResponse active(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant){return service.getActive(tenant);}
}
