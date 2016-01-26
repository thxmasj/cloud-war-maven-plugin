package it.thomasjohansen.saml;

import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

/**
 * @author thomas@thomasjohansen.it
 */
public class OpenSamlBuilders {

    private static volatile XMLObjectBuilderFactory builderFactory =
            XMLObjectProviderRegistrySupport.getBuilderFactory();
    @SuppressWarnings("unchecked")
    public static volatile SAMLObjectBuilder<AuthnRequest> requestBuilder =
            (SAMLObjectBuilder<AuthnRequest>)
                    builderFactory.getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME);
    @SuppressWarnings("unchecked")
    public static volatile SAMLObjectBuilder<Issuer> issuerBuilder =
            (SAMLObjectBuilder<Issuer>)
            builderFactory.getBuilder(Issuer.DEFAULT_ELEMENT_NAME);
    @SuppressWarnings("unchecked")
    public static volatile SAMLObjectBuilder<NameIDPolicy> nameIDPolicyBuilder =
            (SAMLObjectBuilder<NameIDPolicy>)
            builderFactory.getBuilder(NameIDPolicy.DEFAULT_ELEMENT_NAME);
    @SuppressWarnings("unchecked")
    public static volatile SAMLObjectBuilder<AuthnContextClassRef> requestedAuthnCtxClassRefBuilder =
            (SAMLObjectBuilder<AuthnContextClassRef>)
            builderFactory.getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
    @SuppressWarnings("unchecked")
    public static volatile SAMLObjectBuilder<RequestedAuthnContext> requestedAuthnCtxBuilder =
            (SAMLObjectBuilder<RequestedAuthnContext>)
            builderFactory.getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);

    public static AuthnRequest request() {
        return requestBuilder.buildObject();
    }

    public static Issuer issuer(String issuer) {
        Issuer element = issuerBuilder.buildObject();
        element.setValue(issuer);
        return element;
    }

    public static NameIDPolicy transparentNameIdPolicy(String serviceProviderNameQualifier) {
        NameIDPolicy element = nameIDPolicyBuilder.buildObject();
        element.setAllowCreate(true);
        element.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:transparent");
        element.setSPNameQualifier(serviceProviderNameQualifier);
        return element;
    }

    public static NameIDPolicy persistentNameIdPolicy(String serviceProviderNameQualifier) {
        NameIDPolicy element = nameIDPolicyBuilder.buildObject();
        element.setAllowCreate(true);
        element.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        element.setSPNameQualifier(serviceProviderNameQualifier);
        return element;
    }

}
