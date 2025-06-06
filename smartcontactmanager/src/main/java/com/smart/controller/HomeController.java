package com.smart.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.SessionHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	
	@Autowired
	private SessionHelper sessionHelper;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model, HttpSession session) {
		
		if(session.getAttribute("message") != null) {
			sessionHelper.removeMessageFromSession();
		}
		model.addAttribute("title", "Home - Smart Contact Manager");

		return "home";

	}

	@RequestMapping("/about")
	public String about(Model model, HttpSession session) {
		
		if(session.getAttribute("message") != null) {
			sessionHelper.removeMessageFromSession();
		}
		model.addAttribute("title", "About - Smart Contact Manager");

		return "about";

	}

	@RequestMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());

		return "signup";

	}

	// handler for registering user

//	@RequestMapping(value = "/do_register", method=RequestMethod.POST)
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		// yeh @ModelAttribute user ko form ke data se automatically bind kar dega

		try {
			
			if(bindingResult.hasErrors()) {
				System.out.println("ERROR "+bindingResult.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println("Agreement " + agreement);
			System.out.println("USER " + user);
			
			User existingUser = this.userRepository.getUserByUserName(user.getEmail());

			if(existingUser == null) {
				
				User result = this.userRepository.save(user);

				model.addAttribute("user", new User());
				session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));

				return "login";
				
			}else {
				
				model.addAttribute("user", user);
				session.setAttribute("message", new Message("Something went wrong!! Email already registered!", "alert-danger"));

				return "signup";
			}
			
			

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!!" + e.getMessage(), "alert-danger"));

			return "signup";
		}

	}
	

	// handler for custom login
	
	@GetMapping("/signin")
	public String customLogin(Model model, HttpSession session) {
		
		model.addAttribute("title", "Login - Smart Contact Manager");
		
		if(session.getAttribute("message") != null) {
			sessionHelper.removeMessageFromSession();
		}		
		return "login";
	}
	
	
	
}
