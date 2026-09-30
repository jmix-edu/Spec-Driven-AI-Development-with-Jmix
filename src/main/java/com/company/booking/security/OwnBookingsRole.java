package com.company.booking.security;

import com.company.booking.entity.Booking;
import com.company.booking.entity.User;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.security.model.RowLevelBiPredicate;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.security.role.annotation.JpqlRowLevelPolicy;
import io.jmix.security.role.annotation.PredicateRowLevelPolicy;
import io.jmix.security.role.annotation.RowLevelRole;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

/**
 * Keeps an employee to their own data: their own bookings, and only their own user row
 * (so the booker lookup offers nobody else).
 */
@RowLevelRole(name = "Own bookings", code = OwnBookingsRole.CODE)
public interface OwnBookingsRole {

    String CODE = "own-bookings";

    @JpqlRowLevelPolicy(entityClass = Booking.class, where = "{E}.user.username = :current_user_username")
    void readOwnBookings();

    // Creating or changing a booking is allowed only when it belongs to the current user.
    @PredicateRowLevelPolicy(entityClass = Booking.class,
            actions = {RowLevelPolicyAction.CREATE, RowLevelPolicyAction.UPDATE})
    default RowLevelBiPredicate<Booking, ApplicationContext> writeOwnBookings() {
        return (booking, applicationContext) -> {
            String currentUsername = applicationContext.getBean(CurrentAuthentication.class).getUser().getUsername();
            return booking.getUser() != null
                    && Objects.equals(booking.getUser().getUsername(), currentUsername);
        };
    }

    @JpqlRowLevelPolicy(entityClass = User.class, where = "{E}.username = :current_user_username")
    void readOwnUser();
}
