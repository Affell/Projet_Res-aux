package net.affell.reseaux.tp1;

import net.kio.its.responsesystem.IResponseWorker;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager {

    private final Routeur routeur;

    public CommandManager(Routeur routeur) {
        this.routeur = routeur;
    }

    public void handleSocketCommand(String command, String[] args, IResponseWorker worker, String host, String targetIp, int targetPort, boolean server) {
        switch (command) {
            case "name" -> {
                if (args.length != 1) return;
                routeur.getRoutage().put(worker, args[0]);
                if (!server) worker.sendCommand("name", routeur.getServer().getName());
                System.out.println("> Connecté au routeur " + args[0] + " (" + targetIp + ") sur le port " + targetPort);
            }
            case "message" -> {
                if (args.length != 1) return;
                System.out.println("> Message reçu du routeur " + routeur.getRoutage().getOrDefault(worker, "") + " (" + targetIp + ") : \"" + args[0] + "\"");
            }
            case "ping" -> {
                // Format de la commande : ping <source> <cible> <chemin> <timestamp d'envoi>
                if (args.length != 4) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName())) {
                    // Ce routeur est la cible du ping
                    routeur.updateCache(path);
                    try {
                        worker.sendCommand("pong", args[1], args[0], routeur.getServer().getName(), host, String.valueOf(System.nanoTime() - Long.parseLong(args[3])));
                    } catch (NumberFormatException ignored) {
                    }
                } else {
                    // On relaie la commande car ce routeur n'est pas la cible
                    if (!path.contains(routeur.getServer().getName())) { // Si le chemin nous contient déjà, c'est que la commande a fait un tour sur le réseau et n'a pas trouvé la cible, on ne relaie pas
                        routeur.updateCache(path);
                        path.add(routeur.getServer().getName());
                        args[2] = String.join(",", path);
                        routeur.dispatchCommand(args[0], args[1], path, command, args);
                    }
                }
            }
            case "pong" -> {
                // Format de la commande : pong <source> <cible> <chemin> <timestamp d'envoi>
                if (args.length != 5) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName()) && routeur.waitReply) {
                    // Ce routeur est la cible du pong
                    routeur.updateCache(path);
                    routeur.waitReply = false;
                    System.out.println("PONG from " + args[0] + "(" + new StringBuilder(args[2]).reverse() + "): time=" + (Long.parseLong(args[4]) / 1000000D) + "ms");
                } else {
                    // On relaie la commande car ce routeur n'est pas la cible
                    if (!path.contains(routeur.getServer().getName())) {
                        routeur.updateCache(path);
                        path.add(routeur.getServer().getName());
                        args[2] = String.join(",", path);
                        routeur.dispatchCommand(args[0], args[1], path, command, args);
                    }
                }
            }
            case "trace" -> {
                if (args.length != 3) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName())) {
                    routeur.updateCache(path);
                    worker.sendCommand("route", args[1], args[0], routeur.getServer().getName(), String.join(",", args[2]) + "," + routeur.getServer().getName());
                } else {
                    if (!path.contains(routeur.getServer().getName())) {
                        routeur.updateCache(path);
                        path.add(routeur.getServer().getName());
                        args[2] = String.join(",", path);
                        routeur.dispatchCommand(args[0], args[1], path, command, args);
                    }
                }
            }
            case "route" -> {
                if (args.length != 4) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName())) {
                    routeur.updateCache(path);
                    routeur.waitReply = false;
                    String[] split = args[3].split(",");
                    for (int i = 0; i < split.length; i++) {
                        if (i != 0) System.out.println("\t- " + i + ": " + split[i]);
                    }
                } else {
                    if (!path.contains(routeur.getServer().getName())) {
                        routeur.updateCache(path);
                        path.add(routeur.getServer().getName());
                        args[2] = String.join(",", path);
                        routeur.dispatchCommand(args[0], args[1], path, command, args);
                    }
                }
            }
        }
    }


}
