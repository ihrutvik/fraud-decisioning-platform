package dev.hrutvik.fraud.api;

import dev.hrutvik.fraud.review.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Validated @RestController @RequestMapping("/v1/reviews")
public class ReviewController {
    private final ReviewService service;
    public ReviewController(ReviewService service){this.service=service;}
    @GetMapping public List<ReviewResponse> queue(@RequestHeader("X-Tenant-Id") @NotBlank String tenant){return service.queue(tenant);}
    @PostMapping("/{id}/claim") public ReviewResponse claim(@RequestHeader("X-Tenant-Id") @NotBlank String tenant,@RequestHeader("X-Analyst-Id") @NotBlank String analyst,@PathVariable UUID id,@Valid @RequestBody ClaimRequest request){return service.claim(tenant,id,analyst,request.leaseSeconds());}
    @PostMapping("/{id}/resolution") public ReviewResponse resolve(@RequestHeader("X-Tenant-Id") @NotBlank String tenant,@RequestHeader("X-Analyst-Id") @NotBlank String analyst,@PathVariable UUID id,@Valid @RequestBody ResolutionRequest request){return service.resolve(tenant,id,analyst,request.resolution(),request.note());}
    public record ClaimRequest(@Min(30) @Max(3600) long leaseSeconds){}
    public record ResolutionRequest(@NotNull ReviewResolution resolution,@Size(max=1000) String note){}
}
