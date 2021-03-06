package com.vaadin.flow.component.applayout;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.server.startup.RouteRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UI.class)
public class AbstractAppRouterLayoutTest {

    public class TestAppRouterLayout extends AbstractAppRouterLayout {

        @Override
        protected void configure(AppLayout appLayout,
            AppLayoutMenu appLayoutMenu) {
            events.add("Configured");
        }

        @Override
        protected void beforeNavigate(String route, HasElement content) {
            events.add("Before nav to " + route);
        }

        @Override
        protected void afterNavigate(String route, HasElement content) {
            events.add("After nav to " + route);
        }
    }

    @Route("route1")
    private static class Route1 extends Div {
    }

    @Route("route2")
    private static class Route2 extends Div {
    }

    private final List<String> events = new ArrayList<>();

    private AbstractAppRouterLayout systemUnderTest;

    @Before
    public void setup() {
        systemUnderTest = new TestAppRouterLayout();
    }

    @Test
    public void init() {
        Assert.assertEquals(1, events.size());

        // Ensure configure() hook gets called
        Assert.assertEquals("Configured", events.get(0));
    }

    @Test
    public void showRouterLayoutContent() {
        setupFlowRouting();

        AppLayoutMenuItem route1MenuItem = new AppLayoutMenuItem("Route 1",
            "route1");
        systemUnderTest.getAppLayoutMenu().addMenuItems(route1MenuItem,
            new AppLayoutMenuItem("Dummy", "dummy"));

        Route1 route1 = new Route1();

        // Simulate navigation to Route1 (which has a matching menu item)
        systemUnderTest.showRouterLayoutContent(route1);

        // Ensure beforeNavigate() hook gets called
        Assert.assertEquals("Before nav to route1", events.get(1));

        // Ensure afterNavigate() hook gets called
        Assert.assertEquals("After nav to route1", events.get(2));

        // Ensure the matching menu item is selected
        Assert.assertEquals(route1MenuItem,
            systemUnderTest.getAppLayoutMenu().getSelectedMenuItem());
        Assert.assertEquals(route1.getElement(),
            systemUnderTest.getAppLayout().getContent());

        // Simulate navigation to Route2 (which has no matching menu item)
        systemUnderTest.showRouterLayoutContent(new Route2());

        // Ensure selected menu item remains unchanged
        Assert.assertEquals(route1MenuItem,
            systemUnderTest.getAppLayoutMenu().getSelectedMenuItem());
    }

    private void setupFlowRouting() {
        // Isolate Flow's UI and routing mechanism for testing
        PowerMockito.mockStatic(UI.class);
        UI ui = Mockito.mock(UI.class);
        BDDMockito.given(UI.getCurrent()).willReturn(ui);

        RouteRegistry registry = Mockito.mock(RouteRegistry.class);
        Router router = Mockito.spy(new Router(registry));
        Mockito.when(ui.getRouter()).thenReturn(router);

        // Let Flow resolve route URLs
        Mockito.doAnswer(invocationOnMock -> {
            Class routeClass = (Class) invocationOnMock.getArguments()[0];
            Route route = (Route) routeClass.getDeclaredAnnotation(Route.class);
            return route.value();
        }).when(router).getUrl(Mockito.any(Class.class));
    }
}
