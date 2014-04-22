package com.acme.callbacks;

import com.dcsquare.hivemq.spi.message.PUBLISH;
import com.dcsquare.hivemq.spi.message.QoS;
import com.dcsquare.hivemq.spi.message.RetainedMessage;
import com.dcsquare.hivemq.spi.services.RetainedMessageStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Lukas Brandl
 */
public class PublishReceivedTest {

    @Mock
    RetainedMessageStore retainedMessageStore;

    PublishReceived publishReceived;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        Set<RetainedMessage> retainedMessages = new HashSet<RetainedMessage>();
        retainedMessages.add(new RetainedMessage("test", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/a", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/b", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("test/bla/bla", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("abc", new byte[]{1, 2, 3}, QoS.valueOf(0)));
        retainedMessages.add(new RetainedMessage("testtopic", new byte[]{1, 2, 3}, QoS.valueOf(0)));

        when(retainedMessageStore.getRetainedMessages()).thenReturn(retainedMessages);
        publishReceived = new PublishReceived(retainedMessageStore);

    }

    @Test
    public void test_remove_main_topic() throws Exception {
        PUBLISH deletePublish = new PUBLISH();
        deletePublish.setTopic("test");
        deletePublish.setRetain(true);
        deletePublish.setPayload(new byte[]{});
        publishReceived.onPublishReceived(deletePublish, null);
        verify(retainedMessageStore).remove("test");
    }

    @Test
    public void test_remove_sub_topics() throws Exception {
        PUBLISH deletePublish = new PUBLISH();
        deletePublish.setTopic("test");
        deletePublish.setRetain(true);
        deletePublish.setPayload(new byte[]{});
        publishReceived.onPublishReceived(deletePublish, null);
        verify(retainedMessageStore).remove("test/a");
        verify(retainedMessageStore).remove("test/b");
        verify(retainedMessageStore).remove("test/bla/bla");
    }

    @Test
    public void test_dont_remove_other_topics() throws Exception {
        PUBLISH deletePublish = new PUBLISH();
        deletePublish.setTopic("test");
        deletePublish.setRetain(true);
        deletePublish.setPayload(new byte[]{});
        publishReceived.onPublishReceived(deletePublish, null);
        verify(retainedMessageStore, never()).remove("testtopic");
        verify(retainedMessageStore, never()).remove("abc");
    }
}
