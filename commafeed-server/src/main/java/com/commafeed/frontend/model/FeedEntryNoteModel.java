package com.commafeed.frontend.model;

import com.commafeed.backend.model.FeedEntryNote;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.io.Serializable;
import lombok.Data;

@Data
@RegisterForReflection
public class FeedEntryNoteModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long entryId;

    private String note;

    private Integer rating;

    public static FeedEntryNoteModel build(FeedEntryNote entity) {
        FeedEntryNoteModel model = new FeedEntryNoteModel();

        model.setId(entity.getId());
        model.setEntryId(entity.getEntry().getId());
        model.setNote(entity.getNote());
        model.setRating(entity.getRating());

        return model;
    }
}
