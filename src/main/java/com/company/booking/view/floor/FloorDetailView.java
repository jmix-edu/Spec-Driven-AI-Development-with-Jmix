package com.company.booking.view.floor;

import com.company.booking.entity.Floor;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;


@Route(value = "floors/:id", layout = MainView.class)
@ViewController(id = "Floor.detail")
@ViewDescriptor(path = "floor-detail-view.xml")
@EditedEntityContainer("floorDc")
public class FloorDetailView extends StandardDetailView<Floor> {
}