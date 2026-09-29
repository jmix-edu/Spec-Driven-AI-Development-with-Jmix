package com.company.booking.view.building;

import com.company.booking.entity.Building;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "buildings/:id", layout = MainView.class)
@ViewController(id = "Building.detail")
@ViewDescriptor(path = "building-detail-view.xml")
@EditedEntityContainer("buildingDc")
public class BuildingDetailView extends StandardDetailView<Building> {
}