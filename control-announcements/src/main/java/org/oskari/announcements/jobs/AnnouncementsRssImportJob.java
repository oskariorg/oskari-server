package org.oskari.announcements.jobs;

import fi.nls.oskari.annotation.Oskari;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.IOHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.worker.ScheduledJob;
import org.json.JSONObject;
import org.oskari.announcements.actions.AnnouncementsService;
import org.oskari.announcements.actions.AnnouncementsServiceMybatisImpl;
import org.oskari.announcements.model.Announcement;
import org.oskari.xml.XmlHelper;
import org.w3c.dom.Element;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Scheduled job for importing announcements from an RSS feed.
 */
@Oskari("AnnouncementsRssImport")
public class AnnouncementsRssImportJob extends ScheduledJob {

    private static final Logger LOG = LogFactory.getLogger(AnnouncementsRssImportJob.class);

    private static final String PROPERTY_FEED_URL = "oskari.scheduler.job.AnnouncementsRssImport.url";
    private static final String DEFAULT_LOCALE = PropertyUtil.getDefaultLanguage();
    private static final int DEFAULT_ANNOUNCEMENT_DURATION_DAYS = 30;
    private static final String RSS_ROOT_ELEMENT = "rss";
    private static final String OPTIONS_EXTERNAL_ID = "externalId";
    // RFC 2822 date format used by RSS
    private static final DateTimeFormatter RSS_DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    private final AnnouncementsService announcementsService = new AnnouncementsServiceMybatisImpl();

    @Override
    public void execute(Map<String, Object> params) {
        final String feedUrl = getFeedUrl();
        if (feedUrl == null || feedUrl.isEmpty()) {
            LOG.info("RSS import skipped: property", PROPERTY_FEED_URL, "is not configured");
            return;
        }

        try {
            final String feedXml = readFeed(feedUrl);
            final List<Announcement> announcements = convertFeedToAnnouncements(feedXml);
            final int saved = saveAnnouncements(announcements);
            LOG.info("RSS import done. Parsed", announcements.size(), "items and saved", saved, "announcements");
        } catch (Exception e) {
            LOG.error(e, "RSS import failed for", feedUrl);
        }
    }

    @Override
    public String getCronLine() {
        final String line = super.getCronLine();
        if (line != null) {
            return line;
        }
        // Run every 5 minutes by default if cron isn't configured explicitly.
        return "0 0/5 * * * ?";
    }

    protected String getFeedUrl() {
        return PropertyUtil.getOptional(PROPERTY_FEED_URL);
    }

    protected String readFeed(final String feedUrl) throws IOException {
        return IOHelper.getURL(feedUrl);
    }

    protected List<Announcement> convertFeedToAnnouncements(final String feedXml) {
        final List<Announcement> announcements = new ArrayList<>();
        final Element root = XmlHelper.parseXML(feedXml, true);
        if (root == null) {
            throw new IllegalArgumentException("Invalid feed format: missing root element (expected '" + RSS_ROOT_ELEMENT + "')");
        }

        final String rootElementName = XmlHelper.getLocalName(root);
        if (!RSS_ROOT_ELEMENT.equalsIgnoreCase(rootElementName)) {
            throw new IllegalArgumentException("Unsupported feed format. Expected root element '" + RSS_ROOT_ELEMENT
                    + "' but got '" + rootElementName + "'");
        }

        Element container = root;
        final Element channel = XmlHelper.getFirstChild(root, "channel");
        if (channel != null) {
            container = channel;
        }

        final String feedLanguage = safeValue(XmlHelper.getChildValue(container, "language"));
        final String localeKey = feedLanguage == null ? DEFAULT_LOCALE : feedLanguage;

        XmlHelper.getChildElements(container, "item")
                .map(item -> mapFeedItemToAnnouncement(item, localeKey))
                .forEach(announcements::add);

        return announcements;
    }

