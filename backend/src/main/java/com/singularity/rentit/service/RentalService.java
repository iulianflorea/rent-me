package com.singularity.rentit.service;

import com.singularity.rentit.dto.request.CreateRentalRequest;
import com.singularity.rentit.dto.response.QrCodeResponse;
import com.singularity.rentit.dto.response.RentalResponse;
import com.singularity.rentit.entity.ChatRoom;
import com.singularity.rentit.entity.Listing;
import com.singularity.rentit.entity.Rental;
import com.singularity.rentit.entity.User;
import com.singularity.rentit.enums.KycStatus;
import com.singularity.rentit.enums.ListingStatus;
import com.singularity.rentit.enums.RentalStatus;
import com.singularity.rentit.exception.BusinessException;
import com.singularity.rentit.exception.KycNotVerifiedException;
import com.singularity.rentit.exception.ListingUnavailableException;
import com.singularity.rentit.exception.ResourceNotFoundException;
import com.singularity.rentit.repository.ChatRoomRepository;
import com.singularity.rentit.repository.ListingRepository;
import com.singularity.rentit.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalService {

    private final RentalRepository rentalRepository;
    private final ListingRepository listingRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PaymentService paymentService;
    private final QrCodeService qrCodeService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Transactional
    public RentalResponse createRental(CreateRentalRequest request, User tenant) {
        if (tenant.getKycStatus() != KycStatus.VERIFIED) {
            throw new KycNotVerifiedException();
        }

        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing", request.listingId()));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new ListingUnavailableException();
        }

        if (listing.getOwner().getId().equals(tenant.getId())) {
            throw new BusinessException("Cannot rent your own listing", HttpStatus.BAD_REQUEST);
        }

        validateDates(request.startDate(), request.endDate());
        checkAvailability(listing.getId(), request.startDate(), request.endDate());

        int days = (int) ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        BigDecimal pricePerDay = listing.getPricePerDay();
        BigDecimal subtotal = pricePerDay.multiply(BigDecimal.valueOf(days));
        BigDecimal guarantee = PaymentService.shouldApplyGuarantee(listing.getCategory())
                ? subtotal.multiply(new BigDecimal("0.50")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(guarantee);

        String refNumber = "RNT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 9).toUpperCase();

        Rental rental = Rental.builder()
                .listing(listing)
                .tenant(tenant)
                .owner(listing.getOwner())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalDays(days)
                .pricePerDay(pricePerDay)
                .subtotal(subtotal)
                .guaranteeAmount(guarantee)
                .totalAmount(total)
                .status(RentalStatus.PENDING_PAYMENT)
                .referenceNumber(refNumber)
                .build();

        Rental saved = rentalRepository.save(rental);
        log.info("Rental created: {} for listing {}", saved.getId(), listing.getId());
        return toResponse(saved);
    }

    @Transactional
    public RentalResponse markReadyToPickup(Long rentalId, User owner) {
        Rental rental = getRentalAndCheckOwner(rentalId, owner.getId());

        if (rental.getStatus() != RentalStatus.PAID) {
            throw new BusinessException("Rental must be in PAID status", HttpStatus.BAD_REQUEST);
        }

        rental.setStatus(RentalStatus.READY_TO_PICKUP);
        String qrToken = qrCodeService.generateToken();
        rental.setQrCodeToken(qrToken);
        rentalRepository.save(rental);

        chatRoomRepository.findByRentalId(rentalId).ifPresentOrElse(
                room -> {},
                () -> {
                    ChatRoom room = ChatRoom.builder()
                            .rental(rental)
                            .participant1(rental.getOwner())
                            .participant2(rental.getTenant())
                            .build();
                    chatRoomRepository.save(room);
                }
        );

        notificationService.send(
                rental.getTenant(),
                "Produs gata de ridicare",
                rental.getListing().getTitle() + " este gata de ridicare.",
                "READY_TO_PICKUP",
                rentalId
        );

        log.info("Rental {} marked as READY_TO_PICKUP", rentalId);
        return toResponse(rental);
    }

    @Transactional
    public RentalResponse confirmPickup(Long rentalId, User tenant) {
        Rental rental = getRentalAndCheckTenant(rentalId, tenant.getId());

        if (rental.getStatus() != RentalStatus.READY_TO_PICKUP) {
            throw new BusinessException("Rental is not ready for pickup", HttpStatus.BAD_REQUEST);
        }

        rental.setStatus(RentalStatus.ACTIVE);
        rentalRepository.save(rental);

        log.info("Rental {} confirmed as ACTIVE (pickup)", rentalId);
        return toResponse(rental);
    }

    @Transactional
    public RentalResponse returnByQr(String qrToken, User tenant) {
        Rental rental = rentalRepository.findByQrCodeToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid QR token"));

        if (!rental.getTenant().getId().equals(tenant.getId())) {
            throw new BusinessException("Not authorized to return this rental", HttpStatus.FORBIDDEN);
        }

        if (rental.getStatus() != RentalStatus.ACTIVE) {
            throw new BusinessException("Rental is not active", HttpStatus.BAD_REQUEST);
        }

        rental.setStatus(RentalStatus.RETURNED);
        rentalRepository.save(rental);

        paymentService.releaseGuarantee(rental.getId());
        emailService.sendReturnConfirmationToTenant(rental);

        notificationService.send(
                rental.getTenant(),
                "Returnare confirmată",
                "Garanția pentru " + rental.getListing().getTitle() + " a fost eliberată.",
                "RETURN_CONFIRMED",
                rental.getId()
        );

        log.info("Rental {} returned via QR code", rental.getId());
        return toResponse(rental);
    }

    @Transactional
    public RentalResponse cancelRental(Long rentalId, User user, String reason) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        boolean isTenant = rental.getTenant().getId().equals(user.getId());
        boolean isOwner = rental.getOwner().getId().equals(user.getId());

        if (!isTenant && !isOwner) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }

        if (rental.getStatus() != RentalStatus.PENDING_PAYMENT && rental.getStatus() != RentalStatus.PAID) {
            throw new BusinessException("Cannot cancel rental in current status", HttpStatus.BAD_REQUEST);
        }

        rental.setStatus(RentalStatus.CANCELLED);
        rental.setCancellationReason(reason);
        rentalRepository.save(rental);

        log.info("Rental {} cancelled", rentalId);
        return toResponse(rental);
    }

    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyRentalsAsTenant(Long tenantId, Pageable pageable) {
        return rentalRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<RentalResponse> getMyRentalsAsOwner(Long ownerId, Pageable pageable) {
        return rentalRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RentalResponse getRental(Long rentalId, Long userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));

        if (!rental.getTenant().getId().equals(userId) && !rental.getOwner().getId().equals(userId)) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }

        return toResponse(rental);
    }

    @Transactional(readOnly = true)
    public QrCodeResponse getQrCode(Long rentalId, User owner) {
        Rental rental = getRentalAndCheckOwner(rentalId, owner.getId());

        if (rental.getQrCodeToken() == null) {
            throw new BusinessException("QR code not generated yet", HttpStatus.BAD_REQUEST);
        }

        String qrBase64 = qrCodeService.generateQrCodeBase64(rental.getQrCodeToken());
        return new QrCodeResponse(qrBase64, rental.getQrCodeToken());
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (!start.isAfter(LocalDate.now().minusDays(1))) {
            throw new BusinessException("Start date must be in the future", HttpStatus.BAD_REQUEST);
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("End date must be after start date", HttpStatus.BAD_REQUEST);
        }
    }

    private void checkAvailability(Long listingId, LocalDate start, LocalDate end) {
        boolean unavailable = rentalRepository.existsByListingIdAndStatusNotInAndStartDateLessThanAndEndDateGreaterThan(
                listingId,
                List.of(RentalStatus.CANCELLED, RentalStatus.RETURNED),
                end, start
        );
        if (unavailable) throw new ListingUnavailableException();
    }

    private Rental getRentalAndCheckOwner(Long rentalId, Long ownerId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));
        if (!rental.getOwner().getId().equals(ownerId)) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        return rental;
    }

    private Rental getRentalAndCheckTenant(Long rentalId, Long tenantId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental", rentalId));
        if (!rental.getTenant().getId().equals(tenantId)) {
            throw new BusinessException("Not authorized", HttpStatus.FORBIDDEN);
        }
        return rental;
    }

    private RentalResponse toResponse(Rental r) {
        Listing l = r.getListing();
        String firstImage = l.getImages().isEmpty() ? null : l.getImages().get(0).getUrl();
        User tenant = r.getTenant();
        User owner = r.getOwner();

        return new RentalResponse(
                r.getId(), r.getReferenceNumber(),
                new RentalResponse.ListingSummary(l.getId(), l.getTitle(), firstImage, l.getCity()),
                new RentalResponse.UserSummary(tenant.getId(), tenant.getFirstName(), tenant.getLastName(), tenant.getKycStatus() == KycStatus.VERIFIED),
                new RentalResponse.UserSummary(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getKycStatus() == KycStatus.VERIFIED),
                r.getStartDate(), r.getEndDate(), r.getTotalDays(), r.getPricePerDay(),
                r.getSubtotal(), r.getGuaranteeAmount(), r.getTotalAmount(),
                r.getStatus(), r.getQrCodeToken() != null, r.getCreatedAt()
        );
    }
}
