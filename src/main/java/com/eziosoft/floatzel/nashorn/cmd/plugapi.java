package com.eziosoft.floatzel.nashorn.cmd;

import com.eziosoft.floatzel.Commands.FCommand;
import com.jagrosh.jdautilities.command.CommandEvent;

public class plugapi extends FCommand {

    public plugapi(){
        name = "papi";
        description = "Plugin management api";
        category = owner;
        ownerCommand = true;
    }

    @Override
    protected void cmdrun(CommandEvent event){

    }
}
