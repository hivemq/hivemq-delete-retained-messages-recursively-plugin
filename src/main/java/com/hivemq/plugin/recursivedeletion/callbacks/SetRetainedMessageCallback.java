/*
 * Copyright 2016 dc-square GmbH
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

package com.hivemq.plugin.recursivedeletion.callbacks;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.hivemq.spi.message.RetainedMessage;
import com.hivemq.spi.services.AsyncRetainedMessageStore;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class SetRetainedMessageCallback implements FutureCallback<Set<RetainedMessage>> {
    private final String topicToRemove;
    private final Logger log;
    private final AsyncRetainedMessageStore asyncRetainedMessageStore;

    public SetRetainedMessageCallback(final String topicToRemove, final Logger log, final AsyncRetainedMessageStore asyncRetainedMessageStore) {
        this.topicToRemove = topicToRemove;
        this.log = log;
        this.asyncRetainedMessageStore = asyncRetainedMessageStore;
    }

    @Override
    public void onSuccess(Set<RetainedMessage> retainedMessages) {
        if (retainedMessages != null) {
            final List<ListenableFuture<Void>> results = new LinkedList<ListenableFuture<Void>>();
            for (final RetainedMessage retainedMessage : retainedMessages) {
                if (retainedMessage != null
                        && retainedMessage.getTopic().startsWith(topicToRemove + "/") || retainedMessage.getTopic().equals(topicToRemove)) {
                    final ListenableFuture<Void> result = asyncRetainedMessageStore.remove(retainedMessage.getTopic());
                    results.add(result);
                }
            }
            CheckResults(results);
        }
    }

    @VisibleForTesting
    void CheckResults(List<ListenableFuture<Void>> results) {
        Futures.addCallback(Futures.allAsList(results), new FutureCallback<List<Void>>() {
            @Override
            public void onSuccess(List<Void> result) {

            }

            @Override
            public void onFailure(Throwable t) {
                log.trace("Failed to remove retained message {}", t);
            }
        });
    }

    @Override
    public void onFailure(Throwable t) {
        log.trace("Failed to get retained messages for topic {}, {}", topicToRemove, t);
    }
}
