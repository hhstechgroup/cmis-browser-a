package com.engagepoint.labs.core.dao;


import com.engagepoint.labs.core.models.exceptions.BaseException;
import com.engagepoint.labs.core.models.exceptions.ConnectionException;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 1:39 PM
 */
public class ConnectionFactory {

    private static ConnectionFactory connectionFactory;

    private Session session;
    private String url;
    private String username;
    private String password;

    private ConnectionFactory() {
    }

    /**
     * Return session
     *
     * @return session
     */

    public Session getSession() throws BaseException {
        try {
            SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
            Map<String, String> parameter = new HashMap<String, String>();
            parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            if (!username.isEmpty()) {
                parameter.put(SessionParameter.USER, username);
            }
            if (!password.isEmpty()) {
                parameter.put(SessionParameter.PASSWORD, password);
            }
            parameter.put(SessionParameter.ATOMPUB_URL, url);
//        parameter.put(SessionParameter.ATOMPUB_URL, "http://repo.opencmis.org/inmemory/atom/");

            List<Repository> repositories = sessionFactory.getRepositories(parameter);
            Repository repository = repositories.get(0);
            parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
            session = sessionFactory.createSession(parameter);

        } catch (CmisConnectionException e) {
            throw new ConnectionException(e.getMessage());
        } catch (CmisBaseException e) {
            throw new BaseException(e.getMessage());
        }
        return session;
    }

    public static ConnectionFactory getInstance() {
        if (connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
        }
        return connectionFactory;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
