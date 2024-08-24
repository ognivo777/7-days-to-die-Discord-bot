package org.obiz.sdtdbot.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerInfo {
    private static final String LP_PARSE_REGEXP = "^\\d+\\. id=(\\d+), ([^,]+), " +
            "pos=\\((-?[\\d.]+), (-?[\\d.]+), (-?[\\d.]+)\\), " +
            "rot=\\((-?[\\d.]+), (-?[\\d.]+), (-?[\\d.]+)\\), " +
            "remote=(\\w+), " +
            "health=(\\d+), " +
            "deaths=(\\d+), " +
            "zombies=(\\d+), " +
            "players=(\\d+), " +
            "score=(\\d+), " +
            "level=(\\d+), " +
            "pltfmid=(\\w+), " +
            "crossid=(\\w+), " +
            "ip=([^,]+), " +
            "ping=(\\d+)\r?$";
    private static final Pattern PARSE_LP_PATTERN = Pattern.compile(LP_PARSE_REGEXP);
    private Date date;
    private int id;
    private String name;
    private float posX;
    private float posZ;
    private float posY;
    private float rot1;
    private float rot2;
    private float rot3;
    private boolean isRemote;
    private boolean isOnline;
    private int health;
    private int deaths;
    private int zombies;
    private int players;
    private int score;
    private int level;
    private String pltfmid;
    private String crossid;
    private String ip;
    private int ping;
    private int minutesInGame=-1;
    private Date serverLastSeen;

    private static final Logger log = LogManager.getLogger(PlayerInfo.class);

    public static SimpleDateFormat dateTimeNoSecondsFormat = new SimpleDateFormat("YY-MM-dd HH:mm");

    public PlayerInfo(String data) {
        data = data.replaceAll("[\r\n]", "");
//        this.data = data;
        Matcher matcher = PARSE_LP_PATTERN.matcher(data);
        if(matcher.matches()) {
            date = new Date();
            id = Integer.parseInt(matcher.group(1));
            name = matcher.group(2);
            posX = Float.parseFloat(matcher.group(3));
            posZ = Float.parseFloat(matcher.group(4));
            posY = Float.parseFloat(matcher.group(5));
            rot1 = Float.parseFloat(matcher.group(6));
            rot2 = Float.parseFloat(matcher.group(7));
            rot3 = Float.parseFloat(matcher.group(8));
            isRemote = Boolean.parseBoolean(matcher.group(9));
            health = Integer.parseInt(matcher.group(10));
            deaths = Integer.parseInt(matcher.group(11));
            zombies = Integer.parseInt(matcher.group(12));
            players = Integer.parseInt(matcher.group(13));
            score = Integer.parseInt(matcher.group(14));
            level = Integer.parseInt(matcher.group(15));
            pltfmid = matcher.group(16);
            crossid = matcher.group(17);
            ip = matcher.group(18);
            ping = Integer.parseInt(matcher.group(19));
        } else {
            log.error("PATTERN DO NOT MATCH! Pattern:\n" + LP_PARSE_REGEXP + "\nData:\n" + data);
        }
    }

    public PlayerInfo(int id, String name, String pltfmid, int level, int deaths, int zombies, Date date,
                      float posX, float posY, float posZ, int health, int players, int score) {
        this.id = id;
        this.name = name;
        this.pltfmid = pltfmid;
        this.level = level;
        this.deaths = deaths;
        this.zombies = zombies;
        this.date = date;
        this.posX = posX;
        this.posZ = posZ;
        this.posY = posY;
        this.health = health;
        this.players = players;
        this.score = score;
    }

    public Date getDate() {
        return date;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosZ() {
        return posZ;
    }

    public float getPosY() {
        return posY;
    }

    public float getRot1() {
        return rot1;
    }

    public float getRot2() {
        return rot2;
    }

    public float getRot3() {
        return rot3;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public int getHealth() {
        return health;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getZombies() {
        return zombies;
    }

    public int getPlayers() {
        return players;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return level;
    }

    public String getPltfmid() {
        return pltfmid;
    }

    public String getIp() {
        return ip;
    }

    public int getPing() {
        return ping;
    }

    public int getMinutesInGame() {
        return minutesInGame;
    }

    public Date getServerLastSeen() {
        return serverLastSeen;
    }

    @Override
    public String toString() {
        String out = getName() + "(" + getId() + ")\t: " + getLevel() + "lvl; " + getHealth() + "HP; " + getDeaths() + " deaths; " + getZombies() + "(+" + getPlayers() + ")" + " kills; " + getPing() + "ms";
        out += "\npos: " + posX + ", " + posZ + ", " + posY;
//        out += "\nrot: " + rot1 + ", " + rot2 + ", " + rot3;
//        out += "\nremote:" + isRemote;
//        out += "\n" + data;
        return out;
    }
}
