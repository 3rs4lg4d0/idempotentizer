package com.ersalgado.idempotentizer.sql;

import java.time.Instant;
import java.util.Objects;

public class UserDto {

    private Integer id;
    private String name;
    private Instant createdAt;

    public UserDto() {}

    public UserDto(Integer id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserDto userDto = (UserDto) obj;
        return Objects.equals(id, userDto.id)
                && Objects.equals(name, userDto.name)
                && Objects.equals(createdAt, userDto.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, createdAt);
    }
}
