package com.commafeed.frontend.resource;

import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;
import com.commafeed.backend.service.FeedEntryNoteService;
import com.commafeed.security.AuthenticationContext;
import com.commafeed.security.Roles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Path("/rest/entry/note")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
public class FeedEntryNoteREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryNoteService noteService;

    @POST
    @Transactional
    public Response createOrUpdateNote(NoteRequest req) {
        if (req.entryId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("entryId is required")
                    .build();
        }

        try {
            User user = authenticationContext.getCurrentUser();
            FeedEntryNote savedNote =
                    noteService.saveOrUpdateNote(user, req.entryId(), req.note(), req.rating());
            return Response.ok(toDTO(savedNote)).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Transactional
    public Response getNotes() {
        User user = authenticationContext.getCurrentUser();
        List<FeedEntryNote> notes = noteService.getUserNotes(user);
        List<NoteResponse> dtos = notes.stream().map(this::toDTO).toList();
        return Response.ok(dtos).build();
    }

    private NoteResponse toDTO(FeedEntryNote note) {
        return new NoteResponse(
                note.getId(),
                note.getEntry().getId(),
                note.getNote(),
                note.getRating(),
                note.getCreatedAt() != null ? note.getCreatedAt().toString() : null);
    }

    public record NoteRequest(Long entryId, String note, Integer rating) {}

    public record NoteResponse(
            Long id, Long entryId, String note, Integer rating, String createdAt) {}
}
