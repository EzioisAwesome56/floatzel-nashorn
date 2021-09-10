package com.eziosoft.floatzel.nashorn.cmd;

import com.eziosoft.floatzel.Commands.FCommand;
import com.eziosoft.floatzel.nashorn.BasePlugin;
import com.eziosoft.floatzel.Exception.GenericException;
import com.eziosoft.floatzel.nashorn.LoadPluginException;
import com.eziosoft.floatzel.Floatzel;
import com.eziosoft.floatzel.nashorn.utils.Plugin;
import com.jagrosh.jdautilities.command.CommandEvent;

import java.io.FileNotFoundException;

public class plugapi extends FCommand {

    public plugapi(){
        name = "papi";
        description = "Plugin management api";
        category = owner;
        ownerCommand = true;
    }

    @Override
    protected void cmdrun(CommandEvent event) throws GenericException, LoadPluginException {
        // basically lift a bunch of code from Floatzel
        if (event.getArgs().length() < 1) {
            event.getChannel().sendMessage("You didn't provide the function you want to preform!").queue();
            return;
        }
        if (argsplit[0].equals("load")) {
            // check to make sure the user provided a file name
            try {
                if (argsplit[1].isEmpty()) {
                    event.getChannel().sendMessage("you didnt provide a plugin filename for me to register!").queue();
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException e){
                event.reply("You didnt provide a plugin filename for me to load!");
                return;
            }
            String[] info;
            try {
                info = Plugin.getPluginInfo(argsplit[1]);
            } catch (FileNotFoundException e) {
                event.getChannel().sendMessage("Error: that plugin doesn't exist!").queue();
                return;
            } catch (LoadPluginException e) {
                throw e;
            } catch (ArrayIndexOutOfBoundsException e){
                event.reply("You didnt tell me what plugin you wanted to load!");
                return;
            }
            // then register it
            Floatzel.commandClient.addCommand(new BasePlugin(info[0], argsplit[1], info[1]));
            event.getChannel().sendMessage("Plugin has been fucking loaded!").queue();
            return;
        } else if (argsplit[0].equals("run")) {
            try {
                Plugin.runPlugin(event, argsplit[1]);
            } catch (ArrayIndexOutOfBoundsException e){
                event.reply("You didnt tell me what to run!");
                return;
            } catch (GenericException e){
                throw e;
            }
        }

    }
}
