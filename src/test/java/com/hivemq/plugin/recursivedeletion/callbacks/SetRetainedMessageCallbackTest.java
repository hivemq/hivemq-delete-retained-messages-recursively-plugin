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

import com.google.common.util.concurrent.ListenableFuture;
import com.hivemq.spi.message.QoS;
import com.hivemq.spi.message.RetainedMessage;
import com.hivemq.spi.services.AsyncRetainedMessageStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;


public class SetRetainedMessageCallbackTest {

    @Mock
    AsyncRetainedMessageStore asyncRetainedMessageStore;

    @Mock
    Logger log;

    @Mock
    ListenableFuture<Void> future;

    Set<RetainedMessage> retainedMessages;
    SetRetainedMessageCallback setRetainedMessageCallback;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        retainedMessages = new HashSet<RetainedMessage>();
        retainedMessages.add(new RetainedMessage("test", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/a", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/b", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/bla/bla", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("abc", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("testtopic", new byte[]{1, 2, 3}, QoS.valueOf(0)));

        when(asyncRetainedMessageStore.remove(anyString())).thenReturn(future);

        when(future.isDone()).thenReturn(true);

        setRetainedMessageCallback = spy(new SetRetainedMessageCallback("test", log, asyncRetainedMessageStore));

        Mockito.doNothing().when(setRetainedMessageCallback).CheckResults((List<ListenableFuture<Void>>) Mockito.any());
    }

    @Test
    public void testRemoveSubtopic() throws Exception {
        setRetainedMessageCallback.onSuccess(retainedMessages);
        verify(asyncRetainedMessageStore).remove("test");
        verify(asyncRetainedMessageStore).remove("test/a");
        verify(asyncRetainedMessageStore).remove("test/b");
        verify(asyncRetainedMessageStore).remove("test/bla/bla");
    }

    @Test
    public void testDontRemove() throws Exception {
        setRetainedMessageCallback.onSuccess(retainedMessages);
        verify(asyncRetainedMessageStore, never()).remove("abc");
        verify(asyncRetainedMessageStore, never()).remove("testtopic");
    }


}