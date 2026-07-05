package com.commafeed.frontend.model.request;

import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema
@Data
public class IDRequest implements Serializable {

    @Schema(required = true)
    private Long id;
}
