package fi.nls.oskari.rating;

/**
 * Created by MHURME on 17.9.2015.
 *  (teksti, arvosana, käyttäjäviittaus, käyttäjän kertoma rooli, aineistoviittaus mihin arvostelu kohdistuu)
 */
public class Rating {
    private long id;
    private long userId;
    private int rating;
    private String category;
    private String categoryItem;
    private String comment;
    private String userRole;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryItem() {
        return categoryItem;
    }

    public void setCategoryItem(String categoryItem) {
        this.categoryItem = categoryItem;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
