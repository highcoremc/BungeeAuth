package org.nocraft.renay.bungeeauth.storage.implementation.sql.converters;

import org.nocraft.renay.bungeeauth.authentication.hash.HashMethodType;
import org.sql2o.converters.Converter;
import org.sql2o.converters.ConverterException;

public class HashMethodConverter implements Converter<HashMethodType> {

    @Override
    public HashMethodType convert(Object val) throws ConverterException {
        if (val == null) {
            return null;
        }

        if (val instanceof HashMethodType) {
            return (HashMethodType) val;
        }

        if (val instanceof String) {
            return HashMethodType.parse((String) val);
        }

        throw new ConverterException("Cannot convert type " + val.getClass() + " " + HashMethodType.class);
    }

    @Override
    public Object toDatabaseParam(HashMethodType val) {
        return val.toString();
    }
}
