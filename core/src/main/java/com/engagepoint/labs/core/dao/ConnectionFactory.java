package com.engagepoint.labs.core.dao;


import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.bindings.CmisBindingFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: r.reznichenko
 * Date: 6/17/13
 * Time: 1:39 PM
 */
public class ConnectionFactory {

    private static Session session;

    /**
     * Return session
     *
     * @return session
     */
    public static Session getSession() {
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.WEBSERVICES.value());
        //        parameter.put(SessionParameter.ATOMPUB_URL, "http://lab5:8080/chemistry-opencmis-server-inmemory-0.9.0/atom11");
        parameter.put(SessionParameter.ATOMPUB_URL, "http://repo.opencmis.org/inmemory/atom/");

        List<Repository> repositories = sessionFactory.getRepositories(parameter);
        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        session = sessionFactory.createSession(parameter);
        return session;
    }

}
