package com.commafeed.frontend.model.request;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Multiple Mark Request")
@Data
public class MultipleMarkRequest implements Serializable {

    @Schema(description = "list of mark requests", required = true)
    private List<@Valid MarkRequest> requests;
}
