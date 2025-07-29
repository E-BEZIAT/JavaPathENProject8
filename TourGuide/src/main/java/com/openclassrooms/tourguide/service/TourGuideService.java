package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.response.AttractionDTO;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.openclassrooms.tourguide.service.UserService.tripPricerApiKey;

@Service
public class TourGuideService {
	private final UserService userService;
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;

	private final ExecutorService executor =
			Executors.newFixedThreadPool(100);

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			userService.initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this, userService);
		addShutDownHook();
		this.userService = userService;
	}

	/**
	 * Get a personal price for a trip
	 *
	 * @param user
	 * @return
	 */
	public List<Provider> getTripDeals(User user) {
		List<UserReward> userRewards = user.getUserRewards();
		int cumulatativeRewardPoints = userRewards.stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(
				tripPricerApiKey,
				user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(),
				cumulatativeRewardPoints
		);
		if (providers == null) {
			providers = Collections.emptyList();
		}
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Get the location of a user
	 *
	 * @param user
	 * @return
	 */
	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	/**
	 * Get the location of multiple users
	 *
	 * @param users
	 * @return
	 */
	public List<VisitedLocation> listTrackUsersLocation(List<User> users) {
		List<CompletableFuture<VisitedLocation>> futures = users.stream()
				.map(user -> CompletableFuture.supplyAsync(() -> trackUserLocation(user), executor))
				.toList();

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		return futures.stream()
				.map(CompletableFuture::join)
				.collect(Collectors.toList());
	}

	/**
	 * Get the 5 nearest attractions from a user
	 *
	 * @param user
	 * @return
	 */
	public List<AttractionDTO> getNearByAttractions(User user) {
		Location userLocation = userService.getUserLocation(user).location;
		List<Attraction> allAttractions = gpsUtil.getAttractions();
		 return allAttractions.stream()
				 .map(attraction -> {
					 Location attractionLocation = new Location(attraction.latitude, attraction.longitude);
					 double distance = rewardsService.getDistance(attractionLocation, userLocation);
					 int rewardPoints = rewardsService.getRewardPoints(attraction, user);

					 return new AttractionDTO(
							 attraction.attractionName,
                             attractionLocation,
                             userLocation,
							 distance,
							 rewardPoints
					 );
				 })
				 .sorted(Comparator.comparingDouble(AttractionDTO::getDistance))
				 .limit(5)
				 .collect(Collectors.toList());
	}

	/**
	 * Stop tracking
	 *
	 */
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}
}
