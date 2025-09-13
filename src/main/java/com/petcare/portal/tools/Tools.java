package com.petcare.portal.tools;

import com.petcare.portal.services.AdoptionListingsService;
import com.petcare.portal.dtos.AdoptionListingsDto.AdoptionListingsResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * PetCare Tools - Function Calling Interface cho Adoption Listings
 *
 * Cung cấp các công cụ tối ưu để AI tìm kiếm thú cưng cần nhận nuôi
 * thông qua function calling mechanism.
 *
 * Chức năng chính:
 * - Tìm kiếm thú cưng theo tiêu chí (species, gender, age, breed)
 * - Lấy thông tin chi tiết thú cưng cụ thể
 * - Tìm thú cưng phù hợp với sở thích người dùng
 * - Lấy thống kê tổng quan về thú cưng đang chờ nhận nuôi
 *
 * Cách sử dụng với Spring AI Function Calling:
 *
 * 1. Register tools: chatClient.prompt() .tools(tools) // Inject Tools bean
 * .entity(new ParameterizedTypeReference<AdoptionListingsAiResponse>() {})
 * .call() .entity();
 *
 * 2. AI sẽ tự động gọi methods trong Tools khi detect adoption queries
 * 3. Response được format thành structured data với message và list của AdoptionListingsResponse
 */
@Component
public class Tools {

	@Autowired
	private AdoptionListingsService adoptionListingsService;

	// ===== ADOPTION MANAGEMENT TOOLS =====

	/**
	 * Tìm kiếm thú cưng cần nhận nuôi theo tiêu chí
	 *
	 * @param species Loài thú cưng (chó, mèo, chim, v.v.)
	 * @param breedId ID của giống cụ thể
	 * @param gender  Giới tính (MALE, FEMALE)
	 * @param minAge  Tuổi tối thiểu
	 * @param maxAge  Tuổi tối đa
	 * @param page    Trang (bắt đầu từ 0)
	 * @param size    Số items mỗi trang
	 * @return Danh sách thú cưng phù hợp với tiêu chí tìm kiếm
	 */
	@Tool(name = "searchAdoptionListings", description = """
			Tìm kiếm thú cưng cần nhận nuôi theo tiêu chí người dùng muốn.

			CÁCH MAPPING TIẾNG VIỆT → ENGLISH:
			• Giới tính: 'đực/cái' → 'MALE/FEMALE'
			• Loài: 'chó/mèo/chim' → 'DOG/CAT/BIRD'
			• Ví dụ: 'chó cái' → species='DOG', gender='FEMALE'

			TIÊU CHÍ LỌC:
			• species: DOG, CAT, BIRD (bắt buộc nếu muốn lọc theo loài)
			• gender: MALE, FEMALE (bắt buộc nếu muốn lọc theo giới tính)
			• breedId: Nếu bằng không thì để null (không lọc theo giống)
			• minAge, maxAge: khoảng tuổi (tùy chọn nếu không có thì không lọc theo tuổi và để khoảng 0-100)

			QUY TẮC:
			• Chỉ trả về thú cưng có adoptionStatus = PENDING
			• Page: từ 0 trở lên, Size: 1-20 items (tự động điều chỉnh nếu không hợp lệ)
			• KHÔNG thêm tiêu chí nào mà user không đề cập
			• LUÔN cung cấp page và size (page >= 0, size 1-20)
			• AI sẽ gọi tool này TỐI ĐA 1 LẦN per request
			""")
	public Page<AdoptionListingsResponse> searchAdoptionListings(String species, Long breedId, String gender,
			Integer minAge, Integer maxAge, int page, int size) {

		try {
			PageRequest pageRequest = createValidPageRequest(page, size);

			Page<AdoptionListingsResponse> results = adoptionListingsService.getAllAdoptionListings(pageRequest,
					species, null, gender, minAge, maxAge);

			return results;

		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi tìm kiếm thú cưng nhận nuôi: " + e.getMessage());
		}
	}

	/**
	 * Lấy thông tin chi tiết thú cưng cần nhận nuôi
	 *
	 * @param listingId ID của adoption listing
	 * @return Chi tiết thông tin thú cưng
	 */
	@Tool(name = "getAdoptionListingDetails", description = """
			Lấy thông tin CHI TIẾT của một thú cưng cụ thể theo ID.
			Trả về đầy đủ: tên, loài, giống, tuổi, giới tính (MALE/FEMALE), mô tả, hình ảnh.
			Chỉ trả về thú cưng có adoptionStatus = PENDING (đang chờ nhận nuôi).
			""")
	public AdoptionListingsResponse getAdoptionListingDetails(Long listingId) {
		try {
			AdoptionListingsResponse response = adoptionListingsService.getAdoptionListingById(listingId);

			if (response == null) {
				throw new IllegalArgumentException("Không tìm thấy thú cưng với ID: " + listingId);
			}

			return response;

		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi lấy thông tin thú cưng: " + e.getMessage());
		}
	}





