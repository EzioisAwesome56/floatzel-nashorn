package com.eziosoft.floatzel.nashorn.utils;

import com.eziosoft.floatzel.Exception.GenericException;
import com.eziosoft.floatzel.nashorn.LoadPluginException;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import com.eziosoft.floatzel.nashorn.Utils;

public class Plugin {
    public static void runPlugin(CommandEvent event, String name) throws GenericException {
        // before doing ANYTHING; check if the plugin and lib folder exists
        File plugindir = new File("plugins");
        File libdir = new File("plugins/lib");
        if (!plugindir.exists()) {
            plugindir.mkdir();
            libdir.mkdir();
            // write the libs into the folder
            try {
                byte[] npm = IOUtils.toByteArray(Utils.getResourse("/plugin/lib/", "jvm-npm.js"));
                byte[] string = IOUtils.toByteArray(Utils.getResourse("/plugin/lib/", "stringview.js"));
                FileUtils.writeByteArrayToFile(new File("plugins/lib/jvm-npm.js"), npm);
                FileUtils.writeByteArrayToFile(new File("plugins/lib/stringview.js"), string);
            } catch (IOException e) {
                throw new GenericException(e.getMessage());
            }
        }

        // init the scripting engine
        ScriptEngineManager mngt = new ScriptEngineManager();
        mngt.registerEngineName("nashorn2", new NashornScriptEngineFactory());
        ScriptEngine engine = mngt.getEngineByName("nashorn2");
        try {
            engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util, com.eziosoft.floatzel, org.apache.commons.io.IOUtils);");
            // give plugins access to shit
            engine.put("event", event);
            engine.put("guild", event.getGuild());
            engine.put("channel", event.getTextChannel());
            engine.put("message", event.getMessage());
            // load jvm-npm
            engine.eval("load('plugins/lib/jvm-npm.js');");
            // load polyfill.js
            engine.eval(new InputStreamReader(Utils.getResourse("/plugin/lib/", "polyfill.js")));
            // load stringview lib
            engine.eval("load('plugins/lib/stringview.js');");
            // load plugin api utils
            engine.eval(new InputStreamReader(Utils.getResourse("/plugin/", "util.js")));
            // load the plugin file
            engine.eval(new FileReader("plugins/" + name + ".js"));
            // load in Plugin api support file
            engine.eval(new InputStreamReader(Utils.getResourse("/plugin/", "support.js")));
            Invocable runjs = (Invocable) engine;

            // permission checking
            if (!(boolean) runjs.invokeFunction("checkPermission", "")) {
                // check if required permission is bot admin
                if ((boolean) runjs.invokeFunction("isAdmin", "")) {
                    if (!com.eziosoft.floatzel.Util.Utils.isAdmin(event.getAuthor().getId())) {
                        event.getChannel().sendMessage("Error: you are not a bot admin! You cannot run this plugin!").queue();
                        return;
                    }
                } else if ((boolean) runjs.invokeFunction("isOwner", "")) {
                    if (!event.isOwner()) {
                        event.getChannel().sendMessage("Error: you arent a bot owner and cant run this plugin!").queue();
                        return;
                    }
                }
            }

            // try running the plugin
            runjs.invokeFunction("run", "");
        } catch (FileNotFoundException e){
            event.getChannel().sendMessage("The plugin you tired to run doesnt exist!").queue();
            return;
        } catch (ScriptException | NoSuchMethodException e){
            // until i can handle errors better, just print stack trace
            e.printStackTrace();
            return;
        }
    }

    // get plugin information for registering
    public static String[] getPluginInfo(String filename) throws FileNotFoundException, LoadPluginException {
        // we dont need to load the entire plugin API, just enough to get the required strings from them
        ScriptEngineManager mngt = new ScriptEngineManager();
        mngt.registerEngineName("nashorn2", new NashornScriptEngineFactory());
        ScriptEngine engine = mngt.getEngineByName("nashorn2");
        // load the plugin file
        try {
            engine.eval(new FileReader("plugins/" + filename + ".js"));
            // load the support library
            engine.eval(new InputStreamReader(Utils.getResourse("/plugin/", "support.js")));
            Invocable runjs = (Invocable) engine;
            // then load the 2 strings needed
            return new String[]{(String) runjs.invokeFunction("getName", ""), (String) runjs.invokeFunction("getHelp", "")};
        } catch (FileNotFoundException e){
            throw new FileNotFoundException();
        } catch (ScriptException | NoSuchMethodException e){
            // throw that shit baby
            throw new LoadPluginException(e.getMessage());
        }
    }
}
