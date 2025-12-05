package com.shadowfax.vacationbookingsystem.service;

import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.model.UserPrincipal;
import com.shadowfax.vacationbookingsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImp implements UserDetailsService {

    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsImp.class);

    public UserDetailsImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.info("Attempting to load user: {}", username);

        User user = userRepository.findByUsername(username);

        if (user == null) {
            logger.error("User not found: {}", username);
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        logger.info("User found: {}", username);

        return new UserPrincipal(user);
    }
}
