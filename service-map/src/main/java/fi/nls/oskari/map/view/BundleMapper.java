package fi.nls.oskari.map.view;

import fi.nls.oskari.domain.map.view.Bundle;
import org.apache.ibatis.annotations.*;

public interface BundleMapper {

    @Select("select id, name, startup, config, state from oskari_bundle where name = #{name}")
    @Results({
            @Result(property = "bundleId", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "startup", column = "startup"),
            @Result(property = "state", column = "state"),
            @Result(property = "config", column = "config")
    })
    Bundle getBundleTemplateByName(final String name);

    @Insert("insert into oskari_bundle (name, startup, config, state ) values ( #{name}, #{startup}, #{config}, #{state} )")
    @Options(useGeneratedKeys=true)
    long addBundleTemplate(final Bundle bundle);

}
