package com.singularity.rentit.service;

import com.singularity.rentit.dto.response.WishlistResponse;
import com.singularity.rentit.entity.Listing;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.entity.WishlistItem;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.ListingRepository;
import com.singularity.rentit.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    @Transactional
    public void addToWishlist(Long userId, Long listingId) {
        if (wishlistRepository.existsByUserIdAndListingId(userId, listingId)) {
            throw new BusinessException("Already in wishlist", HttpStatus.CONFLICT);
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing", listingId));

        User user = new User();
        user.setId(userId);

        WishlistItem item = WishlistItem.builder()
                .user(user)
                .listing(listing)
                .build();

        wishlistRepository.save(item);
        log.info("Listing {} added to wishlist for user {}", listingId, userId);
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long listingId) {
        wishlistRepository.deleteByUserIdAndListingId(userId, listingId);
        log.info("Listing {} removed from wishlist for user {}", listingId, userId);
    }

    @Transactional(readOnly = true)
    public Page<WishlistResponse> getWishlist(Long userId, Pageable pageable) {
        return wishlistRepository.findByUserId(userId, pageable)
                .map(item -> new WishlistResponse(
                        item.getId(),
                        listingService.toSummaryPublic(item.getListing()),
                        item.getCreatedAt()
                ));
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long listingId) {
        return wishlistRepository.existsByUserIdAndListingId(userId, listingId);
    }
}
