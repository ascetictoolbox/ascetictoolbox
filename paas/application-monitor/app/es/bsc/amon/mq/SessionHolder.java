package es.bsc.amon.mq;

import javax.jms.Session;
import javax.jms.TopicSession;

/**
 * Created by mmacias on 17/8/16.
 */
public class SessionHolder {
    private TopicSession session;

    public SessionHolder(TopicSession session) {
        this.session = session;
    }

    public TopicSession getSession() {
        return session;
    }

    public void setSession(TopicSession session) {
        this.session = session;
    }
}
