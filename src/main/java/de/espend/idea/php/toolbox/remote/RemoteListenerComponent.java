package de.espend.idea.php.toolbox.remote;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.sun.net.httpserver.HttpServer;
import de.espend.idea.php.toolbox.PhpToolboxApplicationService;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class RemoteListenerComponent implements Disposable {

    private final PhpToolboxApplicationService settings;
    private HttpServer server;
    private Thread listenerThread;

    public RemoteListenerComponent(PhpToolboxApplicationService settings) {
        this.settings = settings;

        if(!settings.serverEnabled) {
            return;
        }

        final int port = settings.serverPort;
        final String host = !settings.listenAll ? "localhost" : "0.0.0.0";

        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
        } catch (IOException e) {
            PhpToolboxApplicationService.LOG.error(String.format("Can't bind with server to %s:%s", host, port));
            ApplicationManager.getApplication().invokeLater(() -> Messages.showMessageDialog(
                String.format("Can't bind with server to %s:%s", host, port), "PHP Toolbox",
                Messages.getErrorIcon()
            ));

            return;
        }

        server.createContext("/", new RouterHttpHandler());

        final HttpServer finalServer = server;
        listenerThread = new Thread(() -> {
            finalServer.start();
            PhpToolboxApplicationService.LOG.info(String.format("Starting server on %s:%s", host, port));
        });

        listenerThread.start();
    }

    @Override
    public void dispose() {

        if (listenerThread != null) {
            listenerThread.interrupt();
        }

        if(server != null) {
            server.stop(0);
        }
    }
}