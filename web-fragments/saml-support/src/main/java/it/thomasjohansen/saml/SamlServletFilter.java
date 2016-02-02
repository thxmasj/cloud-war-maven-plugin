package it.thomasjohansen.saml;

import org.opensaml.saml.saml2.core.AuthnRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author thomas@thomasjohansen.it
 */
public class SamlServletFilter implements Filter {

    private String identityProviderAddress;
    private String assertionConsumerAddress;
    private String issuerId;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (inSecurityContext(request)) {
            chain.doFilter(request, response);
            return;
        }
        String samlRequest = AuthenticationRequest.builder()
                .assertionConsumerAddress(assertionConsumerAddress)
                .issuer(issuerId)
                .build().toXML();
        // QueryParam SAMLRequest = base64Encode(toString(authnRequest));
        // QueryParam RelayState =
        redirectToIdentityProvider(response, samlRequest);
    }

    private boolean inSecurityContext(ServletRequest request) {
        return false;
    }

    private AuthnRequest createAuthnRequest() {
        return null;
    }

    private void redirectToIdentityProvider(ServletResponse servletResponse, String samlRequest) throws IOException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.sendRedirect(identityProviderAddress + "?SAMLRequest=" + samlRequest);
    }

    @Override
    public void destroy() {

    }

}
