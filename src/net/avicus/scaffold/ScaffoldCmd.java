package net.avicus.scaffold;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.util.org.apache.commons.io.FileUtils;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class ScaffoldCmd implements CommandExecutor {

    private Scaffold scaffold;

    public ScaffoldCmd(Scaffold scaffold) {
        this.scaffold = scaffold;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            usage(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("version")) {
            sender.sendMessage(ChatColor.GOLD + "Currently running Scaffold v" + scaffold.getDescription().getVersion() + "!");
        }
        else if (sub.equals("reload")) {
            scaffold.reloadConfig();
            scaffold.loadConfig();
            sender.sendMessage(ChatColor.GOLD + "Reloaded configuration file...");
        }
        else if (sub.equals("save")) {
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(ChatColor.RED + "You must be a player!");
                return true;
            }

            Player player = (Player) sender;
            File world = new File(player.getWorld().getName());
            String file = UUID.randomUUID().toString().substring(0, 6) + ".zip";
            File move = new File(scaffold.getSaveDirectory() + "/" + file);

            player.getWorld().save();

            try {
                ZipUtils.zipFolder(world, move);
                sender.sendMessage(ChatColor.GOLD + scaffold.getSaveMessage().replace("<file>", file));
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "An error occurred while compressing, check logs.");
                sender.sendMessage(ChatColor.RED + " > " + e.getMessage());
            }
        }
        else if (sub.equals("clone")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "/scaffold clone <repository> (folder)");
                return true;
            }

            String url = args[1];
            String name = url.split("/")[url.split("/").length - 1];

            if (args.length > 2)
                name = args[2];

            if (new File(name).exists()) {
                sender.sendMessage(ChatColor.RED + "A folder already exists with the name " + name + "!");
                return true;
            }

            int index = StringUtils.ordinalIndexOf(url, "/", 5) + 1;
            String path = url.substring(index);

            boolean hasPath = index > 0 && path.length() > 0;

            if (hasPath)
                url = url.replace(path, "");

            try {
                Process process = Runtime.getRuntime().exec("git clone " + url + " " + name);
                process.waitFor();

                if (!new File(name).exists()) {
                    Scanner scan = new Scanner(process.getErrorStream());
                    throw new IllegalArgumentException(scan.nextLine());
                }
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(ChatColor.RED + "An error occurred while cloning repository, check logs.");
                sender.sendMessage(ChatColor.RED + " > " + e.getMessage());
                return true;
            }

            if (hasPath) {
                String tmp = UUID.randomUUID().toString().substring(0, 6);
                try {
                    FileUtils.moveDirectory(new File(name + "/" + path), new File(tmp));
                    FileUtils.deleteDirectory(new File(name));
                    FileUtils.moveDirectory(new File(tmp), new File(name));
                } catch (IOException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "An error occurred while cloning repository, check logs.");
                    sender.sendMessage(ChatColor.RED + " > " + e.getMessage());
                    return true;
                }
            }

            sender.sendMessage(ChatColor.GOLD + "Cloned repository to " + name + "!");
        }
        else {
            usage(sender);
            return true;
        }

        return true;
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "/scaffold <save/clone/reload/version> (args...)");
    }

}
