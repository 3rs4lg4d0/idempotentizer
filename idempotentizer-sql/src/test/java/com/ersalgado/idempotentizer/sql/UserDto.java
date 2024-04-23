package com.ersalgado.idempotentizer.sql;

import java.util.Objects;

public class UserDto {

    private Integer id;
    private String name;

    public UserDto() {}

    public UserDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserDto userDto = (UserDto) obj;
        return Objects.equals(id, userDto.id) && Objects.equals(name, userDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
