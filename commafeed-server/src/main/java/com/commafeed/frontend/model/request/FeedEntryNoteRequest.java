package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;

@SuppressWarnings("serial")
@Schema(description = "Feed entry note request")
@Data
public class FeedEntryNoteRequest implements Serializable {

    @Schema(description = "entry id", required = true)
    @NotNull
    private Long entryId;

    @Schema(description = "note comment")
    private String comment;

    @Schema(description = "rating from 1 to 5")
    @Min(1)
    @Max(5)
    private Integer rating;
}
