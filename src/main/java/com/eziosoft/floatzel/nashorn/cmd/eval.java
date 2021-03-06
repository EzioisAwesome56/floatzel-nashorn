package com.eziosoft.floatzel.nashorn.cmd;

import com.eziosoft.floatzel.Commands.FCommand;
import com.jagrosh.jdautilities.command.CommandEvent;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;

public class eval extends FCommand {

    public eval(){
        name = "eval";
        help = "runs provided statement";
        ownerCommand = true;
        category = owner;
    }
    @Override
    protected void cmdrun(CommandEvent event) throws ScriptException {
        String code = event.getArgs();
        if (code.length() < 0){
            event.getChannel().sendMessage("Error: invalid arguments!").queue();
            return;
        }
        // enable the scripting manager
        ScriptEngineManager mngt = new ScriptEngineManager();
        ScriptEngine engine;
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_15)){
            try {
                mngt.registerEngineName("nashorn2", (ScriptEngineFactory) eval.class.getClassLoader().loadClass("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory").getConstructor().newInstance());
            } catch (ClassNotFoundException | NoSuchMethodException e){
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            engine = mngt.getEngineByName("nashorn2");
        } else {
            engine = mngt.getEngineByName("nashorn");
        }
        try{
            engine.eval("var imports = new JavaImporter(java.io, java.lang, java.util, com.eziosoft.floatzel);");
        } catch (ScriptException e){
            throw e;
        }
        // now actually run this
        try {
            engine.put("command", this);
            engine.put("event", event);
            engine.put("client", event.getClient());
            engine.put("guild", event.getGuild());
            engine.put("channel", event.getTextChannel());
            engine.put("jda", event.getJDA());
            // now try to run it
            Object out = engine.eval(
                    "(function() {" +
                            "with (imports) {" +
                            code +
                            "}" +
                            "})();");
            if (out == null){
                event.getChannel().sendMessage("```Whatever you tried probably worked, unless you requested a object```").queue();
                return;
            } else {
                event.getChannel().sendMessage("```"+out.toString()+"```").queue();
                return;
            }
        } catch (ScriptException e){
            throw e;
        } catch (Exception e){
            throw e;
        }
    }
}
