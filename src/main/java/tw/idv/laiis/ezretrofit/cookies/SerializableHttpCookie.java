package tw.idv.laiis.ezretrofit.cookies;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpCookie;

/**
 * author: <a href="http://www.jiechic.com" target="_blank">jiechic</a> on 15/5/27.<br/>
 * ref: <a href="http://www.jiechic.com/archives/cookie-automatic-management-optimization-of-okhttp-framework" target="_blank">jiechic's blog</a>
 */
@SuppressWarnings("serial")
public class SerializableHttpCookie implements Serializable {

    private transient final HttpCookie mCookie;
    private transient HttpCookie mClientCookie;

    public SerializableHttpCookie(HttpCookie cookie) {
        this.mCookie = cookie;
    }

    public HttpCookie getCookie() {
        HttpCookie bestCookie = mCookie;
        if (mClientCookie != null) {
            bestCookie = mClientCookie;
        }
        return bestCookie;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(mCookie.getName());
        out.writeObject(mCookie.getValue());
        out.writeObject(mCookie.getComment());
        out.writeObject(mCookie.getCommentURL());
        out.writeObject(mCookie.getDomain());
        out.writeLong(mCookie.getMaxAge());
        out.writeObject(mCookie.getPath());
        out.writeObject(mCookie.getPortlist());
        out.writeInt(mCookie.getVersion());
        out.writeBoolean(mCookie.getSecure());
        out.writeBoolean(mCookie.getDiscard());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        String name = (String) in.readObject();
        String value = (String) in.readObject();
        mClientCookie = new HttpCookie(name, value);
        mClientCookie.setComment((String) in.readObject());
        mClientCookie.setCommentURL((String) in.readObject());
        mClientCookie.setDomain((String) in.readObject());
        mClientCookie.setMaxAge(in.readLong());
        mClientCookie.setPath((String) in.readObject());
        mClientCookie.setPortlist((String) in.readObject());
        mClientCookie.setVersion(in.readInt());
        mClientCookie.setSecure(in.readBoolean());
        mClientCookie.setDiscard(in.readBoolean());
    }
}
