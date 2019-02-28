package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.map.publish.domain.TermsOfUse;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface PublishTermsOfUseMapper {
    @Insert("insert into oskari_terms_of_use_for_publishing userid, agreed, time) values (#{userid}, #{agreed}, #{time})")
    void insertTermsOfUse(TermsOfUse tou);

    @Select("select user, agreed, time from oskari_terms_of_use_for_publishing WHERE userid = #{userId}")
    @Results({
            @Result(property = "user", column = "user"),
            @Result(property = "agreed", column = "agreed"),
            @Result(property = "time", column = "time")
    })
    TermsOfUse findByUserId(final long userId);
}
