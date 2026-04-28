package org.oskari.announcements.jobs;

import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.oskari.announcements.actions.AnnouncementsServiceMybatisImpl;
import org.oskari.announcements.model.Announcement;

import java.time.OffsetDateTime;
import java.util.Collections;

public class AnnouncementsRssImportJobTest {

    private static final String EXTERNAL_ID = "same-guid";
    private static final OffsetDateTime ORIGINAL_BEGIN_DATE = OffsetDateTime.parse("2026-04-01T10:00:00Z");
    private static final OffsetDateTime IMPORTED_BEGIN_DATE = OffsetDateTime.parse("2026-04-03T12:00:00Z");
    private static final OffsetDateTime ORIGINAL_END_DATE = OffsetDateTime.parse("2026-04-30T23:59:59Z");

    private Announcement existing;
    private Announcement imported;
    private AnnouncementsServiceMybatisImpl service;
    private AnnouncementsRssImportJob job;
    private MockedConstruction<AnnouncementsServiceMybatisImpl> mockedServiceConstruction;

    @BeforeEach
    public void setUp() {
        mockedServiceConstruction = Mockito.mockConstruction(AnnouncementsServiceMybatisImpl.class);

        existing = new Announcement();
        existing.setId(42);
        existing.setBeginDate(ORIGINAL_BEGIN_DATE);
        existing.setEndDate(ORIGINAL_END_DATE);
        existing.setLocale(locale("old title", "old link"));
        existing.setOptions(options(true, EXTERNAL_ID, "keep this"));

        imported = new Announcement();
        imported.setBeginDate(IMPORTED_BEGIN_DATE);
        imported.setEndDate(IMPORTED_BEGIN_DATE.plusDays(30));
        imported.setLocale(locale("new title", "new link"));
        imported.setOptions(options(false, EXTERNAL_ID, null));

        job = new AnnouncementsRssImportJob();
        service = mockedServiceConstruction.constructed().get(0);
        Mockito.when(service.getAnnouncementByExternalId(EXTERNAL_ID)).thenReturn(existing);
    }

    @AfterEach
    public void tearDown() {
        mockedServiceConstruction.close();
    }

    @Test
    public void duplicateExternalIdUsesUpdateInsteadOfSave() {
        final int changed = job.saveAnnouncements(Collections.singletonList(imported));

        Assertions.assertEquals(1, changed);
        Mockito.verify(service, Mockito.never()).saveAnnouncement(Mockito.any(Announcement.class));
        Mockito.verify(service).updateAnnouncement(Mockito.any(Announcement.class));
    }

    @Test
    public void duplicateExternalIdUpdatesBeginDateAndKeepsIdAndEndDate() {
        job.saveAnnouncements(Collections.singletonList(imported));

        final Announcement updated = captureUpdatedAnnouncement();
        Assertions.assertEquals(existing.getId(), updated.getId());
        Assertions.assertEquals(IMPORTED_BEGIN_DATE, updated.getBeginDate());
        Assertions.assertEquals(ORIGINAL_END_DATE, updated.getEndDate());
    }

    @Test
    public void duplicateExternalIdUpdatesOnlyLocaleTitleAndLink() {
        job.saveAnnouncements(Collections.singletonList(imported));

        final JSONObject fi = captureUpdatedAnnouncement().getLocale().getJSONObject("fi");
        Assertions.assertEquals("new title", fi.getString("title"));
        Assertions.assertEquals("new link", fi.getString("link"));
    }

    @Test
    public void duplicateExternalIdKeepsExistingOptionsUnchanged() {
        job.saveAnnouncements(Collections.singletonList(imported));

        final JSONObject options = captureUpdatedAnnouncement().getOptions();
        Assertions.assertEquals("keep this", options.getString("other"));
        Assertions.assertTrue(options.getBoolean("showAsPopup"));
        Assertions.assertEquals(EXTERNAL_ID, options.getString("externalId"));
    }

    private Announcement captureUpdatedAnnouncement() {
        final ArgumentCaptor<Announcement> captor = ArgumentCaptor.forClass(Announcement.class);
        Mockito.verify(service).updateAnnouncement(captor.capture());
        return captor.getValue();
    }

    private JSONObject locale(final String title, final String link) {
        return new JSONObject().put("fi", new JSONObject().put("title", title).put("link", link));
    }

    private JSONObject options(final boolean showAsPopup, final String externalId, final String other) {
        final JSONObject options = new JSONObject();
        options.put("showAsPopup", showAsPopup);
        options.put("externalId", externalId);
        if (other != null) {
            options.put("other", other);
        }
        return options;
    }

}
