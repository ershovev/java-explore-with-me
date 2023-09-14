package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserAdminService {

    List<UserDto> getUsers(List<Long> usersIds, int size, int from);

    UserDto saveNewUser(NewUserRequest userDto);

    void deleteUser(long userId);
}
