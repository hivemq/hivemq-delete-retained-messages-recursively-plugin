/*
 * Copyright 2013 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.acme.callbacks;

import com.dcsquare.hivemq.spi.callback.CallbackPriority;
import com.dcsquare.hivemq.spi.callback.events.OnPublishReceivedCallback;
import com.dcsquare.hivemq.spi.callback.exception.OnPublishReceivedException;
import com.dcsquare.hivemq.spi.message.PUBLISH;
import com.dcsquare.hivemq.spi.message.RetainedMessage;
import com.dcsquare.hivemq.spi.security.ClientData;
import com.dcsquare.hivemq.spi.services.RetainedMessageStore;
import com.google.inject.Inject;

public class PublishReceived implements OnPublishReceivedCallback {

    RetainedMessageStore retainedMessageStore;

    @Inject
    PublishReceived(final RetainedMessageStore retainedMessageStore) {
        this.retainedMessageStore = retainedMessageStore;
    }


    @Override
    public void onPublishReceived(final PUBLISH publish, final ClientData clientData) throws OnPublishReceivedException {
        final String message = new String(publish.getPayload());
        if (message.isEmpty() && publish.isRetain()) {
            removeRetainedMessagesRecursively(publish);
        }
    }

    private void removeRetainedMessagesRecursively(final PUBLISH publish) {
        final String topicToRemove = publish.getTopic();
        for (RetainedMessage retainedMessage : retainedMessageStore.getRetainedMessages()) {
            if (retainedMessage.getTopic().startsWith(topicToRemove + "/") || retainedMessage.getTopic().equals(topicToRemove)) {
                retainedMessageStore.remove(retainedMessage.getTopic());
            }
        }
    }

    @Override
    public int priority() {
        return CallbackPriority.MEDIUM;
    }
}
