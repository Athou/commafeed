package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Tag Request")
@Data
public class TagRequest implements Serializable {

    @Schema(description = "entry id", required = true)
    private Long entryId;

    @Schema(description = "tags", required = true)
    private List<String> tags;
}
