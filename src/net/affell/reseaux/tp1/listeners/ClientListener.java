package net.affell.reseaux.tp1.listeners;

import net.affell.reseaux.tp1.Routeur;
import net.kio.its.client.events.CommandReceivedEvent;
import net.kio.its.client.events.ConnectionProtocolSuccessEvent;
import net.kio.its.client.events.ServerSocketClosedEvent;
import net.kio.its.event.EventHandler;
import net.kio.its.event.Listener;

public class ClientListener implements Listener {

    private final Routeur routeur;

    public ClientListener(Routeur routeur) {
        this.routeur = routeur;
    }

    @EventHandler
    public void onConnect(ConnectionProtocolSuccessEvent e) {
    }

    @EventHandler
    public void onCommand(CommandReceivedEvent e) {
        routeur.getCommandManager().handleSocketCommand(e.getCommand(), e.getArgs(), e.getSocketWorker(), e.getSocketWorker().getSocket().getInetAddress().getHostAddress(), e.getSocketWorker().getTargetIp(), e.getSocketWorker().getTargetPort(), false);
    }

    @EventHandler
    public void onDisconnect(ServerSocketClosedEvent e) {
        System.out.println("> Routeur " + routeur.getRoutage().getOrDefault(e.getSocketWorker(), "") + " déconnecté depuis " + e.getSocketWorker().getTargetIp() + " " + e.getSocketWorker().getTargetPort() + " (" + routeur.getClient().getSocketWorkerList().indexOf(e.getSocketWorker()) + ")");
        routeur.getRoutage().remove(e.getSocketWorker());
        routeur.getClient().getSocketWorkerList().remove(e.getSocketWorker());
    }

}
