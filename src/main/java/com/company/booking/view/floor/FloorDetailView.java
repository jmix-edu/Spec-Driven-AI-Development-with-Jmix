package com.company.booking.view.floor;

import com.company.booking.entity.Floor;
import com.company.booking.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.FileRef;
import io.jmix.flowui.component.image.JmixImage;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.*;

@Route(value = "floors/:id", layout = MainView.class)
@ViewController(id = "Floor.detail")
@ViewDescriptor(path = "floor-detail-view.xml")
@EditedEntityContainer("floorDc")
public class FloorDetailView extends StandardDetailView<Floor> {

    @ViewComponent
    private JmixImage<FileRef> planImage;

    @Subscribe
    public void onReady(final ReadyEvent event) {
        updatePlanPreview();
    }

    @Subscribe(id = "floorDc", target = Target.DATA_CONTAINER)
    public void onFloorDcItemPropertyChange(final InstanceContainer.ItemPropertyChangeEvent<Floor> event) {
        if ("plan".equals(event.getProperty())) {
            updatePlanPreview();
        }
    }

    private void updatePlanPreview() {
        planImage.setVisible(getEditedEntity().getPlan() != null);
    }
}
