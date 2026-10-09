package dev.hrutvik.fraud.feedback;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.UUID;

public record FeedbackRequest(@NotNull UUID decisionId,@NotNull FeedbackLabel label,@NotNull FeedbackSource source,@NotBlank @Size(max=160) String externalReference,@NotNull @PastOrPresent Instant observedAt) {}
