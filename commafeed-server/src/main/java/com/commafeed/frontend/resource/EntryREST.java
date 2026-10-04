package com.commafeed.frontend.resource;

import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryService;
import com.commafeed.backend.service.FeedEntryTagService;
import com.commafeed.backend.service.LlmRewriteService;
import com.commafeed.frontend.rest.resources.model.GenerateAlternativeRequest;
import com.commafeed.frontend.rest.resources.model.GenerateAlternativeResponse;
import com.commafeed.frontend.model.request.MarkRequest;
import com.commafeed.frontend.model.request.MultipleMarkRequest;
import com.commafeed.frontend.model.request.StarRequest;
import com.commafeed.frontend.model.request.TagRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import com.google.common.base.Preconditions;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/rest/entry")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feed entries")
public class EntryREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryTagDAO feedEntryTagDAO;
    private final FeedEntryService feedEntryService;
    private final FeedEntryTagService feedEntryTagService;
    private final LlmRewriteService llmRewriteService;

    @Path("/{id}/generate-alternative")
    @POST
    @Transactional
    @Operation(summary = "Generate an alternative for an entry title or content")
    public Response generateAlternative(
            @Parameter(description = "Feed entry id", required = true) @PathParam("id") Long id,
            @Valid
                    @Parameter(description = "Generate alternative request", required = true)
                    GenerateAlternativeRequest request) {
        if (request == null
                || request.getTarget() == null
                || (!"title".equals(request.getTarget()) && !"content".equals(request.getTarget()))
                || request.getPrompt() == null
                || request.getPrompt().isBlank()) {
            throw new BadRequestException("target and prompt are required");
        }

        User user = authenticationContext.getCurrentUser();
        try {
            LlmRewriteService.RewriteResult result =
                    llmRewriteService.rewrite(
                            user, id, request.getTarget(), request.getPrompt());
            return Response.ok(
                            new GenerateAlternativeResponse(
                                    result.originalEntry(),
                                    request.getTarget(),
                                    request.getPrompt(),
                                    result.generatedAlternative()))
                    .build();
        } catch (LlmRewriteService.EntryNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        } catch (LlmRewriteService.LlmUnavailableException e) {
            return Response.status(Status.SERVICE_UNAVAILABLE).build();
        }
    }

    @Path("/mark")
    @POST
    @Transactional
    @Operation(summary = "Mark a feed entry", description = "Mark a feed entry as read/unread")
    public Response markEntry(
            @Valid @Parameter(description = "Mark Request", required = true) MarkRequest req) {
        Preconditions.checkNotNull(req);
        Preconditions.checkNotNull(req.getId());

        User user = authenticationContext.getCurrentUser();
        feedEntryService.markEntry(user, Long.valueOf(req.getId()), req.isRead());
        return Response.ok().build();
    }

    @Path("/markMultiple")
    @POST
    @Transactional
    @Operation(
            summary = "Mark multiple feed entries",
            description = "Mark feed entries as read/unread")
    public Response markEntries(
            @Valid @Parameter(description = "Multiple Mark Request", required = true)
                    MultipleMarkRequest req) {
        Preconditions.checkNotNull(req);
        Preconditions.checkNotNull(req.getRequests());

        User user = authenticationContext.getCurrentUser();
        for (MarkRequest r : req.getRequests()) {
            Preconditions.checkNotNull(r.getId());
            feedEntryService.markEntry(user, Long.valueOf(r.getId()), r.isRead());
        }

        return Response.ok().build();
    }

    @Path("/star")
    @POST
    @Transactional
    @Operation(summary = "Star a feed entry", description = "Mark a feed entry as read/unread")
    public Response starEntry(
            @Valid @Parameter(description = "Star Request", required = true) StarRequest req) {
        Preconditions.checkNotNull(req);
        Preconditions.checkNotNull(req.getId());

        User user = authenticationContext.getCurrentUser();
        feedEntryService.starEntry(user, Long.valueOf(req.getId()), req.isStarred());

        return Response.ok().build();
    }

    @Path("/tags")
    @GET
    @Transactional
    @Operation(
            summary = "Get list of tags for the user",
            description = "Get list of tags for the user")
    public Response getTags() {
        User user = authenticationContext.getCurrentUser();
        List<String> tags = feedEntryTagDAO.findByUser(user);
        return Response.ok(tags).build();
    }

    @Path("/tag")
    @POST
    @Transactional
    @Operation(summary = "Set feed entry tags")
    public Response tagEntry(
            @Valid @Parameter(description = "Tag Request", required = true) TagRequest req) {
        Preconditions.checkNotNull(req);
        Preconditions.checkNotNull(req.getEntryId());

        User user = authenticationContext.getCurrentUser();
        feedEntryTagService.updateTags(user, req.getEntryId(), req.getTags());

        return Response.ok().build();
    }
}
