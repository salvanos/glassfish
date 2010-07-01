/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txr.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txr.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.admin.rest;

import java.util.HashSet;
import java.util.Set;

import org.glassfish.admin.rest.staticresources.SessionsResource;
import org.jvnet.hk2.annotations.Service;

/**
 * Adapter for REST management interface
 * @author Rajeshwar Patil , Ludovic Champenois
 */
@Service
public class RestManagementAdapter extends RestAdapter {

    @Override
    public String getContextRoot() {
        return CONTEXT;
    }

    @Override
    protected Set<Class<?>> getResourcesConfig() {
        final Set<Class<?>> r = new HashSet<Class<?>>();

        // uncomment if you need to run the generator:
        r.add(GeneratorResource.class);
        r.add(org.glassfish.admin.rest.resources.DomainResource.class);
        r.add(SessionsResource.class); //TODO this needs to be added to all rest adapters that want to be secured. Decide on it after the discussion to unify RestAdapter is concluded

        //body readers, not in META-INF/services anymore
        r.add(org.glassfish.admin.rest.provider.FormReader.class);
        r.add(org.glassfish.admin.rest.provider.ParameterMapFormReader.class);
        r.add(org.glassfish.admin.rest.provider.JsonHashMapProvider.class);
        r.add(org.glassfish.admin.rest.provider.XmlHashMapProvider.class);

        //body writers
        r.add(org.glassfish.admin.rest.provider.GetResultListJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultListXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultListHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.FormWriter.class);
        r.add(org.glassfish.admin.rest.provider.GetResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.GetResultHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.OptionsResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.OptionsResultXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.ActionReportResultHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.StringListResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.StringListResultXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.StringListResultHtmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.CommandResourceGetResultJsonProvider.class);
        r.add(org.glassfish.admin.rest.provider.CommandResourceGetResultXmlProvider.class);
        r.add(org.glassfish.admin.rest.provider.CommandResourceGetResultHtmlProvider.class);


        return r;
    }
    public static final String CONTEXT = "/management";
}
