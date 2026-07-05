package com.commafeed.frontend.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("serial")
@Schema(description = "Add Category Request")
@Data
public class AddCategoryRequest implements Serializable {

    @Schema(description = "name", required = true)
    @NotEmpty
    @Size(max = 128)
    private String name;

    @Schema(description = "parent category id, if any")
    @Size(max = 128)
    private String parentId;
}