    protected Announcement mapFeedItemToAnnouncement(final Element itemElement, final String localeKey) {
        final String title = safeValue(XmlHelper.getChildValue(itemElement, "title"));
        final String link = safeValue(XmlHelper.getChildValue(itemElement, "link"));
        final String guid = safeValue(XmlHelper.getChildValue(itemElement, "guid"));
        final String pubDate = safeValue(XmlHelper.getChildValue(itemElement, "pubDate"));

        final Announcement announcement = new Announcement();

        // Parse pubDate (RFC 2822 format) or use current time if parsing fails
        OffsetDateTime beginDate;
        try {
            if (pubDate != null && !pubDate.isEmpty()) {
                beginDate = OffsetDateTime.parse(pubDate, RSS_DATE_FORMATTER);
            } else {
                beginDate = OffsetDateTime.now(ZoneOffset.UTC);
            }
        } catch (Exception e) {
            LOG.warn("Failed to parse pubDate:", pubDate, "using current time instead", e);
            beginDate = OffsetDateTime.now(ZoneOffset.UTC);
        }
        announcement.setBeginDate(beginDate);
        announcement.setEndDate(beginDate.plusDays(DEFAULT_ANNOUNCEMENT_DURATION_DAYS));

        final JSONObject localized = new JSONObject();
        localized.put("title", title == null ? "" : title);
        localized.put("link", link == null ? "" : link);
        final JSONObject locale = new JSONObject();
        locale.put(localeKey, localized);
        announcement.setLocale(locale);

        final JSONObject options = new JSONObject();
        options.put("showAsPopup", false);
        if (guid != null) {
            options.put(OPTIONS_EXTERNAL_ID, guid);
        }
        announcement.setOptions(options);

        return announcement;
    }

    protected int saveAnnouncements(final List<Announcement> announcements) {
        int savedCount = 0;
        for (Announcement announcement : announcements) {
            try {
                final String externalId = safeValue(announcement.getOptions().optString(OPTIONS_EXTERNAL_ID, null));
                if (externalId != null) {
                    Announcement existingAnnouncement = announcementsService.getAnnouncementByExternalId(externalId);
                    if (existingAnnouncement != null) {
                        updateExistingAnnouncement(existingAnnouncement, announcement);
                        announcementsService.updateAnnouncement(existingAnnouncement);
                        savedCount++;
                        continue;
                    }
                }
                announcementsService.saveAnnouncement(announcement);
                savedCount++;
            } catch (Exception e) {
                LOG.warn(e, "Failed to save announcement for RSS item:", announcement.getLocale());
            }
        }
        return savedCount;
    }

    protected void updateExistingAnnouncement(final Announcement existingAnnouncement,
            final Announcement importedAnnouncement) {
        existingAnnouncement.setBeginDate(importedAnnouncement.getBeginDate());
        existingAnnouncement.setLocale(mergeLocale(existingAnnouncement.getLocale(), importedAnnouncement.getLocale()));
    }

    protected JSONObject mergeLocale(final JSONObject existingLocale, final JSONObject importedLocale) {
        final JSONObject mergedLocale = existingLocale == null
                ? new JSONObject()
                : new JSONObject(existingLocale.toString());
        if (importedLocale == null) {
            return mergedLocale;
        }
        for (String language : importedLocale.keySet()) {
            final JSONObject importedLocalized = importedLocale.optJSONObject(language);
            if (importedLocalized == null) {
                continue;
            }
            final JSONObject mergedLocalized = mergedLocale.optJSONObject(language) == null
                    ? new JSONObject()
                    : new JSONObject(mergedLocale.getJSONObject(language).toString());
            mergedLocalized.put("title", importedLocalized.optString("title", ""));
            mergedLocalized.put("link", importedLocalized.optString("link", ""));
            mergedLocale.put(language, mergedLocalized);
        }
        return mergedLocale;
    }

    private String safeValue(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
