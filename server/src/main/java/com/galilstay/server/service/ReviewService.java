package com.galilstay.server.service;

import com.galilstay.server.dto.ReviewDto;
import com.galilstay.server.exception.NotFoundException;
import com.galilstay.server.exception.RuleViolationException;
import com.galilstay.server.model.Reservation;
import com.galilstay.server.model.ReservationStatus;
import com.galilstay.server.model.Review;
import com.galilstay.server.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviews;
    private final ReservationService reservationService;

    public List<ReviewDto> findAll() {
        return reviews.findAllByOrderByWrittenOnDesc().stream().map(this::toDto).toList();
    }

    public ReviewDto create(ReviewDto dto) {
        Reservation reservation = reservationService.load(dto.getReservationId());
        if (reservation.getStatus() != ReservationStatus.COMPLETED)
            throw new RuleViolationException("אפשר לכתוב חוות דעת רק על שהייה שהסתיימה");
        if (reviews.existsByReservationId(reservation.getId()))
            throw new RuleViolationException("כבר נכתבה חוות דעת על ההזמנה הזו");

        Review review = new Review();
        review.setReservation(reservation);
        review.setStars(dto.getStars());
        review.setText(dto.getText());
        review.setWrittenOn(LocalDate.now());
        return toDto(reviews.save(review));
    }

    public void delete(Long id) {
        if (!reviews.existsById(id)) throw new NotFoundException("חוות דעת", id);
        reviews.deleteById(id);
    }

    private ReviewDto toDto(Review review) {
        Reservation r = review.getReservation();
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setReservationId(r.getId());
        dto.setStars(review.getStars());
        dto.setText(review.getText());
        dto.setWrittenOn(review.getWrittenOn());
        dto.setUnitName(r.getUnit().getName());
        dto.setGuestName(r.getGuest().getFirstName() + " " + r.getGuest().getLastName());
        return dto;
    }
}
