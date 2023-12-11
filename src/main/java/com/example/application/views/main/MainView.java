package com.example.application.views.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.TextArea;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Route("")
public class MainView extends VerticalLayout {
    public record File(String fileName, String contents) {}

    public String run(String host, File jclFile, File ymlFile) {
        try {
            Thread.sleep(3000);
            return jclFile.contents();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public MainView() {
        setMaxWidth("920px");
        Map<String, File> files = new HashMap<>();
        Button runButton = new Button("Run");
        runButton.setEnabled(false);

        TextField user = new TextField();
        user.setPlaceholder("user");
        user.setRequired(true);
        PasswordField password = new PasswordField();
        password.setPlaceholder("password");
        password.setRequired(true);

        ComboBox<String> jclHost = new ComboBox<>();
        jclHost.setPlaceholder("Select a host");
        jclHost.setItems("my.host.com", "my.host2.com");
        jclHost.setRequired(true);
        jclHost.setRequiredIndicatorVisible(true);
        jclHost.addValueChangeListener(event -> {
            if (!user.isEmpty() && !password.isEmpty() && files.size() == 2)
                runButton.setEnabled(true);
        });

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload uploadFiles = new Upload(buffer);
        uploadFiles.setMaxFiles(2);
//        uploadFiles.setAcceptedFileTypes(".jcl", ".yml", ".yaml");

        uploadFiles.addSucceededListener(event ->
                {
                    String fileName = event.getFileName();
                    try {
                        File file = new File(
                                fileName,
                                new String(buffer.getInputStream(fileName).readAllBytes())
                        );
                        if (fileName.endsWith(".jcl")) files.put("jcl", file);
                        if (fileName.endsWith(".yml")) files.put("yml", file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        TextArea textArea = new TextArea();
        textArea.setLabel("Job output");
        textArea.setMaxHeight("400px");
        textArea.setMinHeight("400px");
        textArea.setWidth("890px");
        textArea.setReadOnly(true);

        runButton.addClickListener(click -> {
            String result = run(jclHost.getValue(), files.get("jcl"), files.get("yml"));
            textArea.setValue(result);
        });

        FormLayout form = new FormLayout();
        form.add(uploadFiles, user,
                jclHost, password);

        add(
                new H1("Vaadin JCL Runner"),
                form,
                runButton,
                textArea
        );
    }
}