/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PlainTextPasswordEncoder;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare that password to a translated version of the
 * password provided by the user. If they match, then authentication succeeds.
 * Default password translator is plaintext translator.
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class QueryDatabaseAuthenticationHandler extends
    JdbcDaoSupport implements AuthenticationHandler {

    protected final Log log = LogFactory.getLog(getClass());

    private PasswordEncoder passwordTranslator;

    private String sql;

    public boolean authenticate(final Credentials request) {
        final UsernamePasswordCredentials uRequest = (UsernamePasswordCredentials) request;
        final String username = uRequest.getUserName();
        final String password = uRequest.getPassword();
        final String encryptedPassword = this.passwordTranslator
            .encode(password);
        final String dbPassword = (String) getJdbcTemplate().queryForObject(
            this.sql, new Object[] {username}, String.class);
        return dbPassword.equals(encryptedPassword);
    }

    public boolean supports(Credentials credentials) {
        return credentials != null
            && UsernamePasswordCredentials.class.isAssignableFrom(credentials
                .getClass());
    }

    protected void initDao() throws Exception {
        if (this.sql == null) {
            throw new IllegalStateException("sql must be set on "
                + this.getClass().getName());
        }

        if (this.passwordTranslator == null) {
            this.passwordTranslator = new PlainTextPasswordEncoder();
            log.info("No passwordTranslator set for "
                + this.getClass().getName() + ".  Using default of "
                + this.passwordTranslator.getClass().getName());
        }
    }

    /**
     * @param passwordTranslator The passwordTranslator to set.
     */
    public void setPasswordTranslator(final PasswordEncoder passwordTranslator) {
        this.passwordTranslator = passwordTranslator;
    }

    /**
     * @param sql The sql to set.
     */
    public void setSql(final String sql) {
        this.sql = sql;
    }
}