package it.thomasjohansen.saml;

import org.junit.Test;

/**
 * @author thomas@thomasjohansen.it
 */
public class AuthenticationRequestTest {

    @Test
    public void whenConvertingToXmlAllDataIsPreserved() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .assertionConsumerAddress("AssertionConsumerAddress")
                .forceAuthentication(true)
                .issuer("Issuer")
                .passive(true)
                .persistentId("PersistentId")
                .protocolBinding(AuthenticationRequest.ProtocolBinding.HTTP_Post)
                .transparentId("TransparentId")
                .build();
        String xml = request.toXML();

    }


}
