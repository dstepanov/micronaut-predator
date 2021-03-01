package io.micronaut.data.jdbc.postgres;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.data.model.DataType;

import javax.persistence.ManyToOne;
import java.util.UUID;

@MappedEntity
public class UuidTest {
    @GeneratedValue
    @Id
    private UUID uuid;

    @ManyToOne(targetEntity=UuidChild.class)
    @MappedProperty(type = DataType.UUID)
    private UuidChild child;

    private String name;

    public UuidTest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UuidChild getChild() {
        return child;
    }

    public void setChild(UuidChild child) {
        this.child = child;
    }

}
