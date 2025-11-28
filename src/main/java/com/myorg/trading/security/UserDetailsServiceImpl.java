package com.myorg.trading.security;

import com.myorg.trading.domain.entity.User;
import com.myorg.trading.domain.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.userRepository = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // convert role string to authorities
        List<SimpleGrantedAuthority> auth = List.of(new SimpleGrantedAuthority(u.getRole() == null ? "ROLE_USER" : u.getRole()));
        return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPasswordHash(), auth);
    }
}
