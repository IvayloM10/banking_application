package com.example.banking_application.services.impl;

import com.example.banking_application.config.CurrentUser;
import com.example.banking_application.models.dtos.CardDto;
import com.example.banking_application.models.dtos.UserLoginDto;
import com.example.banking_application.models.dtos.UserRegisterDto;
import com.example.banking_application.models.entities.Account;
import com.example.banking_application.models.entities.Card;
import com.example.banking_application.models.entities.User;
import com.example.banking_application.repositories.AccountRepository;
import com.example.banking_application.repositories.CardRepository;
import com.example.banking_application.repositories.UserRepository;
import com.example.banking_application.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private ModelMapper modelMapper;
    private CardRepository cardRepository;

    private AccountRepository accountRepository;

    private CurrentUser currentUser;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper, CardRepository cardRepository, AccountRepository accountRepository, CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
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
    @Transactional
    public void createCardAndAccountForUser(Long userId, CardDto cardDetails) {
        User byId = this.userRepository.findById(userId).get();
        Card card = new Card();
        card.setCvvNumber(generateCVV());
        card.setAccountNumber(generateCardNumber());
        card.setExpirationDate(LocalDate.now());
        card.setType(cardDetails.getCardType());
        card.setCurrency(cardDetails.getCurrency());
        byId.setCard(card);
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setUser(byId);
        account.setCurrency(card.getCurrency());
        byId.setAccount(account);
        this.accountRepository.save(account);
        this.cardRepository.save(card);
    }


    @Override
    public User getCurrentUser(Long id) {
        return this.userRepository.findById(id).get();
    }

    private String generateAccountNumber() {
        return "AC" + new Random().nextInt(999999);
    }

    private String generateCardNumber() {
        return "CARD" + new Random().nextInt(99999999);
    }

    private String generateCVV() {
        return String.valueOf(new Random().nextInt(999));
    }

}
