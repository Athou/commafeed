package com.commafeed.frontend.resource;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.service.LLMRewriteService;
import com.commafeed.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/rest/entry")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
public class LLMRewriteREST {

    private final FeedEntryDAO entryDAO;
    private final LLMRewriteService rewriteService;

    @POST
    @Path("/{id}/generate-alternative")
    @Transactional
    public Response generateAlternative(@PathParam("id") Long id, RewriteRequest req) {
        FeedEntry entry = entryDAO.findById(id);
        if (entry == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Entry with ID " + id + " not found")
                    .build();
        }

        String originalText;
        if ("title".equalsIgnoreCase(req.target())) {
            originalText = entry.getContent().getTitle();
        } else if ("content".equalsIgnoreCase(req.target())) {
            originalText = entry.getContent().getContent();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid target. Allowed values: 'title', 'content'")
                    .build();
        }

        try {
            String alternative =
                    rewriteService.generateAlternative(originalText, req.target(), req.prompt());
            return Response.ok(
                            new RewriteResponse(
                                    id, req.target(), req.prompt(), originalText, alternative))
                    .build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("LLM service is not configured properly: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_GATEWAY)
                    .entity("Failed to communicate with LLM provider: " + e.getMessage())
                    .build();
        }
    }

    public record RewriteRequest(String target, String prompt) {}

    public record RewriteResponse(
            Long entryId,
            String target,
            String prompt,
            String originalText,
            String generatedAlternative) {}
}
