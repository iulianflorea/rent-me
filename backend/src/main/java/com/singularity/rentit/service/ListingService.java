package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.CreateListingRequest;
import com.singularity.rentit.dto.request.UpdateListingRequest;
import com.singularity.rentit.dto.response.ListingResponse;
import com.singularity.rentit.dto.response.ListingSummaryResponse;
import com.singularity.rentit.entity.Listing;
import com.singularity.rentit.entity.ListingImage;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.CategoryType;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.ListingStatus;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.KycNotVerifiedException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.exception.UnauthorizedException;
import com.singularity.rentit.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListingService {

    private final ListingRepository listingRepository;
    private final StorageService storageService;
    private final ReviewService reviewService;
    private final GeoService geoService;

    @Transactional
    public ListingResponse createListing(CreateListingRequest request, User owner) {
        if (owner.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException();
        }

        Listing listing = Listing.builder()
                .owner(owner)
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .status(ListingStatus.DRAFT)
                .pricePerDay(request.pricePerDay())
                .pricePerWeek(request.pricePerWeek())
                .pricePerMonth(request.pricePerMonth())
                .address(request.address())
                .city(request.city())
                .county(request.county())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .categoryAttributes(request.categoryAttributes())
                .build();

        Listing saved = listingRepository.save(listing);
        log.info("Listing created: {} by user {}", saved.getId(), owner.getId());
        return toResponse(saved, null);
    }

    @Transactional
    public ListingResponse addImages(Long listingId, List<MultipartFile> files, User owner) {
        Listing listing = getAndCheckOwner(listingId, owner.getId());

        if (listing.getImages().size() + files.size() > 10) {
            throw new BusinessException("Maximum 10 images allowed per listing", HttpStatus.BAD_REQUEST);
        }

        int order = listing.getImages().size();
        for (MultipartFile file : files) {
            String url = storageService.uploadFile(file, "listings/" + listingId);
            ListingImage image = ListingImage.builder()
                    .listing(listing)
                    .url(url)
                    .displayOrder(order++)
                    .build();
            listing.getImages().add(image);
        }

        Listing saved = listingRepository.saveAndFlush(listing);
        return toResponse(saved, null);
    }

    @Transactional
    public ListingResponse updateListing(Long listingId, UpdateListingRequest request, User owner) {
        Listing listing = getAndCheckOwner(listingId, owner.getId());

        if (request.title() != null) listing.setTitle(request.title());
        if (request.description() != null) listing.setDescription(request.description());
        if (request.pricePerDay() != null) listing.setPricePerDay(request.pricePerDay());
        if (request.pricePerWeek() != null) listing.setPricePerWeek(request.pricePerWeek());
        if (request.pricePerMonth() != null) listing.setPricePerMonth(request.pricePerMonth());
        if (request.address() != null) listing.setAddress(request.address());
        if (request.city() != null) listing.setCity(request.city());
        if (request.county() != null) listing.setCounty(request.county());
        if (request.latitude() != null) listing.setLatitude(request.latitude());
        if (request.longitude() != null) listing.setLongitude(request.longitude());
        if (request.categoryAttributes() != null) listing.setCategoryAttributes(request.categoryAttributes());
        if (request.status() != null) listing.setStatus(request.status());

        Listing saved = listingRepository.save(listing);
        log.info("Listing updated: {}", listingId);
        return toResponse(saved, null);
    }

    @Transactional
    public void deleteListing(Long listingId, User owner) {
        Listing listing = getAndCheckOwner(listingId, owner.getId());
        listing.setStatus(ListingStatus.INACTIVE);
        listingRepository.save(listing);
        log.info("Listing deactivated: {}", listingId);
    }

    @Transactional
    public ListingResponse publishListing(Long listingId, User owner) {
        Listing listing = getAndCheckOwner(listingId, owner.getId());

        if (listing.getImages().isEmpty()) {
            throw new BusinessException("At least one image is required to publish", HttpStatus.BAD_REQUEST);
        }

        listing.setStatus(ListingStatus.ACTIVE);
        Listing saved = listingRepository.save(listing);
        log.info("Listing published: {}", listingId);
        return toResponse(saved, null);
    }

    @Transactional
    public ListingResponse getListing(Long listingId, Double userLat, Double userLon) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        listing.setViewsCount(listing.getViewsCount() + 1);
        listingRepository.save(listing);

        Double distance = null;
        if (userLat != null && userLon != null && listing.getLatitude() != null) {
            distance = geoService.haversineDistance(userLat, userLon, listing.getLatitude(), listing.getLongitude());
        }

        return toResponse(listing, distance);
    }

    @Transactional(readOnly = true)
    public Page<ListingSummaryResponse> searchListings(
            CategoryType category, BigDecimal minPrice, BigDecimal maxPrice,
            String city, String county, String search,
            LocalDate startDate, LocalDate endDate,
            Double userLat, Double userLon,
            Pageable pageable
    ) {
        LocalDate from = startDate != null ? startDate : LocalDate.now().minusDays(1);
        LocalDate to = endDate != null ? endDate : LocalDate.now().plusYears(10);

        return listingRepository.searchListings(
                category, minPrice, maxPrice, city, county, search, from, to, pageable
        ).map(l -> toSummary(l, userLat, userLon));
    }

    @Transactional(readOnly = true)
    public Page<ListingSummaryResponse> getMyListings(User owner, Pageable pageable) {
        return listingRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId(), pageable)
                .map(l -> toSummary(l, null, null));
    }

    @Transactional(readOnly = true)
    public Page<ListingSummaryResponse> getNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        return listingRepository.findNearby(lat, lon, radiusKm, pageable)
                .map(l -> toSummary(l, lat, lon));
    }

    private Listing getAndCheckOwner(Long listingId, Long ownerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));
        if (!listing.getOwner().getId().equals(ownerId)) {
            throw new UnauthorizedException("Not authorized to modify this listing");
        }
        return listing;
    }

    private ListingResponse toResponse(Listing l, Double distance) {
        User owner = l.getOwner();
        List<ListingResponse.ImageDto> images = l.getImages().stream()
                .map(img -> new ListingResponse.ImageDto(img.getId(), img.getUrl(), img.getDisplayOrder()))
                .toList();
        String firstImage = images.isEmpty() ? null : images.get(0).url();

        BigDecimal guaranteeAmount = l.getCategory() == CategoryType.REAL_ESTATE
                ? BigDecimal.ZERO
                : l.getPricePerDay().multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);

        return new ListingResponse(
                l.getId(), l.getTitle(), l.getDescription(), l.getCategory(), l.getStatus(),
                l.getPricePerDay(), l.getPricePerWeek(), l.getPricePerMonth(),
                guaranteeAmount,
                l.getAddress(), l.getCity(), l.getCounty(), l.getLatitude(), l.getLongitude(),
                l.getCategoryAttributes(), images, firstImage,
                new ListingResponse.OwnerSummary(
                        owner.getId(), owner.getFirstName(), owner.getLastName(),
                        owner.getKycStatus().name(),
                        reviewService.getAverageRating(owner.getId()),
                        reviewService.getReviewCount(owner.getId())
                ),
                l.getViewsCount(), distance, false,
                l.getCreatedAt(), l.getUpdatedAt()
        );
    }

    public ListingSummaryResponse toSummaryPublic(Listing l) {
        return toSummary(l, null, null);
    }

    private ListingSummaryResponse toSummary(Listing l, Double userLat, Double userLon) {
        String firstImage = l.getImages().isEmpty() ? null : l.getImages().get(0).getUrl();
        Double distance = null;
        if (userLat != null && userLon != null && l.getLatitude() != null) {
            distance = geoService.haversineDistance(userLat, userLon, l.getLatitude(), l.getLongitude());
        }
        User owner = l.getOwner();

        return new ListingSummaryResponse(
                l.getId(), l.getTitle(), l.getCategory(), l.getStatus(), l.getPricePerDay(),
                l.getCity(), l.getCounty(), l.getLatitude(), l.getLongitude(), firstImage,
                new ListingSummaryResponse.OwnerSummary(
                        owner.getId(), owner.getFirstName(), owner.getLastName(),
                        owner.getKycStatus().name(),
                        reviewService.getAverageRating(owner.getId()),
                        reviewService.getReviewCount(owner.getId())
                ),
                distance
        );
    }
}
