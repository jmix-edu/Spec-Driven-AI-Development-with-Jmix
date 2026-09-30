package com.company.booking.security;

import com.company.booking.entity.*;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

/**
 * Books rooms and desks for themselves; sees the office structure only to pick in the booking form.
 * Assign together with {@link UiMinimalRole} and the row-level {@link OwnBookingsRole}.
 */
@ResourceRole(name = "Employee", code = EmployeeRole.CODE)
public interface EmployeeRole {

    String CODE = "employee";

    // No DELETE: employees cancel their bookings (status = CANCELLED) instead.
    @EntityAttributePolicy(entityClass = Booking.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Booking.class,
            actions = {EntityPolicyAction.READ, EntityPolicyAction.CREATE, EntityPolicyAction.UPDATE})
    void booking();

    @EntityAttributePolicy(entityClass = Building.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Building.class, actions = EntityPolicyAction.READ)
    void building();

    @EntityAttributePolicy(entityClass = Floor.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Floor.class, actions = EntityPolicyAction.READ)
    void floor();

    @EntityAttributePolicy(entityClass = Room.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Room.class, actions = EntityPolicyAction.READ)
    void room();

    @EntityAttributePolicy(entityClass = Desk.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Desk.class, actions = EntityPolicyAction.READ)
    void desk();

    @EntityAttributePolicy(entityClass = Amenity.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Amenity.class, actions = EntityPolicyAction.READ)
    void amenity();

    // Only the attributes of User's instance name.
    @EntityAttributePolicy(entityClass = User.class, attributes = {"username", "firstName", "lastName"},
            action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.READ)
    void user();

    @ViewPolicy(viewIds = {"Booking.list", "Booking.detail", "Room.list", "Desk.list", "User.list"})
    @MenuPolicy(menuIds = "Booking.list")
    void views();
}
