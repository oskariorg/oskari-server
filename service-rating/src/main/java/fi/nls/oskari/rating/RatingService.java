package fi.nls.oskari.rating;

import fi.nls.oskari.service.OskariComponent;
import java.util.List;

/**
 * Created by MHURME on 11.9.2015.
 */
public abstract class RatingService extends OskariComponent {
    public abstract Rating saveRating(Rating rating);
    public abstract List<Rating> getAllRatingsFor(String category, String categoryItem);
    public abstract String[] getAverageRatingFor(String category, String categoryItem);
    public abstract String findLatestAdminRating(String category, String categoryItem, String adminRole);
    public abstract boolean validateRequiredStrings(Rating rating);
}
