package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryTagDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryTag;
import com.commafeed.backend.model.User;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeedEntryTagServiceTest {

    private FeedEntryTagService underTest;

    @Mock
    private FeedEntryDAO feedEntryDAO;

    @Mock
    private FeedEntryTagDAO feedEntryTagDAO;

    @Captor
    private ArgumentCaptor<List<FeedEntryTag>> feedEntryListArgumentCaptor;

    @Before
    public void init() {
        underTest = new FeedEntryTagService(feedEntryDAO, feedEntryTagDAO);
    }

    @Test
    public void updateTags() {
        // Arrange
        final User user = new User();
        final FeedEntry entry = new FeedEntry();
        final List<String> tagNames = Lists.newArrayList("tag1", "tag2");
        final FeedEntryTag tag1 = new FeedEntryTag(user, entry, "tag1");
        final FeedEntryTag tag3 = new FeedEntryTag(user, entry, "tag3");
        final ArrayList<FeedEntryTag> existingTags = Lists.newArrayList(tag1, tag3);

        when(feedEntryDAO.findById(1L)).thenReturn(entry);
        when(feedEntryTagDAO.findByEntry(user, entry)).thenReturn(existingTags);

        // Act
        underTest.updateTags(user, 1L, tagNames);

        // Assert
        verify(feedEntryTagDAO).saveOrUpdate(feedEntryListArgumentCaptor.capture());
        assertEquals(1, feedEntryListArgumentCaptor.getValue().size());
        assertEquals("tag2", feedEntryListArgumentCaptor.getValue().get(0).getName());
        verify(feedEntryTagDAO).delete(feedEntryListArgumentCaptor.capture());
        assertEquals(1, feedEntryListArgumentCaptor.getValue().size());
        assertEquals("tag3", feedEntryListArgumentCaptor.getValue().get(0).getName());
    }

    @Test
    public void updateTags_entryDoesNotExist() {
        // Arrange
        final User user = new User();
        final List<String> tagNames = Lists.newArrayList("tag1", "tag2");
        when(feedEntryDAO.findById(1L)).thenReturn(null);

        // Act
        underTest.updateTags(user, 1L, tagNames);

        // Assert
        verifyZeroInteractions(feedEntryTagDAO);
    }

    @Test
    public void getTagNames() {
        // Arrange
        final FeedEntryTag tag1 = new FeedEntryTag(new User(), new FeedEntry(), "tag1");
        final FeedEntryTag tag2 = new FeedEntryTag(new User(), new FeedEntry(), "tag2");
        final ArrayList<FeedEntryTag> tags = Lists.newArrayList(tag1, tag2);

        // Act
        final Set<String> result = underTest.getTagNames(tags);

        // Assert
        assertEquals("tag1", result.toArray()[0]);
    }

    @Test
    public void getRemoveList() {
        // Arrange
        final FeedEntryTag tag1 = new FeedEntryTag(new User(), new FeedEntry(), "tag1");
        final FeedEntryTag tag2 = new FeedEntryTag(new User(), new FeedEntry(), "tag2");
        final FeedEntryTag tag3 = new FeedEntryTag(new User(), new FeedEntry(), "tag3");
        final FeedEntryTag tag4 = new FeedEntryTag(new User(), new FeedEntry(), "tag4");
        final ArrayList<FeedEntryTag> existingTags = Lists.newArrayList(tag1, tag2, tag3, tag4);
        final ArrayList<String> tagNames = Lists.newArrayList("tag1", "tag3");

        // Act
        final List<FeedEntryTag> result = underTest.getRemoveList(existingTags, tagNames);

        // Assert
        assertEquals(2, result.size());
        assertEquals("tag2", result.get(0).getName());
        assertEquals("tag4", result.get(1).getName());
    }

    @Test
    public void getAddList() {
        // Arrange
        final User user = new User();
        final FeedEntry entry = new FeedEntry();
        final ArrayList<String> tagNames = Lists.newArrayList("tag1", "tag2", "tag3");
        final HashSet<String> existingTagNames = Sets.newHashSet("tag2", "tag3");

        // Act
        final List<FeedEntryTag> result = underTest.getAddList(user, entry, tagNames, existingTagNames);

        // Assert
        assertEquals(1, result.size());
        assertEquals(user, result.get(0).getUser());
        assertEquals(entry, result.get(0).getEntry());
        assertEquals("tag1", result.get(0).getName());
    }
}