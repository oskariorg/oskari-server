package fi.nls.oskari.spring.security.saml;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.NameID;
import org.springframework.security.saml.SAMLCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by SMAKINEN on 3.2.2015.
 */
public class SAMLCredentialHelper {

    public static SAMLCredential createCredential(final String nameId, Map<String, String> attributes) {
        return createCredential(nameId, attributes, "persistent");
    }
    public static SAMLCredential createCredential(final String nameId, Map<String, String> attributes, final String nameIdFormat) {
        SAMLCredential credential = mock(SAMLCredential.class);
        //public SAMLCredential(NameID nameID, Assertion authenticationAssertion, String remoteEntityID, List< Attribute > attributes, String localEntityID) {
        NameID id = mock(NameID.class);
        doReturn(id).when(credential).getNameID();
        doReturn(nameId).when(id).getValue();
        doReturn(nameIdFormat).when(id).getFormat();
        List<Attribute> attributeList = new ArrayList<>();
        for(final Map.Entry<String, String> entry : attributes.entrySet()) {
            Attribute attr = mock(Attribute.class);
            doReturn(entry.getKey()).when(attr).getName();
            attributeList.add(attr);

            doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    return entry.getValue();
                }
            }).when(credential).getAttributeAsString(entry.getKey());
        }
        return credential;
    }
}
