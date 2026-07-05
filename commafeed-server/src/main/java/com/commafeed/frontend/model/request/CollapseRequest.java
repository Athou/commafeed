package com.commafeed.frontend.model.request;

import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Mark Request")
@Data
public class CollapseRequest implements Serializable {

    @Schema(description = "category id", required = true)
    private Long id;

    @Schema(description = "collapse", required = true)
    private boolean collapse;
}
