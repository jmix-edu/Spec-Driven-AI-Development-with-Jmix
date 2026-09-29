package com.company.booking.view.desk;

import com.company.booking.entity.Desk;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;


@Route(value = "desks", layout = MainView.class)
@ViewController(id = "Desk.list")
@ViewDescriptor(path = "desk-list-view.xml")
@LookupComponent("desksDataGrid")
@DialogMode(width = "64em")
public class DeskListView extends StandardListView<Desk> {

}