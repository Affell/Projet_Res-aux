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
                if (args.length != 4) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName())) {
                    routeur.updateCache(path);
                    try {
                        worker.sendCommand("pong", args[1], args[0], routeur.getServer().getName(), host, String.valueOf(System.nanoTime() - Long.parseLong(args[3])));
                    } catch (NumberFormatException ignored) {
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
            case "pong" -> {
                if (args.length != 5) return;
                List<String> path = Arrays.stream(args[2].split(",")).collect(Collectors.toList());
                if (args[1].equals(routeur.getServer().getName()) && routeur.waitReply) {
                    routeur.updateCache(path);
                    routeur.waitReply = false;
                    System.out.println("PONG from " + args[0] + "(" + new StringBuilder(args[2]).reverse() + "): time=" + (Long.parseLong(args[4]) / 1000000D) + "ms");
                } else {
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
