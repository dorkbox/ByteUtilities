module dorkbox.bytes {
    exports dorkbox.bytes;

    requires transitive dorkbox.updates;

    requires static com.esotericsoftware.kryo;
    requires static io.netty.common;
    requires static io.netty.buffer;

    requires transitive kotlin.stdlib;
}
