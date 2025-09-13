package com.petcare.portal.tools;

import com.petcare.portal.services.AdoptionListingsService;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class Tools {

	@Autowired
	private AdoptionListingsService adoptionListingsService;

	// ===== ADOPTION MANAGEMENT TOOLS =====

	/**
	 * Search for pets available for adoption by criteria
	 *
	 * @param species Pet species (dog, cat, bird, etc.)
	 * @param breedId ID of specific breed
	 * @param gender  Gender (MALE, FEMALE)
	 * @param minAge  Minimum age
	 * @param maxAge  Maximum age
	 * @param page    Page number (starting from 0)
	 * @param size    Number of items per page
	 * @return List of pets matching search criteria
	 */
	@Tool(name = "searchAdoptionListings", description = """
			Search for pets available for adoption based on user criteria.

			VIETNAMESE → ENGLISH MAPPING:
			• Gender: 'đực/cái' → 'MALE/FEMALE'
			• Species: 'chó/mèo/chim' → 'DOG/CAT/BIRD'
			• Example: 'chó cái' → species='DOG', gender='FEMALE'

			FILTER CRITERIA:
			• species: DOG, CAT, BIRD (required if filtering by species)
			• gender: MALE, FEMALE (required if filtering by gender)
			• breedId: If zero then set to null (no breed filtering)
			• minAge, maxAge: age range (optional, if not provided no age filtering, use 0-100 range)

			RULES:
			• Only return pets with adoptionStatus = PENDING
			• Page: from 0 onwards, Size: 1-20 items (auto-adjust if invalid)
			• DO NOT add criteria that user didn't mention
			• ALWAYS provide page and size (page >= 0, size 1-20)
			• AI will call this tool MAXIMUM 1 TIME per request
			""")
	public Page<AdoptionListingsResponse> searchAdoptionListings(String species, Long breedId, String gender,
			Integer minAge, Integer maxAge, int page, int size) {

		try {
			PageRequest pageRequest = createValidPageRequest(page, size);

			Page<AdoptionListingsResponse> results = adoptionListingsService.getAllAdoptionListings(pageRequest,
					species, null, gender, minAge, maxAge);

			return results;

		} catch (Exception e) {
			throw new RuntimeException("Error searching for adoptable pets: " + e.getMessage());
		}
	}

	/**
	 * Get detailed information for a specific pet available for adoption
	 *
	 * @param listingId ID of adoption listing
	 * @return Detailed pet information
	 */
	@Tool(name = "getAdoptionListingDetails", description = """
			Get DETAILED information for a specific pet by ID.
			Returns complete info: name, species, breed, age, gender (MALE/FEMALE), description, images.
			Only returns pets with adoptionStatus = PENDING (waiting for adoption).
			""")
	public AdoptionListingsResponse getAdoptionListingDetails(Long listingId) {
		try {
			AdoptionListingsResponse response = adoptionListingsService.getAdoptionListingById(listingId);

			if (response == null) {
				throw new IllegalArgumentException("Pet not found with ID: " + listingId);
			}

			return response;

		} catch (Exception e) {
			throw new RuntimeException("Error retrieving pet information: " + e.getMessage());
		}
	}





	/**
	 * Find pets matching adopter preferences
	 *
	 * @param preferredSpecies Preferred species
	 * @param maxAge           Maximum acceptable age
	 * @param preferredGender  Preferred gender
	 * @param page             Page number
	 * @param size             Items per page
	 * @return List of matching pets
	 */
	@Tool(name = "findMatchingPets", description = """
			SUGGEST pets matching user PREFERENCES.

			PARAMETERS:
			• preferredSpecies: DOG/CAT/BIRD (REQUIRED - from 'chó/mèo/chim')
			• maxAge: maximum age (optional - from 'under X years')
			• preferredGender: MALE/FEMALE (optional - from 'đực/cái')

			WHEN TO USE:
			• User expresses preference: 'I like dogs', 'want female cat under 2 years'
			• Need to suggest pets matching preference

			NOTES:
			• ALWAYS provide page and size (page >= 0, size 1-20)
			• preferredSpecies is REQUIRED

			RESULT: List of PENDING pets matching preferences.
			""")
	public Page<AdoptionListingsResponse> findMatchingPets(String preferredSpecies, Integer maxAge,
			String preferredGender, int page, int size) {

		try {
			PageRequest pageRequest = createValidPageRequest(page, size);

			// Use flexible criteria - species is required, others are optional
			return adoptionListingsService.getAllAdoptionListings(pageRequest, preferredSpecies, null,
					preferredGender != null ? preferredGender.toUpperCase() : null, null, maxAge);

		} catch (Exception e) {
			throw new RuntimeException("Error finding matching pets: " + e.getMessage());
		}
	}


	/**
	 * Get adoption listings statistics
	 *
	 * @return Overview statistics
	 */
	@Tool(name = "getAdoptionStatistics", description = """
			Get OVERVIEW STATISTICS of pets available for adoption.

			RETURNS:
			• totalListings: Total pets waiting for adoption
			• dogCount: Number of dogs
			• catCount: Number of cats
			• birdCount: Number of birds
			• otherCount: Number of other species

			USE WHEN:
			• User asks: 'how many dogs are available for adoption?'
			• Need overview of available pets
			• Only count pets with adoptionStatus = PENDING
			""")
	public AdoptionStatistics getAdoptionStatistics() {
		try {
			// Count by species using direct queries
			Page<AdoptionListingsResponse> dogResults = adoptionListingsService
					.getAllAdoptionListings(PageRequest.of(0, 1), "DOG", null, null, null, null);
			long dogCount = dogResults.getTotalElements();

			Page<AdoptionListingsResponse> catResults = adoptionListingsService
					.getAllAdoptionListings(PageRequest.of(0, 1), "CAT", null, null, null, null);
			long catCount = catResults.getTotalElements();

			Page<AdoptionListingsResponse> birdResults = adoptionListingsService
					.getAllAdoptionListings(PageRequest.of(0, 1), "BIRD", null, null, null, null);
			long birdCount = birdResults.getTotalElements();

			// Get total count
			Page<AdoptionListingsResponse> allResults = adoptionListingsService
					.getAllAdoptionListings(PageRequest.of(0, 1), null, null, null, null, null);
			long totalCount = allResults.getTotalElements();

			AdoptionStatistics stats = new AdoptionStatistics();
			stats.setTotalListings(totalCount);
			stats.setDogCount(dogCount);
			stats.setCatCount(catCount);
			stats.setBirdCount(birdCount);
			stats.setOtherCount(totalCount - dogCount - catCount - birdCount);

			return stats;

		} catch (Exception e) {
			throw new RuntimeException("Error retrieving statistics: " + e.getMessage());
		}
	}

	/**
	 * Class to contain adoption statistics
	 */
	public static class AdoptionStatistics {
		private long totalListings;
		private long dogCount;
		private long catCount;
		private long birdCount;
		private long otherCount;

		// Getters and setters
		public long getTotalListings() {
			return totalListings;
		}

		public void setTotalListings(long totalListings) {
			this.totalListings = totalListings;
		}

		public long getDogCount() {
			return dogCount;
		}

		public void setDogCount(long dogCount) {
			this.dogCount = dogCount;
		}

		public long getCatCount() {
			return catCount;
		}

		public void setCatCount(long catCount) {
			this.catCount = catCount;
		}

		public long getBirdCount() {
			return birdCount;
		}

		public void setBirdCount(long birdCount) {
			this.birdCount = birdCount;
		}

		public long getOtherCount() {
			return otherCount;
		}

		public void setOtherCount(long otherCount) {
			this.otherCount = otherCount;
		}
	}

	/**
	 * Helper method to validate and set default pagination parameters
	 * @param page Raw page number from AI
	 * @param size Raw size from AI
	 * @return Valid PageRequest
	 */
	private PageRequest createValidPageRequest(int page, int size) {
		int validPage = Math.max(0, page); // Page must be >= 0
		int validSize = Math.max(1, Math.min(size, 20)); // Size must be 1-20

		// Log validation if parameters were adjusted
		if (validPage != page || validSize != size) {
			System.out.println("DEBUG: Pagination validation - Original(page: " + page + ", size: " + size +
				") -> Valid(page: " + validPage + ", size: " + validSize + ")");
		}

		return PageRequest.of(validPage, validSize);
	}

	/**
	 * Clean emoji and problematic Unicode characters from text
	 * to avoid database encoding errors with MySQL utf8 (not utf8mb4)
	 * @param text Text to clean
	 * @return Text with emoji cleaned
	 */
	public String cleanEmojiAndUnicode(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		try {
			// Remove emoji and 4-byte Unicode characters (UTF8MB4)
			// This pattern removes most emoji but keeps Vietnamese letters
			String cleaned = text
				// Remove emoji ranges - main emoji blocks
				.replaceAll("[\\x{1F600}-\\x{1F64F}]", "") // Emoticons
				.replaceAll("[\\x{1F300}-\\x{1F5FF}]", "") // Misc Symbols and Pictographs
				.replaceAll("[\\x{1F680}-\\x{1F6FF}]", "") // Transport and Map
				.replaceAll("[\\x{1F1E0}-\\x{1F1FF}]", "") // Regional indicator symbol
				.replaceAll("[\\x{2600}-\\x{26FF}]", "")   // Misc symbols
				.replaceAll("[\\x{2700}-\\x{27BF}]", "")   // Dingbats
				.replaceAll("[\\x{1f926}-\\x{1f937}]", "")  // Gestures
				.replaceAll("[\\x{10000}-\\x{10ffff}]", "") // Supplementary planes

				// Remove zero width joiner and variation selectors (important parts of emoji)
				.replaceAll("\\u200d", "") // Zero width joiner
				.replaceAll("\\uFE0F", "") // Variation selector-16
				.replaceAll("\\uFE0E", "") // Variation selector-15

				// Remove other problematic Unicode characters
				.replaceAll("\\u2640", "") // Female sign
				.replaceAll("\\u2642", "") // Male sign
				.replaceAll("\\u2695", "") // Medical symbol
				.replaceAll("\\u2696", "") // Scales symbol
				.replaceAll("\\u26A0", "") // Warning symbol

				// Remove mathematical symbols and other special chars
				.replaceAll("[\\x{2190}-\\x{21FF}]", "") // Arrows
				.replaceAll("[\\x{2300}-\\x{23FF}]", "") // Misc Technical
				.replaceAll("[\\x{2B00}-\\x{2BFF}]", "") // Misc symbols and arrows

				// Remove enclosing and combining characters
				.replaceAll("[\\x{20D0}-\\x{20FF}]", "") // Combining Diacritical Marks for Symbols
				.replaceAll("[\\x{1F900}-\\x{1F9FF}]", "") // Supplemental Symbols and Pictographs

				.trim();

			// Additional cleanup - remove multiple spaces
			cleaned = cleaned.replaceAll("\\s+", " ");

			// Remove control characters but keep newlines and tabs
			cleaned = cleaned.replaceAll("[\\x{0000}-\\x{0008}]", "")
						   .replaceAll("[\\x{000B}-\\x{000C}]", "")
						   .replaceAll("[\\x{000E}-\\x{001F}]", "")
						   .replaceAll("[\\x{007F}-\\x{009F}]", "");

			return cleaned;

		} catch (Exception e) {
			// Return original text if cleaning fails
			return text;
		}
	}

}