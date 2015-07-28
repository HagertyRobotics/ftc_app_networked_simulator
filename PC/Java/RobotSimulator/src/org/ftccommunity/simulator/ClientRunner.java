package org.ftccommunity.simulator;

public class ClientRunner implements Runnable {
    private final CoppeliaApiClient simClient;

    public ClientRunner(CoppeliaApiClient newSimClient) {
        this.simClient = newSimClient;
    }

    @Override
    public void run() {
        simClient.loop();
    }
}
