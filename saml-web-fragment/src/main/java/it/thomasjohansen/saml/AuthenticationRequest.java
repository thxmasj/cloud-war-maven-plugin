package it.thomasjohansen.saml;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.UUID;

/**
 * @author thomas@thomasjohansen.it
 */
public class AuthenticationRequest {

    private AuthnRequest request;
    enum ProtocolBinding {HTTP_Post, Redirect}

    private AuthenticationRequest() {
        // Used by builder
    }

    public String toXML() {
        try {
            DocumentBuilder documentBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            Marshaller marshaller =
                    XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(request);
            Element element = marshaller.marshall(request);
            return document.toString();
        } catch (ParserConfigurationException|MarshallingException e) {
            throw new RuntimeException("Failed to create XML", e);
        }
    }

    public Builder builder() {
        return new Builder(new AuthenticationRequest());
    }

    public static class Builder {

        private AuthenticationRequest instance;

        public Builder(AuthenticationRequest instance) {
            this.instance = instance;
            this.instance.request = OpenSamlBuilders.request();
            AuthnContextClassRef authnCtxClassRef =
                    OpenSamlBuilders.requestedAuthnCtxClassRefBuilder.buildObject();
            authnCtxClassRef.setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

            RequestedAuthnContext authnCtx =
                    OpenSamlBuilders.requestedAuthnCtxBuilder.buildObject();

            authnCtx.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
            authnCtx.getAuthnContextClassRefs().add(authnCtxClassRef);
            this.instance.request.setRequestedAuthnContext(authnCtx);
            this.instance.request.setID(UUID.randomUUID().toString());
            this.instance.request.setIssueInstant(DateTime.now());
            this.instance.request.setVersion(SAMLVersion.VERSION_20);
        }

        public Builder issuer(String issuer) {
            instance.request.setIssuer(OpenSamlBuilders.issuer(issuer));
            return this;
        }

        public Builder transparentId(String serviceProviderNameQualifier) {
            instance.request.setNameIDPolicy(OpenSamlBuilders.transparentNameIdPolicy(serviceProviderNameQualifier));
            return this;
        }

        public Builder persistentId(String serviceProviderNameQualifier) {
            instance.request.setNameIDPolicy(OpenSamlBuilders.persistentNameIdPolicy(serviceProviderNameQualifier));
            return this;
        }

        public Builder assertionConsumerAddress(String assertionConsumerAddress) {
            instance.request.setAssertionConsumerServiceURL(assertionConsumerAddress);
            return this;
        }

        public Builder forceAuthentication(boolean forceAuthentication) {
            instance.request.setForceAuthn(forceAuthentication);
            return this;
        }

        public Builder passive(boolean passive) {
            instance.request.setIsPassive(passive);
            return this;
        }

        public Builder protocolBinding(ProtocolBinding protocolBinding) {
            switch (protocolBinding) {
                case HTTP_Post:
                    instance.request.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
                    break;
                case Redirect:
                    instance.request.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:Redirect");
                    break;
            }
            return this;
        }

        public AuthenticationRequest build() {
            return instance;
        }

    }

}
