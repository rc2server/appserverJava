package edu.wvu.stat.rc2.server;

import java.io.Console;

import org.mindrot.jbcrypt.BCrypt;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class HashPasswordCommand extends Command {

	public HashPasswordCommand() {
		super("hashpw", "hashes a password with bcrypt");
	}

	@Override
	public void configure(Subparser subparser) {
	}

	@Override
	public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
		Console c = System.console();
		if (c == null) {
			System.err.println("no console");
			System.exit(1);
		}
		
		String pass = new String(c.readPassword("Enter the password"));
		String hash = BCrypt.hashpw(pass, BCrypt.gensalt());
		System.out.println(hash);
	}

}
