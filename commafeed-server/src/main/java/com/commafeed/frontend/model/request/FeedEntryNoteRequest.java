package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedEntryNoteRequest {

    @NotNull private Long entryId;

    @NotBlank
    @Size(max = 1000)
    private String note;

    @Min(1)
    @Max(5)
    private Integer rating;
}
