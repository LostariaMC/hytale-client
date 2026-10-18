package fr.lostaria.hytaleclient.listeners;

import com.hypixel.hytale.server.core.event.events.BootEvent;
import fr.lostaria.hytaleclient.model.ServerStatus;
import fr.lostaria.hytaleclient.service.ServerStatusReporter;

public class BootListener {

    private final ServerStatusReporter statusReporter;

    public BootListener(ServerStatusReporter statusReporter) {
        this.statusReporter = statusReporter;
    }

    public void onBoot(BootEvent event) {
        statusReporter.report(ServerStatus.RUNNING);
    }
}
