package org.mongojack.internal.stream;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import org.bson.BsonWriter;
import org.bson.UuidRepresentation;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.mongojack.MongoJsonMappingException;

import java.io.IOException;

public class JacksonEncoder<T> implements Encoder<T> {

    private final Class<T> clazz;
    private final Class<?> view;
    private final ObjectMapper objectMapper;
    private final UuidRepresentation uuidRepresentation;

    public JacksonEncoder(Class<T> clazz, Class<?> view, ObjectMapper objectMapper, final UuidRepresentation uuidRepresentation) {
        this.clazz = clazz;
        this.view = view;
        this.objectMapper = objectMapper;
        this.uuidRepresentation = uuidRepresentation;
    }

    public JacksonEncoder<T> withUuidRepresentation(final UuidRepresentation uuidRepresentation) {
        return new JacksonEncoder<>(
            clazz,
            view,
            objectMapper,
            uuidRepresentation
        );
    }

    @Override
    public void encode(BsonWriter writer, T value, EncoderContext encoderContext) {
        try(JsonGenerator generator = new DBEncoderBsonGenerator(writer, uuidRepresentation)) {
            objectMapper.writerWithView(view).writeValue(generator, value);
        } catch (JsonMappingException e) {
            throw new MongoJsonMappingException(e);
        } catch (IOException e) {
            throw new MongoException("Error writing object out", e);
        }
    }

    @Override
    public Class<T> getEncoderClass() {
        return clazz;
    }
}
