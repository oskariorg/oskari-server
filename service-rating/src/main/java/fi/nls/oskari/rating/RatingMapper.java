package fi.nls.oskari.rating;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by MHURME on 14.9.2015.
 */
public interface RatingMapper {
    Rating find(@Param("id") long id);
    String findLatestAdminRating(@Param("category") String category, @Param("categoryItem") String categoryItem, @Param("adminRole") String adminRole);
    List<Rating> findAllFor(@Param("category") String category, @Param("categoryItem") String categoryItem);
    void insertRating(Rating rating);
    void updateRating(Rating rating);
}
