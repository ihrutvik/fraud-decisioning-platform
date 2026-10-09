package dev.hrutvik.fraud.api;

import dev.hrutvik.fraud.feedback.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Validated @RestController @RequestMapping("/v1/feedback")
public class FeedbackController {
    private final FeedbackService service;
    public FeedbackController(FeedbackService service){this.service=service;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse ingest(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant,@RequestHeader("Idempotency-Key") @NotBlank @Size(max=100) String key,@Valid @RequestBody FeedbackRequest request){return service.ingest(tenant,key,request);}
    @GetMapping("/decisions/{decisionId}") public List<FeedbackResponse> history(@RequestHeader("X-Tenant-Id") @NotBlank @Size(max=80) String tenant,@PathVariable UUID decisionId){return service.history(tenant,decisionId);}
}
