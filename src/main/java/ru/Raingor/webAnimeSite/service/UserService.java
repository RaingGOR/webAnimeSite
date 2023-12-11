package ru.Raingor.webAnimeSite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import ru.Raingor.webAnimeSite.dtos.RegistrationUserDto;
import ru.Raingor.webAnimeSite.dtos.UserDTO;
import ru.Raingor.webAnimeSite.exceptions.UserNotCreatedException;
import ru.Raingor.webAnimeSite.models.User;
import ru.Raingor.webAnimeSite.repository.UserRepository;
import ru.Raingor.webAnimeSite.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleService roleService;

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // CRUD
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(int id) {
        Optional<User> foundUserById = userRepository.findById(id);
        return foundUserById.orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void saveUserInDataBase(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateUserInDB(int id, User user) {
        user.setId(id);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(int id) {
        userRepository.deleteById(id);
    }

    // New additional

    public int countUsers() {
        List<User> users = userRepository.findAll();
        return users.size();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByName(username);
    }

//    @Override
//    @Transactional
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(
//                String.format("Пользователь '%s' не найден", username)
//        ));
//
//        return new org.springframework.security.core.userdetails.User(
//                user.getName(),
//                user.getPassword(),
//                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList())
//        );
//    }

    //Convert UserDTO -> User
    public User convertToUser(RegistrationUserDto registrationUserDto) {
        User user = new User();

        user.setName(registrationUserDto.getName());
        user.setEmail(registrationUserDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationUserDto.getPassword()));
        user.setRoles(List.of(roleService.getUsersRole()));

        return user;
    }

    //convert User -> UserDTO
    public UserDTO convertToUserDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }

    //logic
    public ResponseEntity<?> getAllUsersDto() {
        List<UserDTO> userDTOS = this.getAllUsers().stream().map(this::convertToUserDTO).collect(Collectors.toList());
        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    public ResponseEntity<?> getUserDtoById(int id) {
        UserDTO userDTO = convertToUserDTO(getUserById(id));
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> createNewUser(RegistrationUserDto regUserDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError fieldError : errors) {
                errorMsg
                        .append(fieldError.getField())
                        .append(" - ")
                        .append(fieldError.getDefaultMessage())
                        .append(";");
            }

            throw new UserNotCreatedException(errorMsg.toString());
        }
        saveUserInDataBase(convertToUser(regUserDto));

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Transactional
    public ResponseEntity<?> updateUser(int id, RegistrationUserDto registrationUserDto) {
        updateUserInDB(id, convertToUser(registrationUserDto));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> deleteUserControl(int id) {
        deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
