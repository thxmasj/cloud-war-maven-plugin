package it.thomasjohansen.saml;

import org.apache.cxf.jaxrs.impl.HttpHeadersImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.rs.security.saml.sso.SSOConstants;
import org.apache.cxf.rs.security.saml.sso.state.ResponseState;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.wss4j.common.saml.SamlAssertionWrapper;
import org.opensaml.saml.saml2.core.AuthnRequest;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

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
        AuthnRequest authnRequest = createAuthnRequest();
        // QueryParam SAMLRequest = base64Encode(toString(authnRequest));
        // QueryParam RelayState =
        redirectToIdentityProvider(response);
    }

    private boolean inSecurityContext(ServletRequest request) {
        return false;
    }

    private AuthnRequest createAuthnRequest() {
        return null;
    }

    private void redirectToIdentityProvider(ServletResponse servletResponse) throws IOException {
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        response.sendRedirect(identityProviderAddress);
    }

    @Override
    public void destroy() {

    }

}
