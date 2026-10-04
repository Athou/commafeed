package com.commafeed.frontend.model;

import com.commafeed.backend.model.FeedEntryNote;

import io.quarkus.runtime.annotations.RegisterForReflection;

import lombok.Data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
@Schema(description = "Feed entry note")
@Data
@RegisterForReflection
public class FeedEntryNoteModel implements Serializable {

    @Schema(description = "note id", required = true)
    private Long id;

    @Schema(description = "entry id", required = true)
    private Long entryId;

    @Schema(description = "note comment")
    private String comment;

    @Schema(description = "rating from 1 to 5")
    private Integer rating;

    @Schema(description = "note creation date", required = true)
    private Date created;

    public static FeedEntryNoteModel from(FeedEntryNote note) {
        FeedEntryNoteModel model = new FeedEntryNoteModel();
        model.setId(note.getId());
        model.setEntryId(note.getFeedEntry().getId());
        model.setComment(note.getComment());
        model.setRating(note.getRating());
        model.setCreated(note.getCreated());
        return model;
    }
}
