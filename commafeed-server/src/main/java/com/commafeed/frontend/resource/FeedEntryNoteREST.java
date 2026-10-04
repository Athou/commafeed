package com.commafeed.frontend.resource;

import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.frontend.model.FeedEntryNoteModel;
import com.commafeed.frontend.model.request.FeedEntryNoteRequest;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/rest/entry/note")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
@Tag(name = "Feed entry notes")
public class FeedEntryNoteREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryNoteService feedEntryNoteService;

    @POST
    @Transactional
    @Operation(summary = "Save or update a feed entry note")
    public FeedEntryNoteModel saveNote(
            @Valid @Parameter(description = "Feed entry note request", required = true)
                    FeedEntryNoteRequest request) {
        User user = authenticationContext.getCurrentUser();
        FeedEntryNote note =
                feedEntryNoteService.saveOrUpdate(
                        user, request.getEntryId(), request.getComment(), request.getRating());
        if (note == null) {
            throw new NotFoundException("Feed entry not found");
        }
        return FeedEntryNoteModel.from(note);
    }

    @Path("/list")
    @GET
    @Transactional
    @Operation(summary = "Get feed entry notes for the current user")
    public List<FeedEntryNoteModel> getNotes() {
        User user = authenticationContext.getCurrentUser();
        return feedEntryNoteService.findByUser(user).stream()
                .map(FeedEntryNoteModel::from)
                .toList();
    }
}
