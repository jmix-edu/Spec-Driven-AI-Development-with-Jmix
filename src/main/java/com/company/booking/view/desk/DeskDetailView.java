package com.company.booking.view.desk;

import com.company.booking.entity.Desk;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "desks/:id", layout = MainView.class)
@ViewController(id = "Desk.detail")
@ViewDescriptor(path = "desk-detail-view.xml")
@EditedEntityContainer("deskDc")
public class DeskDetailView extends StandardDetailView<Desk> {
}