package com.company.booking.view.amenity;

import com.company.booking.entity.Amenity;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "amenities", layout = MainView.class)
@ViewController(id = "Amenity.list")
@ViewDescriptor(path = "amenity-list-view.xml")
@LookupComponent("amenitiesDataGrid")
@DialogMode(width = "64em")
public class AmenityListView extends StandardListView<Amenity> {
}