package fi.nls.oskari.map.publish.service;

import fi.nls.oskari.map.publish.domain.TermsOfUse;
import fi.nls.oskari.service.db.BaseIbatisService;

import java.sql.Timestamp;

public class PublishTermsOfUseServiceIbatisImpl extends BaseIbatisService<TermsOfUse> implements PublishTermsOfUseService {
    @Override
    protected String getNameSpace() {
        return "PublishedMap";
    }
    
    @Override
    public int insert(final TermsOfUse termsOfUse) {
        final TermsOfUse accepted = findByUserId(termsOfUse.getUserid());
        if (accepted == null) {
            return insert(getNameSpace() + ".insertTermsOfUse", termsOfUse);
        }
        
        return -1;
    }
    

    public int setUserAgreed(final long userId) {

        final TermsOfUse accepted = findByUserId(userId);
        if (accepted == null) {
            final TermsOfUse tou = new TermsOfUse();
            tou.setAgreed(true);
            tou.setUserid(userId);
            tou.setTime(new Timestamp(System.currentTimeMillis()));
            return insert(getNameSpace() + ".insertTermsOfUse", tou);
        }
        
        return -1;
    }

    public TermsOfUse findByUserId(final long userId) {
        final TermsOfUse toe = queryForObject(getNameSpace() + ".findAgreedTermsOfUse", userId);
        return toe;
    }
}
