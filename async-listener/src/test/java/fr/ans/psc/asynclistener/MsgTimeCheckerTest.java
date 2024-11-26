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
package fr.ans.psc.asynclistener;

import fr.ans.psc.asynclistener.consumer.MsgTimeChecker;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MsgTimeCheckerTest {

    @Test
    void testSingletonInstance() {
        MsgTimeChecker instance1 = MsgTimeChecker.getInstance();
        MsgTimeChecker instance2 = MsgTimeChecker.getInstance();
        assertEquals(instance1, instance2);
    }

    @Test
    void testSetMsgConsumptionTimestamp() {
        MsgTimeChecker instance = MsgTimeChecker.getInstance();
        Instant expectedTimestamp = Instant.parse("2020-01-01T00:00:00Z");
        instance.setTimestamp(expectedTimestamp);
        assertNotNull(instance.getMsgConsumptionTimestamp());
        assertEquals(expectedTimestamp, instance.getMsgConsumptionTimestamp());
    }

    @Test
    void testHasRecentConsumptionTimestamp() {
        MsgTimeChecker instance = MsgTimeChecker.getInstance();
        instance.setMsgConsumptionTimestamp();
        assertTrue(instance.hasRecentConsumptionTimestamp());
    }

    @Test
    void testNoRecentConsumptionTimestamp() {
        MsgTimeChecker instance = MsgTimeChecker.getInstance();
        instance.setTimestamp(Instant.now().minusSeconds(5));
        assertFalse(instance.hasRecentConsumptionTimestamp());
    }
}

