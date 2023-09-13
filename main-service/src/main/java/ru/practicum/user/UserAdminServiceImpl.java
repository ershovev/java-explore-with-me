package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.UserNotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> usersIds, int from, int size) {
        List<User> users;
        if (usersIds != null) {
            users = userRepository.findAllByIdIn(usersIds);
        } else {
            Pageable pageable = PageRequest.of((from / size), size);
            users = userRepository.findAll(pageable).getContent();
        }
        return UserMapper.toUserDtoList(users);
    }

    @Override
    @Transactional
    public UserDto saveNewUser(NewUserRequest newUserRequest) {
        User user = UserMapper.toUser(newUserRequest);
        User createdUser = userRepository.save(user);
        log.info("Добавлен пользователь: " + createdUser.toString());

        return UserMapper.toUserDto(createdUser);
    }

    @Override
    @Transactional
    public void deleteUser(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        userRepository.deleteById(userId);
        log.info("Удален пользователь с id: " + userId);
    }
}