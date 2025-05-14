package com.smart.controller;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;
import com.smart.config.MyConfig;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.SessionHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private SessionHelper sessionHelper;

	UserController(HomeController homeController, UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder, MyConfig myConfig, ForgotController forgotController) {
		this.passwordEncoder = passwordEncoder;
	}

	// method for adding common data to response (sabhi method me use karne ke liye)

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String username = principal.getName();
		System.out.println("USERNAME " + username);

		User user = this.userRepository.getUserByUserName(username);
		System.out.println("USER = " + user);

		model.addAttribute("user", user);

	}

	// Dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard - Smart Contact Manager");

		sessionHelper.removeMessageFromSession();

		return "normal/user_dashboard";

	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {

		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());

		sessionHelper.removeMessageFromSession();

		return "normal/add_contact_form";

	}

	// processing add contact form

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, BindingResult bindingResult, Principal principal,
			Model model, HttpSession session) {

		try {

			if (bindingResult.hasErrors()) {
				System.out.println("ERROR " + bindingResult.toString());

				model.addAttribute("contact" + contact);

				return "normal/add_contact_form";
			}

			String name = principal.getName();

			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file

			if (file.isEmpty()) {
				//
				System.out.println("File is empty");
				// agar image value null hai tb ham
				contact.setImage("contact.png");

			} else {

				contact.setImage(file.getOriginalFilename());

				File file2 = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded");

			}

			contact.setUser(user);

			user.getContacts().add(contact);

			this.userRepository.save(user);

			System.out.println("CONTACT = " + contact);

			System.out.println("Added to data base");

			model.addAttribute("contact", new Contact());

			session.setAttribute("message", new Message("Successfully added !!", "alert-success"));

			return "normal/add_contact_form";

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

			session.setAttribute("message", new Message("Something went wrong !!", "alert-danger"));

			return "normal/add_contact_form";

		}

	}

	// Show contact handler
	// per page 5 contact bhej rhe hai baad me ise n number of contacts kar sakte
	// hai hm
	//

	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "View Contacts - Smart Contact Manager");

//		// contacts ki list bhejni hai yaha se  +++ ham isko use kar sakte hai lekin ham yaha comntact ki repository use karenge
//		String usrName = principal.getName();
//		User user = this.userRepository.getUserByUserName(usrName);
//		List<Contact> contacts = user.getContacts();

		// Prevent negative page number
		if (page == null || page < 0) {
			page = 0;
		}
		sessionHelper.removeMessageFromSession();

		String userName = principal.getName();

		User user = this.userRepository.getUserByUserName(userName);

		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);

		model.addAttribute("currentPage", page);

		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contact";
	}
	

//	@PostMapping("/search")
//	public String searchContact(@RequestParam("query") String query, Principal principal, Model model) {
//		
//		String userName = principal.getName();
//		
//		User user = this.userRepository.getUserByUserName(userName);
//		
//		List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
//		List<Contact> byPhone = this.contactRepository.findByPhoneContainingAndUser(query, user);
//		
//		// Dono list ko merge karo:
//		Set<Contact> uniqueContacts = new LinkedHashSet<>();
//		uniqueContacts.addAll(contacts);
//		uniqueContacts.addAll(byPhone);
//
//		
//		model.addAttribute("contacts", uniqueContacts);
//		
//		model.addAttribute("currentPage", 0);
//		
//		model.addAttribute("totalPages", 1);
//		
//		return "normal/show_contact";
//	}
//	
	
	// showing perticular contact details

	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		System.out.println("CID = " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		String userName = principal.getName();

		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			model.addAttribute("contact", contact);

			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";

	}

	// delete contact handler

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

