package work.tinax.DiscordTTS;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

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
    }
}
