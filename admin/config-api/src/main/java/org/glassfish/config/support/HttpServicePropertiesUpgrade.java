/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.glassfish.config.support;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.AccessLog;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.grizzly.config.dom.Http;
import com.sun.grizzly.config.dom.Protocol;
import org.glassfish.api.admin.AdminCommandContext;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

@Service
public class HttpServicePropertiesUpgrade extends BaseLegacyConfigurationUpgrade {
    @Inject
    private Configs configs;

    public void execute(AdminCommandContext context) {
        for (Config config : configs.getConfig()) {
            HttpService service = config.getHttpService();
            if (service != null) {
                try {
                    for (Property property : service.getProperty()) {
                        upgrade(context, property, service);
                    }
                } catch (TransactionFailure tf) {
                    Logger.getAnonymousLogger().log(Level.SEVERE, "Failure while upgrading http-service properties."
                        + "  Please check logs for errors", tf);
                    throw new RuntimeException(tf);
                }
            }
        }
    }

    private void upgrade(final AdminCommandContext context, final Property property, final HttpService service)
        throws TransactionFailure {
        final String name = property.getName();
        if ("accessLoggingEnabled".equals(name)) {
            updatePropertyToAttribute(context, service, "accessLoggingEnabled", "accessLoggingEnabled");
        } else if ("accessLogBufferSize".equals(name)) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setBufferSizeBytes(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogBufferSize");
            report(context,
                "Moved http-service.property.accessLogBufferSize to http-service.access-log.buffer-size-bytes");
        } else if ("accessLogWriteInterval".equals(name)) {
            ConfigSupport.apply(new SingleConfigCode<AccessLog>() {
                @Override
                public Object run(AccessLog param) {
                    param.setWriteIntervalSeconds(property.getValue());
                    return param;
                }
            }, service.getAccessLog());
            removeProperty(service, "accessLogWriteInterval");
            report(context,
                "Moved http-service.property.accessLogWriteInterval to http-service.access-log.write-interval-seconds");
        } else if ("sso-enabled".equals(name)) {
            updatePropertyToAttribute(context, service, "sso-enabled", "ssoEnabled");
        } else if ("authPassthroughEnabled".equalsIgnoreCase(name)) {
            for (Config config : configs.getConfig()) {
                System.out.println("config = " + config);
                for (Protocol protocol : config.getNetworkConfig().getProtocols().getProtocol()) {
                    System.out.println("protocol = " + protocol);
                    System.out.println("protocol.getHttp() = " + protocol.getHttp());
                    if (protocol.getHttp() != null) {
                        ConfigSupport.apply(new SingleConfigCode<Http>() {
                            @Override
                            public Object run(Http param) {
                                param.setAuthPassThroughEnabled(property.getValue());
                                return param;
                            }
                        }, protocol.getHttp());
                    }
                }
            }
            removeProperty(service, name);
            report(context,
                "Moved http-service.property.authPassthroughEnabled to http.auth-pass-through-enabled");
        }
    }
}
