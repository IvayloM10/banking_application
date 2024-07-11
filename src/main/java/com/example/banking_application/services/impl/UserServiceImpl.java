package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;

    private CurrentUser currentUser;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.currentUser = currentUser;
    }

    @Override
    public boolean register(UserRegisterDto userRegisterDto) {
        if(!userRegisterDto.getPassword().equals(userRegisterDto.getConfirmPassword())){
            return false;
        }

        Optional<User> possibleSameUser = this.userRepository.findByUsernameOrEmail(userRegisterDto.getUsername(), userRegisterDto.getEmail());

        if(possibleSameUser.isPresent()){
            return false;
        }

        User user = modelMapper.map(userRegisterDto, User.class);
        user.setPassword(passwordEncoder.encode(userRegisterDto.getPassword()));
        this.userRepository.save(user);
        return true;
    }

    @Override
    public boolean login(UserLoginDto userLoginDto) {

        Optional<User> searchedUser = this.userRepository.findByUsername(userLoginDto.getUsername());

        if(searchedUser.isEmpty()){
            return false;
        }
        User user = searchedUser.get();

        if(!passwordEncoder.matches(userLoginDto.getPassword(), user.getPassword())){
            return false;
        }

        this.currentUser = this.modelMapper.map(userLoginDto,CurrentUser.class);
        this.currentUser.setFullName(String.join(" ",user.getFirstName(), user.getLastName()));

        return true;
    }
}
