package com.mediforme.mediforme.controller;

import com.mediforme.mediforme.domain.Member;
import com.mediforme.mediforme.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final RegisterService registerService;

    @Autowired
    public MemberController(RegisterService userService) {
        this.registerService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Member> registerUser(@RequestBody Member member) {
        Member registeredUser = registerService.registerUser(member);
        return ResponseEntity.ok(registeredUser);
    }
}
