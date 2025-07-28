package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.response.AttractionDTO;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;

import java.util.List;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;
    @Autowired
    private UserService userService;

    /**
     * A home page for TourGuide
     *
     * @return the message "Greetings from TourGuide!"
     */
    @GetMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    /**
     * A page to see location of a user
     *
     * @param userName
     * @return the method that get the Location of the user
     */
    @GetMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return userService.getUserLocation(getUser(userName));
    }

    /**
     * Get the 5 nearby attractions of the User
     *
     * @param userName
     * @return the methode that get the 5 nearbu attractions of the user
     */
    @GetMapping("/getNearbyAttractions")
    public List<AttractionDTO> getNearbyAttractions(@RequestParam String userName) {
    	return tourGuideService.getNearByAttractions(getUser(userName));
    }

    /**
     * Get all the rewards from a user
     *
     * @param userName
     * @return the methode that get all the rewards from a user
     */
    @GetMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return userService.getUserRewards(getUser(userName));
    }

    /**
     * Get the Trip deals for a user
     *
     * @param userName
     * @return the method that get the trip deals for a user
     */
    @GetMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }

    /**
     * Get the user with the UserName
     *
     * @param userName
     * @return the method that get the user with his UserName
     */
    private User getUser(String userName) {
    	return userService.getUser(userName);
    }
}