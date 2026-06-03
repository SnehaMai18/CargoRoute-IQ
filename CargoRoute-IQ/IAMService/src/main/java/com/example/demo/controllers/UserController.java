package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.UserDTO;
import com.example.demo.DTO.UserUpdateDTO;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/cargoRoute/user")
public class UserController {

    @Autowired
    private UserService userService;
    UserRepository usersRepository;

    // CREATE / UPDATE USER (ADMIN)
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO usersDTO) {

        UserDTO savedUser = userService.createUser(usersDTO);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // GET ALL USERS (ADMIN)
    @GetMapping("/getAllUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {

        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // GET USER BY ID (ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {

        UserDTO user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
    
    
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMyProfile(
            HttpServletRequest request
    ) {
        Object id = request.getAttribute("userId");
        if (!(id instanceof Number)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = ((Number) id).longValue();
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO dto) {

        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

}