/*
 * Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.ans.psc.asynclistener.consumer;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

@Getter
public class MsgTimeChecker {

    private static final int MSG_ACTIVITY_WINDOW = 3; // in seconds

    private volatile Instant msgConsumptionTimestamp = null;

    private MsgTimeChecker() {}
    
    private static class Holder {
        private static final MsgTimeChecker INSTANCE = new MsgTimeChecker();
    }
    
    public static MsgTimeChecker getInstance() {
        return Holder.INSTANCE;
    } 

    public synchronized void setMsgConsumptionTimestamp() {
        this.msgConsumptionTimestamp = Instant.now();
    }

    // Only used in tests
    public synchronized void setTimestamp(Instant msgConsumptionTimestamp) {
        this.msgConsumptionTimestamp = msgConsumptionTimestamp;
    }

    public boolean hasRecentConsumptionTimestamp() {
        if (msgConsumptionTimestamp == null) {
            return false;
        }
        Instant now = Instant.now();
        return Duration.between(msgConsumptionTimestamp, now).toSeconds() < MSG_ACTIVITY_WINDOW;
    }
}
