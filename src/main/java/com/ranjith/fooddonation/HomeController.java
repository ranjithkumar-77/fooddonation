package com.ranjith.fooddonation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ranjith.fooddonation.model.Donation;
import com.ranjith.fooddonation.model.User;
import com.ranjith.fooddonation.repository.DonationRepository;
import com.ranjith.fooddonation.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;
    @GetMapping("/")
public String home() {
    return "home";
}

@GetMapping("/signup")
public String signupPage() {
    return "signup";
}

@PostMapping("/register")
public String register(@ModelAttribute User user) {

    if (user.getRole() == null || user.getRole().isEmpty()) {
        user.setRole("DONOR");
    }

    userRepository.save(user);

    return "login";
}

@GetMapping("/ngo-signup")
public String ngoSignupPage() {
    return "ngo-signup";
}

@PostMapping("/ngo-register")
public String ngoRegister(@ModelAttribute User user) {

    user.setRole("NGO");

    userRepository.save(user);

    return "login";
}

@GetMapping("/login")
public String loginPage() {
    return "login";
}

@PostMapping("/login")
public String login(@RequestParam String email,
                    @RequestParam String password,
                    Model model,
                    HttpSession session) {

    User user = userRepository.findByEmailAndPassword(email, password);

    if (user == null) {
        model.addAttribute("error", "Invalid Email or Password");
        return "login";
    }

    session.setAttribute("loggedInUser", user);
    model.addAttribute("name", user.getName());

    if ("NGO".equals(user.getRole())) {
        return "ngo-dashboard";
    }

    return "dashboard";
}
@GetMapping("/dashboard")
public String dashboard(HttpSession session, Model model) {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        return "redirect:/login";
    }

    model.addAttribute("name", user.getName());

    return "dashboard";
}

@GetMapping("/donate")
public String donatePage(HttpSession session) {

    if (session.getAttribute("loggedInUser") == null) {
        return "redirect:/login";
    }

    return "donate";
}

@PostMapping("/donate")
public String saveDonation(@ModelAttribute Donation donation,
                           HttpSession session) {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        return "redirect:/login";
    }

    donation.setDonorName(user.getName());

    if (donation.getStatus() == null || donation.getStatus().isEmpty()) {
        donation.setStatus("Pending");
    }

    donationRepository.save(donation);

    return "redirect:/mydonations";
}

@GetMapping("/mydonations")
public String myDonations(HttpSession session, Model model) {

    User user = (User) session.getAttribute("loggedInUser");

    if (user == null) {
        return "redirect:/login";
    }

    model.addAttribute("donations",
            donationRepository.findByDonorName(user.getName()));

    return "mydonations";
}

@GetMapping("/map")
public String mapPage() {
    return "map";
}
@GetMapping("/ngo-dashboard")
public String ngoDashboard(Model model) {

    model.addAttribute("totalDonations", donationRepository.count());

    model.addAttribute("pendingDonations",
            donationRepository.countByStatus("Pending"));

    model.addAttribute("acceptedDonations",
            donationRepository.countByStatus("Accepted"));

    model.addAttribute("pickupDonations",
            donationRepository.countByStatus("Pickup Scheduled"));

    model.addAttribute("completedDonations",
            donationRepository.countByStatus("Completed"));

    return "ngo-dashboard";
}

@GetMapping("/pending-donations")
public String pendingDonations(Model model) {

    model.addAttribute("donations", donationRepository.findAll());

    return "pending-donations";
}

@GetMapping("/view-location/{id}")
public String viewLocation(@PathVariable Long id, Model model) {

    Donation donation = donationRepository.findById(id).orElse(null);

    if (donation == null) {
        return "redirect:/pending-donations";
    }

    model.addAttribute("donation", donation);

    return "view-location";
}

@GetMapping("/accept/{id}")
public String acceptDonation(@PathVariable Long id) {

    Donation donation = donationRepository.findById(id).orElse(null);

    if (donation != null) {

        donation.setStatus("Accepted");

        donationRepository.save(donation);
    }

    return "redirect:/pending-donations";
}

@GetMapping("/schedule/{id}")
public String schedulePickupPage(@PathVariable Long id,
                                 Model model) {

    Donation donation = donationRepository.findById(id).orElse(null);

    model.addAttribute("donation", donation);

    return "schedule-pickup";
}

@PostMapping("/schedule-pickup")
public String savePickup(@RequestParam Long id,
                         @RequestParam String pickupDate,
                         @RequestParam String pickupTime) {

    Donation donation = donationRepository.findById(id).orElse(null);

    if (donation != null) {

        donation.setPickupDate(pickupDate);
        donation.setPickupTime(pickupTime);
        donation.setStatus("Pickup Scheduled");

        donationRepository.save(donation);
    }

    return "redirect:/pending-donations";
}

@GetMapping("/complete/{id}")
public String completeDonation(@PathVariable Long id) {

    Donation donation = donationRepository.findById(id).orElse(null);

    if (donation != null) {

        donation.setStatus("Completed");

        donationRepository.save(donation);
    }

    return "redirect:/pending-donations";
}

@GetMapping("/history")
public String history(Model model) {

    model.addAttribute("donations",
            donationRepository.findByStatus("Completed"));

    return "history";
}
}