package org.obiz.sdtdbot;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Config {

    private static final Logger log = LogManager.getLogger(Config.class);

    //here bunch of configs
    private String dbUrl = "jdbc:h2:./pathDB;AUTO_SERVER=TRUE";
    private String dbUser = "sa";
    private String dbPassword = "sa";
    private String token = "";
    private String host = "";
    private int port = 0;
    private String sshUser = "";
    private String sshPasswd = "";
    private int telnetPort = 0;
    private String telnetPasswd = "";
    private String logFileName = "";
    private long botChannelId = 0;
    private String opDiscordRole = "";
    private String ownerDiscordID = "";
    private String runServerCmd;
    private String killServerCmd;
    private String MAP_FILE = "";
    private String MAP_FILE_SMALL = "";

    public Config(String[] args) {
        if (args.length > 0) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            token = args[2];
        } else {

            Configurations configs = new Configurations();
            try {
                Configuration config = configs.properties(new File("config.properties"));
                if(config.containsKey("server.db.url")) {
                    dbUrl = config.getString("server.db.url", dbUrl);
                    log.debug("dbUrl = " + dbUrl);
                    dbUser = config.getString("server.db.user", dbUser);
                    dbPassword = config.getString("server.db.password", dbPassword);
                }
                token = config.getString("discord.token");
                log.debug("token = " + token);
                botChannelId = config.getLong("discord.chanel");
                log.debug("botChannelId = " + botChannelId);
                opDiscordRole = config.getString("discord.opRole");
                log.debug("opDiscordRole = " + opDiscordRole);
                ownerDiscordID = config.getString("discord.ownerID");
                log.debug("ownerDiscordID = " + ownerDiscordID);
                host = config.getString("server.ssh.host");
                log.debug("host = " + host);
                port = config.getInt("server.ssh.port");
                log.debug("port = " + port);
                sshUser = config.getString("server.ssh.user");
                log.debug("sshUser = " + sshUser);
                sshPasswd = config.getString("server.ssh.password");
                log.debug("sshPasswd = " + sshPasswd);
                telnetPort = config.getInt("server.telnet.port");
                log.debug("telnetPort = " + telnetPort);
                telnetPasswd = config.getString("server.telnet.password");
                log.debug("telnetPasswd = " + telnetPasswd);
                logFileName = config.getString("server.logFileName");
                log.debug("logFileName = " + logFileName);
                runServerCmd = config.getString("server.runServerCmd");
                log.debug("runServerCmd = " + runServerCmd);
                killServerCmd = config.getString("server.killServerCmd");
                log.debug("killServerCmd = " + killServerCmd);
                MAP_FILE = config.getString("server.mapFile");
                log.debug("MAP_FILE = " + MAP_FILE);
                MAP_FILE_SMALL = config.getString("server.mapFileSmall");
                log.debug("MAP_FILE_SMALL = " + MAP_FILE_SMALL);
            } catch (ConfigurationException cex) {
                // Something went wrong
                log.fatal("Can't read config:" + cex.getMessage(), cex);
            }
            log.info("Config loaded");
        }
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getToken() {
        return token;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSshUser() {
        return sshUser;
    }

    public String getSshPasswd() {
        return sshPasswd;
    }

    public int getTelnetPort() {
        return telnetPort;
    }

    public String getTelnetPasswd() {
        return telnetPasswd;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public long getBotChannelId() {
        return botChannelId;
    }

    public String getOpDiscordRole() {
        return opDiscordRole;
    }

    public String getOwnerDiscordID() {
        return ownerDiscordID;
    }

    public String getRunServerCmd() {
        return runServerCmd;
    }

    public String getKillServerCmd() {
        return killServerCmd;
    }

    public String getMapFile() {
        return MAP_FILE;
    }

    public String getMapFileSmall() {
        return MAP_FILE_SMALL;
    }
}
