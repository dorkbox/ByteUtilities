module dorkbox.byteUtils {
    exports dorkbox.bytes;

    requires transitive dorkbox.updates;

    requires static com.esotericsoftware.kryo;
    requires static io.netty.common;
    requires static io.netty.buffer;
    requires static org.lz4.java;

    requires transitive kotlin.stdlib;
}
