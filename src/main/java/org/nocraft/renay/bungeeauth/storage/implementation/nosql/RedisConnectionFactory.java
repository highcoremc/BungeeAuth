package org.nocraft.renay.bungeeauth.storage.implementation.nosql;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.codec.RedisCodec;
import org.nocraft.renay.bungeeauth.storage.ConnectionFactory;
import org.nocraft.renay.bungeeauth.storage.StorageCredentials;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class RedisConnectionFactory implements ConnectionFactory<StatefulRedisConnection<String, byte[]>> {

    private StatefulRedisConnection<String, byte[]> connection;
    private final StorageCredentials credentials;
    private RedisClient client;

    public RedisConnectionFactory(StorageCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public String getImplementationName() {
        return "Redis";
    }

    @Override
    public void init() {
        RedisURI uri = new RedisURI();

        String address = this.credentials.getAddress();
        String[] addressSplit = address.split(":");
        address = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : 6379;

        String password = this.credentials.getPassword();

        uri.setHost(address);
        uri.setPort(port);
        uri.setTimeout(this.credentials.getConnectionTimeout());

        if (null != password && 0 != password.length()) {
            uri.setPassword(password);
        }

        client = RedisClient.create(uri);
    }

    @Override
    public void shutdown() {
        this.connection.close();
        this.client.shutdown();
    }

    @Override
    public StatefulRedisConnection<String, byte[]> getConnection() {
        if (connection == null || !connection.isOpen()) {
            connection = client.connect(new UserByteArrayCodec());
        }

        return this.connection;
    }

    @Override
    public Function<String, String> getStatementProcessor() {
        return null;
    }

    public static class UserByteArrayCodec implements RedisCodec<String, byte[]> {

        @Override
        public String decodeKey(ByteBuffer bytes) {
            return null;
        }

        @Override
        public byte[] decodeValue(ByteBuffer bytes) {
            return new byte[0];
        }

        @Override
        public ByteBuffer encodeKey(String key) {
            return null;
        }

        @Override
        public ByteBuffer encodeValue(byte[] value) {
            return null;
        }
    }
}
