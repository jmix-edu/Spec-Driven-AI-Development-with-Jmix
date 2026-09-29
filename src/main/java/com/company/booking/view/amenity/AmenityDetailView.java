package com.company.booking.view.amenity;

import com.company.booking.entity.Amenity;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "amenities/:id", layout = MainView.class)
@ViewController(id = "Amenity.detail")
@ViewDescriptor(path = "amenity-detail-view.xml")
@EditedEntityContainer("amenityDc")
public class AmenityDetailView extends StandardDetailView<Amenity> {
}