	/**
	 * Tìm thú cưng phù hợp cho người nhận nuôi
	 *
	 * @param preferredSpecies Loài ưa thích
	 * @param maxAge           Tuổi tối đa chấp nhận
	 * @param preferredGender  Giới tính ưa thích
	 * @param page             Trang
	 * @param size             Số items mỗi trang
	 * @return Danh sách thú cưng phù hợp
	 */
	@Tool(name = "findMatchingPets", description = """
			ĐỀ XUẤT thú cưng phù hợp với SỞ THÍCH người dùng.

			THAM SỐ:
			• preferredSpecies: DOG/CAT/BIRD (BẮT BUỘC - từ 'chó/mèo/chim')
			• maxAge: tuổi tối đa (tùy chọn - từ 'dưới X tuổi')
			• preferredGender: MALE/FEMALE (tùy chọn - từ 'đực/cái')

			KHI NÀO SỬ DỤNG:
			• User nói sở thích: 'tôi thích chó', 'muốn mèo cái dưới 2 tuổi'
			• Cần gợi ý pet phù hợp với preference

			LƯU Ý:
			• LUÔN cung cấp page và size (page >= 0, size 1-20)
			• preferredSpecies là BẮT BUỘC

			KẾT QUẢ: Danh sách pet PENDING match với sở thích.
			""")
	public Page<AdoptionListingsResponse> findMatchingPets(String preferredSpecies, Integer maxAge,
			String preferredGender, int page, int size) {

		try {
			PageRequest pageRequest = createValidPageRequest(page, size);

			// Use flexible criteria - species is required, others are optional
			return adoptionListingsService.getAllAdoptionListings(pageRequest, preferredSpecies, null,
					preferredGender != null ? preferredGender.toUpperCase() : null, null, maxAge);

		} catch (Exception e) {
			throw new RuntimeException("Lỗi khi tìm thú cưng phù hợp: " + e.getMessage());
		}
	}


	/**
	 * Lấy thống kê adoption listings
	 *
	 * @return Thống kê tổng quan
	 */
	@Tool(name = "getAdoptionStatistics", description = """
			Lấy THỐNG KÊ tổng quan về thú cưng đang cần nhận nuôi.

			TRẢ VỀ:
			• totalListings: Tổng số thú cưng chờ nhận nuôi
			• dogCount: Số lượng chó
			• catCount: Số lượng mèo
			• birdCount: Số lượng chim
			• otherCount: Số lượng loài khác

			SỬ DỤNG KHI:
			• User hỏi: 'có bao nhiêu con chó đang cần nhận nuôi?'
			• Cần biết tổng quan về available pets
			• Chỉ đếm thú cưng có adoptionStatus = PENDING
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
			throw new RuntimeException("Lỗi khi lấy thống kê: " + e.getMessage());
		}
	}

	/**
	 * Class để chứa thống kê adoption
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
	 * Helper method để validate và set default pagination parameters
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
	 * Clean emoji và ký tự Unicode gây vấn đề khỏi text
	 * để tránh lỗi database encoding MySQL với utf8 (không phải utf8mb4)
	 * @param text Text cần clean
	 * @return Text đã được clean emoji
	 */
	public String cleanEmojiAndUnicode(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		try {
			// Remove emoji và các ký tự Unicode 4-byte (UTF8MB4)
			// Pattern này remove hầu hết emoji nhưng giữ lại chữ cái tiếng Việt
			String cleaned = text
				// Remove emoji ranges - các block chính của emoji
				.replaceAll("[\\x{1F600}-\\x{1F64F}]", "") // Emoticons
				.replaceAll("[\\x{1F300}-\\x{1F5FF}]", "") // Misc Symbols and Pictographs
				.replaceAll("[\\x{1F680}-\\x{1F6FF}]", "") // Transport and Map
				.replaceAll("[\\x{1F1E0}-\\x{1F1FF}]", "") // Regional indicator symbol
				.replaceAll("[\\x{2600}-\\x{26FF}]", "")   // Misc symbols
				.replaceAll("[\\x{2700}-\\x{27BF}]", "")   // Dingbats
				.replaceAll("[\\x{1f926}-\\x{1f937}]", "")  // Gestures
				.replaceAll("[\\x{10000}-\\x{10ffff}]", "") // Supplementary planes

				// Remove zero width joiner và variation selectors (phần quan trọng của emoji)
				.replaceAll("\\u200d", "") // Zero width joiner
				.replaceAll("\\uFE0F", "") // Variation selector-16
				.replaceAll("\\uFE0E", "") // Variation selector-15

				// Remove other problematic Unicode characters
				.replaceAll("\\u2640", "") // Female sign
				.replaceAll("\\u2642", "") // Male sign
				.replaceAll("\\u2695", "") // Medical symbol
				.replaceAll("\\u2696", "") // Scales symbol
				.replaceAll("\\u26A0", "") // Warning symbol

				// Remove mathematical symbols và other special chars
				.replaceAll("[\\x{2190}-\\x{21FF}]", "") // Arrows
				.replaceAll("[\\x{2300}-\\x{23FF}]", "") // Misc Technical
				.replaceAll("[\\x{2B00}-\\x{2BFF}]", "") // Misc symbols and arrows

				// Remove enclosing và combining characters
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