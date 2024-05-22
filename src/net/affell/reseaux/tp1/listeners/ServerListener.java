package net.affell.reseaux.tp1.listeners;

import net.affell.reseaux.tp1.Routeur;
import net.kio.its.event.EventHandler;
import net.kio.its.event.Listener;
import net.kio.its.server.events.ClientSocketClosedEvent;
import net.kio.its.server.events.CommandReceivedEvent;
import net.kio.its.server.events.ConnectionProtocolSuccessEvent;

public class ServerListener implements Listener {

    private final Routeur routeur;

    public ServerListener(Routeur routeur) {
        this.routeur = routeur;
    }

    @EventHandler
    public void onConnect(ConnectionProtocolSuccessEvent e) {
        e.getServerWorker().sendCommand("name", routeur.getServer().getName());
    }

    @EventHandler
    public void onMessage(CommandReceivedEvent e) {
        routeur.getCommandManager().handleSocketCommand(e.getCommand(), e.getArgs(), e.getServerWorker(), e.getServerWorker().getClientSocket().getLocalAddress().getHostAddress(), e.getServerWorker().getClientSocket().getInetAddress().getHostAddress(), e.getServerWorker().getClientSocket().getLocalPort(), true);
    }

    @EventHandler
    public void onDisconnect(ClientSocketClosedEvent e) {
        System.out.println("> Client déconnecté depuis " + e.getServerWorker().getClientSocket().getInetAddress().getHostAddress() + " (" + e.getServerWorker().getClientMacAddress() + ")");
        routeur.getRoutage().remove(e.getServerWorker());
        routeur.getServer().getClients().remove(e.getServerWorker());
    }
}
