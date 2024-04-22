package work.tinax.discordTTS;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws LoginException
    {
    	String token = System.getenv("DISCORD_TOKEN");
    	if (token == null || token.isEmpty()) {
    		System.err.println("envvar DISCORD_TOKEN is not defined or is empty!");
    		System.exit(1);
    	}
    	
    	JDA jda = JDABuilder.createDefault(token).build();
    	
    	jda.addEventListener(new DiscordBot());

		CommandListUpdateAction commands = jda.updateCommands();
		commands.addCommands(
				Commands.slash("join_tts", "ttsbird を VC に参加させます"),
				Commands.slash("leave_tts", "ttsbird を VC から退出させます")
		);
		commands.queue();
    }
}