//		System.out.println("CID = " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);

		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {

			user.getContacts().remove(contact); // hamne user me casecadetype ko all karke rakha hai isliye contact
												// delete nhi hoga isliye yaha pe isko use kar rhe hai

			this.userRepository.save(user);

			session.setAttribute("message", new Message("Contact deleted successfully...", "alert-success"));

			return "redirect:/user/show-contact/0";
		}

		return "redirect:/user/show-contact/0";

	}

	// Update form handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model) {

		model.addAttribute("title", "update contact");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/update_form";

	}

	// update contact form

	@PostMapping("/process-update")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Principal principal) {

		System.out.println("CONTACT NAME " + contact.getName());
		System.out.println("CONTACT ID " + contact.getCId());

		try {

			// old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getCId()).get();

			if (file.isEmpty()) {
				// agar file empty hai to purani photo ko rakhne ke liye

				contact.setImage(oldContactDetail.getImage());

			} else {

				// Delete old photo

				File deleteFile = new ClassPathResource("static/image").getFile();

				File file1 = new File(deleteFile, oldContactDetail.getImage());

				file1.delete();

				// update new photo

				File file2 = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			}

			User user = userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

		} catch (Exception e) {
			// TODO: handle exception
		}

		return "redirect:/user/" + contact.getCId() + "/contact";
	}

	// profile handler

	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {

		String userName = principal.getName();
		
		model.addAttribute("title",  userName + " - Smart Contact Manager");

		User user = userRepository.getUserByUserName(userName);

		model.addAttribute("title", user.getName());

		model.addAttribute("user", user);
		
		sessionHelper.removeMessageFromSession();

		return "normal/user_profile";
	}

	@PostMapping("/profile-update")
	public String profileUpdate(Principal principal, Model model) {

		String userName = principal.getName();

		User user = userRepository.getUserByUserName(userName);

		model.addAttribute("user", user);

		return "normal/update_profile";
	}
	
	
	// update profile process handler

	@PostMapping("/process-update-profile")
	public String processUpdateProfile(@ModelAttribute User user, @RequestParam("userImage") MultipartFile file,
			Model model) throws IOException {

		System.out.println("USERID = " + user.getId());

		System.out.println("image = " + user.getImageUrl());

//		User oldContactDetail = this.userRepository.findById(user.getId()).get();

		try {

			if (file.isEmpty()) {
				// agar file empty hai to purani photo ko rakhne ke liye

				user.setImageUrl(user.getImageUrl());

			} else {

				// delete old image

				if (!user.getImageUrl().equals("default.png")) {

					File deleteFile = new ClassPathResource("static/image").getFile();

					File file1 = new File(deleteFile, user.getImageUrl());

					file1.delete();
				}

				// update new photo

				File file2 = new ClassPathResource("static/image").getFile();

				Path path = Paths.get(file2.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				user.setImageUrl(file.getOriginalFilename());

			}

			userRepository.save(user);

		} catch (Exception e) {
			// TODO: handle exception

		}

		return "redirect:/user/profile";
	}
	
	
	// Open Settings handler

	@GetMapping("/setting")
	public String openSetting(Principal principal, Model model) {
		
		model.addAttribute("title", "Settings - Smart Contact Manager");
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);
		
		
		
		return "normal/settings";
		
	}
	
	// Change password handler
	
	@PostMapping("/change-process-password")
	public String changeProcessPassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword,
			Principal principal, HttpSession session) {
		
		// current user
		
		String userName = principal.getName();
		
		User user = this.userRepository.getUserByUserName(userName);  
		
		// Match old password with new password
		
		if(!this.passwordEncoder.matches(oldPassword, user.getPassword())) {
			
			 session.setAttribute("message", new Message("Old Password is Incorrect...", "alert-danger"));
		        return "redirect:/user/setting";
			
		}
		
		//  Check if new & confirm passwords match
		
		if(!newPassword.equals(confirmPassword)) {
			
			session.setAttribute("message", new Message("New password and confirm password do not match!", "alert-danger"));
			
			return "redirect:/user/setting"; 
			
		}
		
		// Update new password
		
		user.setPassword(passwordEncoder.encode(newPassword));
		
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Password changed successfully!", "alert-success"));
		
		return "redirect:/user/setting";
		
	}
	
	
	// creating order for payment
	
	@PostMapping("/create-order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) {
		
//		System.out.println("Order function executed");
		
		System.out.println(data);
		
		int amt = Integer.parseInt(data.get("amount").toString());
		
		try {
			
			RazorpayClient client = new RazorpayClient("rzp_test_fxip5V4ioepJdE", "DgsJqecK6dPSozc0rxMlzL42");
			
			JSONObject paymentRequest = new JSONObject();
			
			paymentRequest.put("amount", amt*100);
			paymentRequest.put("currency", "INR");
			paymentRequest.put("receipt", "txn_235425");
			
			// creating new order 
			
			Order order = client.orders.create(paymentRequest);
			
			// save the order in Database
			
			MyOrder myOrder = new MyOrder();
			
			myOrder.setOrderId(order.get("id"));
			myOrder.setAmount(order.get("amount") + "");
			myOrder.setReceipt(order.get("receipt"));
			myOrder.setPaymentId(null);
			myOrder.setStatus("created");
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			
			myOrder.setUser(user);
			
			this.myOrderRepository.save(myOrder);
			
			System.out.println(order);
			
			
			return order.toString();
			
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}
	
	@PostMapping("/update-order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		System.out.println(data);
		
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		myOrder.setPaymentId(data.get("payment_id").toString());
		
		myOrder.setStatus(data.get("status").toString());
		
		this.myOrderRepository.save(myOrder);
		
		return ResponseEntity.ok(Map.of("msg", "updated"));
		
	}
	
	
	
	

}
