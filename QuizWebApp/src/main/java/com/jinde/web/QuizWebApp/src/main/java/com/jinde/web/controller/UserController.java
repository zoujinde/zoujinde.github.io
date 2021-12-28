package com.jinde.web.controller;

import java.util.List;

import com.jinde.web.model.User;

public class UserController {

    private MailController mailService;

    public void setMailService(MailController mailService) {
        this.mailService = mailService;
    }

    private List<User> users = null; /*new ArrayList<>(List.of(
            new User(1, "bob@example.com", "password", "Bob"),
            new User(2, "alice@example.com", "password", "Alice"),
            new User(3, "tom@example.com", "password", "Tom")));*/

    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                mailService.sendLoginMail(user);
                return user;
            }
        }
        throw new RuntimeException("login failed.");
    }

    /*
    public User getUser(long id) {
        return this.users.stream().filter(user -> user.getId() == id).findFirst().orElseThrow();
    }

    public User register(String email, String password, String name) {
        users.forEach((user) -> {
            if (user.getEmail().equalsIgnoreCase(email)) {
                throw new RuntimeException("email exist.");
            }
        });
        User user = new User(users.stream().mapToLong(u -> u.getId()).max().getAsLong() + 1, email, password, name);
        users.add(user);
        mailService.sendRegistrationMail(user);
        return user;
    }*/

}
