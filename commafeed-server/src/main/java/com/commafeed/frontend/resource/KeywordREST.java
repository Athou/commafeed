package com.commafeed.frontend.resource;

import com.commafeed.backend.dao.FeedEntryKeywordDAO;
import com.commafeed.backend.model.FeedEntryKeyword;
import com.commafeed.backend.model.User;
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

@Path("/rest/entry/keyword")
@RolesAllowed(Roles.USER)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Singleton
public class KeywordREST {

    private final AuthenticationContext authenticationContext;
    private final FeedEntryKeywordDAO keywordDAO;

    @POST
    @Transactional
    public Response addKeyword(KeywordRequest req) {
        if (req.keyword() == null || req.keyword().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("keyword is required")
                    .build();
        }

        User user = authenticationContext.getCurrentUser();
        FeedEntryKeyword entity = new FeedEntryKeyword();
        entity.setUser(user);
        entity.setKeyword(req.keyword().trim());
        entity.setTelegramChatId(req.telegramChatId());

        keywordDAO.saveOrUpdate(entity);
        return Response.ok(
                        new KeywordResponse(
                                entity.getId(), entity.getKeyword(), entity.getTelegramChatId()))
                .build();
    }

    @GET
    @Transactional
    public Response getKeywords() {
        User user = authenticationContext.getCurrentUser();
        List<FeedEntryKeyword> keywords = keywordDAO.findByUser(user);
        List<KeywordResponse> dtos =
                keywords.stream()
                        .map(
                                k ->
                                        new KeywordResponse(
                                                k.getId(), k.getKeyword(), k.getTelegramChatId()))
                        .toList();
        return Response.ok(dtos).build();
    }

    public record KeywordRequest(String keyword, String telegramChatId) {}

    public record KeywordResponse(Long id, String keyword, String telegramChatId) {}
}
