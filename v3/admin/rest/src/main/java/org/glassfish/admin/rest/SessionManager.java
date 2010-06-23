/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Rest Sessions. 
 * @author Mitesh Meswani
 */
public class SessionManager {

    //TODO Singleton pattern. Get rid of it in next checkin. Put SessionManager at application level in ResourceConfig
    private static SessionManager theSessionManager = new SessionManager();
    private SessionManager() {}
    public static SessionManager getSessionManager() { return theSessionManager;}

    private final SecureRandom randomGenerator = new SecureRandom();

    private Map<Long,SessionData > activeSessions = new ConcurrentHashMap<Long, SessionData>(); //To guard against parallel mutation corrupting the map  

    //TODO createSession is public. this package should not be exported
    public String createSession() {
        long sessionId;
        do {
            sessionId = randomGenerator.nextLong();
        } while(isSessionExist(sessionId));

        saveSession(sessionId);
        return Long.toString(sessionId);
    }

    public boolean authenticate(String sessionId) {
        boolean authenticated = false;

        Long sessionIdKey = convertToSessionIdKey(sessionId);

        if(sessionIdKey != null) {
            SessionData sessionData = activeSessions.get(sessionIdKey);
            if(sessionData != null) {
                authenticated = sessionData.isSessionActive();
                if(authenticated) {
                    // update last access time
                    sessionData.updateLastAccessTime();
                } else {
                    activeSessions.remove(sessionIdKey);
                }
            }
        }
        return authenticated;
    }

    /**
     * Deletes Session corresponding to given <code> sessionId </code>
     * @param sessionId
     * @return
     */
    public boolean deleteSession(String sessionId) {
        boolean sessionDeleted = false;
        Long sessionIdKey = convertToSessionIdKey(sessionId);
        if(sessionId != null) {
            SessionData removedSession = activeSessions.remove(sessionIdKey);
            sessionDeleted = (removedSession != null);
        }
        return sessionDeleted;
    }


    /**
     * @return converted <code>String sessionId</code>to Long. Null if the String can not be parsed to Long
     */
    private Long convertToSessionIdKey(String sessionId) {
        Long sessionIdKey;
        try {
            sessionIdKey = Long.parseLong(sessionId);
        }catch (NumberFormatException e) {
            sessionIdKey = null;
        }
        return sessionIdKey;
    }


    private void saveSession(long sessionId) {
        purgeInactiveSessions();
        activeSessions.put(sessionId, new SessionData(sessionId) );
    }

    private void purgeInactiveSessions() {
        Set<Map.Entry<Long, SessionData>> activeSessionsSet = activeSessions.entrySet();
        for (Map.Entry<Long, SessionData> entry : activeSessionsSet) {
            if(!entry.getValue().isSessionActive() ) {
                activeSessionsSet.remove(entry);
            }
        }
    }

    private boolean isSessionExist(long sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    private static class SessionData{
        private static long INACTIVE_SESSION_DEFAULT_LIFETIME_IN_MILIS = 30 /*mins*/ * 60 /*secs/min*/ * 1000 /*milis/seconds*/;
        private long sessionId;
        private long creationTime = System.currentTimeMillis();
        private long lassAccessedTime = creationTime;
        private long inactiveSessionLifeTime = INACTIVE_SESSION_DEFAULT_LIFETIME_IN_MILIS;

        public SessionData(long sessionId) {
            this.sessionId = sessionId;
        }

        /**
         * @return true if the session has not timed out. false otherwise
         */
        public boolean isSessionActive() {
            return lassAccessedTime + inactiveSessionLifeTime > System.currentTimeMillis();
        }

        /**
         * Update the last accessed time to current time
         */
        public void updateLastAccessTime() {
            lassAccessedTime = System.currentTimeMillis();
        }
    }


}
