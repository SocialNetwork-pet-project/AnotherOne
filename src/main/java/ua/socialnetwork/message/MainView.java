package ua.socialnetwork.message;


import com.github.rjeschke.txtmark.Processor;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.socialnetwork.security.SecurityUser;

@Route("/chat")
public class MainView extends VerticalLayout {
    private final Storage storage;
    private Registration registration;

    private Grid<Storage.ChatMessage> grid;
    private VerticalLayout chat;
    private VerticalLayout login;
    private String user = "";

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //ToDO put in try catch
    SecurityUser u = (SecurityUser) authentication.getPrincipal();

    public MainView(Storage storage) {
        this.storage = storage;

        buildLogin();
        buildChat();
    }

//    ToDO remove login form
    private void buildLogin() {
        login = new VerticalLayout() {{
            TextField field = new TextField();
            field.setValue(u.getFullName());
            field.setReadOnly(true);
            field.setPlaceholder("Please, introduce yourself");
            add(
                    field,
                    new Button("Join") {{
                        addClickListener(click -> {
                            login.setVisible(false);
                            chat.setVisible(true);
                            user = field.getValue();
                            storage.addRecordJoined(user);
                        });
                        addClickShortcut(Key.ENTER);
                    }}
            );
        }};
        add(login);
    }

    private void buildChat() {
        chat = new VerticalLayout();
        add(chat);
        chat.setVisible(false);

        grid = new Grid<>();
        grid.setItems(storage.getMessages());
        grid.addColumn(new ComponentRenderer<>(message -> new Html(renderRow(message))))
                .setAutoWidth(true);

        TextField field = new TextField();

        chat.add(
                grid,
                new HorizontalLayout() {{
                    add(
                            field,
                            new Button("➡") {{
                                addClickListener(click -> {
                                    storage.addRecord(user, field.getValue());
                                    field.clear();
                                });
                                addClickShortcut(Key.ENTER);
                            }}
                    );
                }}
        );
    }

    public void onMessage(Storage.ChatEvent event) {
        if (getUI().isPresent()) {
            UI ui = getUI().get();
            ui.getSession().lock();
            ui.beforeClientResponse(grid, ctx -> grid.scrollToEnd());
            ui.access(() -> grid.getDataProvider().refreshAll());
            ui.getSession().unlock();
        }
    }

    private String renderRow(Storage.ChatMessage message) {
        if (message.getName().isEmpty()) {
            return Processor.process(String.format("_User **%s** is just joined the chat!_", message.getMessage()));
        } else {
            return Processor.process(String.format("**%s**: %s", message.getName(), message.getMessage()));
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        registration = storage.attachListener(this::onMessage);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
    }
}