package com.company.booking.view.building;

import com.company.booking.entity.Building;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "buildings", layout = MainView.class)
@ViewController(id = "Building.list")
@ViewDescriptor(path = "building-list-view.xml")
@LookupComponent("buildingsDataGrid")
@DialogMode(width = "64em")
public class BuildingListView extends StandardListView<Building> {

}