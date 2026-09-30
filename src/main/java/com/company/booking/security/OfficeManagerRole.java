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
 * Manages the office structure and the amenity catalog; sees all bookings read-only.
 * Assign together with {@link UiMinimalRole}.
 */
@ResourceRole(name = "Office Manager", code = OfficeManagerRole.CODE)
public interface OfficeManagerRole {

    String CODE = "office-manager";

    @EntityAttributePolicy(entityClass = Building.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Building.class, actions = EntityPolicyAction.ALL)
    void building();

    @EntityAttributePolicy(entityClass = Floor.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Floor.class, actions = EntityPolicyAction.ALL)
    void floor();

    @EntityAttributePolicy(entityClass = Room.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Room.class, actions = EntityPolicyAction.ALL)
    void room();

    @EntityAttributePolicy(entityClass = Desk.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Desk.class, actions = EntityPolicyAction.ALL)
    void desk();

    @EntityAttributePolicy(entityClass = Amenity.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Amenity.class, actions = EntityPolicyAction.ALL)
    void amenity();

    @EntityAttributePolicy(entityClass = Booking.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Booking.class, actions = EntityPolicyAction.READ)
    void booking();

    // Only the attributes of User's instance name, for the booker column.
    @EntityAttributePolicy(entityClass = User.class, attributes = {"username", "firstName", "lastName"},
            action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = User.class, actions = EntityPolicyAction.READ)
    void user();

    @ViewPolicy(viewIds = {
            "Booking.list", "Booking.detail",
            "Building.list", "Building.detail",
            "Floor.detail", "Room.detail", "Desk.detail",   // composition dialogs
            "Amenity.list", "Amenity.detail"})
    @MenuPolicy(menuIds = {"Booking.list", "Building.list", "Amenity.list"})
    void views();
}
