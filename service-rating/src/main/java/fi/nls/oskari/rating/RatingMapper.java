package fi.nls.oskari.rating;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by MHURME on 14.9.2015.
 */
public interface RatingMapper {
    Rating find(@Param("id") long id);
    List<Rating> findAllFor(@Param("category") String category, @Param("categoryItem") String categoryItem);
    long insertRating(Rating rating);
    void updateRating(Rating rating);
}
