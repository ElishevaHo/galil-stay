package com.galilstay.server.config;

import com.galilstay.server.model.*;
import com.galilstay.server.repository.*;
import com.galilstay.server.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** ממלא נתוני דוגמה בהפעלה הראשונה (רק כשהמסד ריק) */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final HostRepository hosts;
    private final UnitRepository units;
    private final GuestRepository guests;
    private final ExtraRepository extras;
    private final ReservationRepository reservations;
    private final ReviewRepository reviews;
    private final PricingService pricing;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (hosts.count() == 0) {
            Host host = new Host();
            host.setUsername("host");
            host.setPasswordHash(encoder.encode("galil2026"));
            host.setDisplayName("נועה - בעלת המתחם");
            hosts.save(host);
        }
        if (units.count() > 0) return;

        Unit oren = unit("בקתת האורן", UnitType.CABIN, "הגליל העליון", 750, 950, 150, 2, 1, 2, false, true, false,
                "בקתת עץ רומנטית לזוג מול נוף הרי מירון, עם ג'קוזי פנימי ואח מבוערת");
        Unit gefen = unit("וילת הגפן", UnitType.VILLA, "רמת הגולן", 1900, 2600, 350, 12, 5, 2, true, true, true,
                "וילה משפחתית מרווחת עם בריכה פרטית מחוממת, מתחם מנגל וגינה גדולה");
        Unit zait = unit("סוויטת הזית", UnitType.SUITE, "הגליל העליון", 950, 1250, 150, 2, 1, 1, false, true, false,
                "סוויטה מעוצבת בתוך מטע זיתים עתיק, מקלחת גשם וספא זוגי");
        Unit nahal = unit("בקתת הנחל", UnitType.CABIN, "עמק החולה", 680, 880, 120, 4, 2, 2, false, false, true,
                "בקתה כפרית על גדת הנחל, מתאימה למשפחה קטנה, מותר להגיע עם כלב");
        Unit kerem = unit("וילת הכרם", UnitType.VILLA, "רמת הגולן", 2200, 2900, 400, 14, 6, 3, true, true, false,
                "וילת יוקרה בין כרמי היין עם בריכה אינסופית וסלון אירוח ענק");
        Unit tavor = unit("סוויטת התבור", UnitType.SUITE, "עמק יזרעאל", 820, 1050, 130, 3, 1, 1, false, true, false,
                "סוויטה עם מרפסת פרטית מול הר תבור, ארוחת בוקר מוגשת לחדר");
        units.saveAll(List.of(oren, gefen, zait, nahal, kerem, tavor));

        Extra breakfast = extra("ארוחת בוקר גלילית", 120, true);
        Extra massage = extra("עיסוי זוגי (60 דק')", 480, false);
        Extra wine = extra("מארז יינות בוטיק", 210, false);
        Extra lateOut = extra("יציאה מאוחרת (עד 15:00)", 150, false);
        Extra bbq = extra("ערכת מנגל ופחמים", 90, false);
        extras.saveAll(List.of(breakfast, massage, wine, lateOut, bbq));

        Guest dana = guest("דנה", "אברמוביץ", "dana.a@mail.co.il", "052-4417823", "חיפה");
        Guest yossi = guest("יוסי", "מזרחי", "yossim@mail.co.il", "054-9031122", "תל אביב");
        Guest michal = guest("מיכל", "שטרן", "michal.stern@mail.co.il", "050-7712045", "ירושלים");
        Guest amir = guest("אמיר", "חדד", "amir.h@mail.co.il", "053-2280431", "באר שבע");
        Guest tamar = guest("תמר", "גולדברג", "tamar.g@mail.co.il", "058-6649017", "רעננה");
        Guest roni = guest("רוני", "ביטון", "roni.b@mail.co.il", "052-3318876", "אשדוד");
        guests.saveAll(List.of(dana, yossi, michal, amir, tamar, roni));

        LocalDate t = LocalDate.now();
        Reservation r1 = reservation(dana, oren, t.minusDays(40), t.minusDays(38), 2, 0, Set.of(breakfast, wine), ReservationStatus.COMPLETED);
        Reservation r2 = reservation(yossi, gefen, t.minusDays(30), t.minusDays(26), 6, 4, Set.of(bbq), ReservationStatus.COMPLETED);
        Reservation r3 = reservation(michal, zait, t.minusDays(18), t.minusDays(16), 2, 0, Set.of(massage), ReservationStatus.COMPLETED);
        Reservation r4 = reservation(tamar, kerem, t.minusDays(12), t.minusDays(9), 8, 3, Set.of(), ReservationStatus.COMPLETED);
        Reservation r5 = reservation(amir, nahal, t.minusDays(1), t.plusDays(2), 2, 2, Set.of(breakfast), ReservationStatus.CONFIRMED);
        Reservation r6 = reservation(roni, tavor, t, t.plusDays(2), 2, 0, Set.of(breakfast, lateOut), ReservationStatus.CONFIRMED);
        Reservation r7 = reservation(dana, gefen, t.plusDays(3), t.plusDays(6), 5, 3, Set.of(bbq, wine), ReservationStatus.CONFIRMED);
        Reservation r8 = reservation(michal, oren, t.plusDays(5), t.plusDays(7), 2, 0, Set.of(massage), ReservationStatus.PENDING);
        Reservation r9 = reservation(yossi, zait, t.plusDays(16), t.plusDays(20), 2, 0, Set.of(breakfast), ReservationStatus.PENDING);
        Reservation r10 = reservation(tamar, tavor, t.plusDays(25), t.plusDays(27), 2, 1, Set.of(), ReservationStatus.CONFIRMED);
        Reservation r11 = reservation(amir, kerem, t.plusDays(9), t.plusDays(11), 10, 2, Set.of(), ReservationStatus.CANCELLED);
        r11.setRefund(r11.getAmount() / 2);
        reservations.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11));

        reviews.saveAll(List.of(
                review(r1, 5, "שקט מוחלט, נוף מדהים והג'קוזי פשוט חלום. נחזור!"),
                review(r2, 4, "וילה ענקית ונקייה, הבריכה מעולה לילדים. החניה קצת צפופה"),
                review(r3, 5, "הסוויטה הכי יפה שהיינו בה, העיסוי היה שווה כל שקל"),
                review(r4, 4, "מקום מפנק לחגיגה משפחתית, נשמח להגיע שוב בקיץ")
        ));
    }

    private Unit unit(String name, UnitType kind, String region, double weekday, double weekend, double cleaning,
                      int capacity, int bedrooms, int minNights, boolean pool, boolean jacuzzi, boolean pets,
                      String description) {
        Unit u = new Unit();
        u.setName(name);
        u.setKind(kind);
        u.setRegion(region);
        u.setWeekdayRate(weekday);
        u.setWeekendRate(weekend);
        u.setCleaningFee(cleaning);
        u.setCapacity(capacity);
        u.setBedrooms(bedrooms);
        u.setMinNights(minNights);
        u.setPrivatePool(pool);
        u.setJacuzzi(jacuzzi);
        u.setPetsAllowed(pets);
        u.setDescription(description);
        u.setListed(true);
        return u;
    }

    private Extra extra(String title, double price, boolean perNight) {
        Extra e = new Extra();
        e.setTitle(title);
        e.setPrice(price);
        e.setChargedPerNight(perNight);
        return e;
    }

    private Guest guest(String first, String last, String email, String phone, String city) {
        Guest g = new Guest();
        g.setFirstName(first);
        g.setLastName(last);
        g.setEmail(email);
        g.setPhone(phone);
        g.setCity(city);
        g.setJoinedOn(LocalDate.now().minusMonths(3));
        return g;
    }

    private Reservation reservation(Guest guest, Unit unit, LocalDate arrival, LocalDate departure,
                                    int adults, int children, Set<Extra> chosen, ReservationStatus status) {
        Reservation r = new Reservation();
        r.setGuest(guest);
        r.setUnit(unit);
        r.setArrival(arrival);
        r.setDeparture(departure);
        r.setAdults(adults);
        r.setChildren(children);
        r.setExtras(new HashSet<>(chosen));
        r.setStatus(status);
        r.setAmount(pricing.quote(unit, arrival, departure, chosen).total());
        r.setCreatedAt(LocalDateTime.of(arrival.minusDays(20), LocalTime.NOON));
        return r;
    }

    private Review review(Reservation reservation, int stars, String text) {
        Review r = new Review();
        r.setReservation(reservation);
        r.setStars(stars);
        r.setText(text);
        r.setWrittenOn(reservation.getDeparture().plusDays(1));
        return r;
    }
}
