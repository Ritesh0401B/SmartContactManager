package com.smart.controller;

import java.security.Principal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailService;
import com.smart.services.SessionHelper;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    // Password encoder (for encrypting new password)
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // For user details (Spring Security)
    @Autowired
    private UserDetailsService userDetailsService;

    // Accessing user data from DB
    @Autowired
    private UserRepository userRepository;

    // For handling session-based messages
    @Autowired
    private SessionHelper sessionHelper;

    // For sending OTP emails
    @Autowired
    private EmailService emailService;

    // Constructor injection (if needed)
    public ForgotController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // 📩 Forgot Password Page
    @GetMapping("/forgot")
    public String openEmailForm(Model model) {
        model.addAttribute("title", "Forgot Password - Smart Contact Manager");
        return "forgot_email_form";
    }

    // 🔐 OTP Generation & Email Sending
    @PostMapping("/verify-otp")
    public String sendOtp(@RequestParam("email") String email, HttpSession session, Model model) {

        model.addAttribute("title", "Otp - Smart Contact Manager");
        System.out.println("EMAIL = " + email);

        // Get user from DB using email
        User user = this.userRepository.getUserByUserName(email);

        if (user == null) {
            // If user doesn't exist, show error message
            session.setAttribute("message", new Message("User does not exist with this email!!", "alert-danger"));
            return "forgot_email_form";
        } else {
            // Generate 4-digit random OTP
            int otp = 1000 + new Random().nextInt(9000);
            System.out.println("OTP = " + otp);

            // Compose OTP email
            String subject = "OTP From SCM";
            String message = "<div style='border: 1px solid #e2e2e2; padding: 20px'><h1>OTP is <b>" + otp + "</b></h1></div>";
            String to = email;

            // Send OTP to user email
            boolean flag = this.emailService.sendEmail(subject, message, to);

            if (flag) {
                // Store OTP & email in session
                session.setAttribute("myOtp", otp);
                session.setAttribute("email", email);
                model.addAttribute("email", email);
                session.setAttribute("message", new Message("Check your mail id!!", "alert-success"));
                return "verify_otp";
            } else {
                session.setAttribute("message", new Message("ERROR...", "alert-danger"));
                return "forgot_email_form";
            }
        }
    }

    // ✅ Verify OTP and Open Change Password Page
    @PostMapping("/change-password")
    public String verifyOtp(@RequestParam("otp") int otp, HttpSession session, Model model) {

        if (session.getAttribute("message") != null) {
            sessionHelper.removeMessageFromSession();
        }

        model.addAttribute("title", "Change Password - Smart Contact Manager");

        // Get OTP & email from session
        int myOtp = (int) session.getAttribute("myOtp");
        String email = (String) session.getAttribute("email");

        if (myOtp == otp) {
            // If OTP matches, go to change password page
            return "change_password";
        } else {
            // If OTP doesn't match, show error message
            model.addAttribute("email", email);
            session.setAttribute("message", new Message("You have entered wrong otp", "alert-danger"));
            return "verify_otp";
        }
    }

    // 🔒 Process New Password Submission
    @PostMapping("/forgot-process-password")
    public String forgotProcessPassword(@RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        HttpSession session,
                                        Principal principal) {

        // Get email from session
        String userName = (String) session.getAttribute("email");

        // Get user from DB
        User user = this.userRepository.getUserByUserName(userName);

        // Validate if new and confirm passwords match
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("message", new Message("New password and confirm password do not match!", "alert-danger"));
            return "change_password";
        }

        // Encrypt and update new password
        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(user);

        // Show success message and redirect to signin
        session.setAttribute("message", new Message("Password changed successfully!", "alert-success"));
        return "redirect:/signin?change=Password changed successfully!!";
    }
}