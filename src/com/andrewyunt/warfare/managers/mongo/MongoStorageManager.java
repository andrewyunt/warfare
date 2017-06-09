package com.andrewyunt.warfare.managers.mongo;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.game.Cage;
import com.andrewyunt.warfare.game.Game;
import com.andrewyunt.warfare.game.loot.LootChest;
import com.andrewyunt.warfare.lobby.Server;
import com.andrewyunt.warfare.lobby.SignDisplay;
import com.andrewyunt.warfare.managers.StorageManager;
import com.andrewyunt.warfare.player.Booster;
import com.andrewyunt.warfare.player.GamePlayer;
import com.andrewyunt.warfare.player.Kit;
import com.andrewyunt.warfare.player.Party;
import com.andrewyunt.warfare.purchases.Powerup;
import com.andrewyunt.warfare.purchases.Purchasable;
import com.andrewyunt.warfare.purchases.PurchaseType;
import com.andrewyunt.warfare.utilities.Utils;
import com.mongodb.CursorType;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class MongoStorageManager extends StorageManager{

    private final Warfare warfare = Warfare.getInstance();

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private MongoCollection<Document> playerCollection;
    private MongoCollection<Document> serverCollection;
    private MongoCollection<Document> partyCollection;
    private MongoCollection<Document> signCollection;
    private MongoCollection<Document> arenaCollection;
    private MongoCollection<Document> partyServersCollection;

    private boolean hasInserted = false;
    private ObjectId serverId;

    @Override
    public boolean connect() {
        ConfigurationSection config = warfare.getConfig().getConfigurationSection("mongo");
        String address = config.getString("address");
        int port = config.getInt("port");
        String database = config.getString("database");
        boolean auth = config.getBoolean("auth.enabled");
        MongoCredential mongoCredential;

        if (auth) {
            String username = config.getString("auth.username");
            String password = config.getString("auth.password");
            mongoCredential = MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
        } else {
            mongoCredential = null;
        }

        try {
            mongoClient = auth ? new MongoClient(new ServerAddress(address, port), Collections.singletonList(mongoCredential)) : new MongoClient(address, port);
            mongoDatabase = mongoClient.getDatabase(database);
        } catch (Exception exception) {
            handleException(exception);
            return false;
        }

        return true;
    }

    @Override
    public void disconnect() {
        mongoClient.close();
    }

    @Override
    public void updateDB() {
        playerCollection = mongoDatabase.getCollection("players");
        serverCollection = mongoDatabase.getCollection("gameservers");
        partyCollection = mongoDatabase.getCollection("parties");
        signCollection = mongoDatabase.getCollection("signs");
        arenaCollection = mongoDatabase.getCollection("arenas");
        partyServersCollection = mongoDatabase.getCollection("partyservers");

        if (StaticConfiguration.LOBBY) {
            Bukkit.getScheduler().runTaskAsynchronously(Warfare.getInstance(), this::getPartyServers);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void savePlayer(GamePlayer player) {
        Document document = new Document();
        document.put("_id", player.getUUID());
        document.put("name", player.getName());
        Party party = warfare.getPartyManager().getParty(player.getUUID());
        if (party != null) {
            document.put("party", party.getLeader());
        }
        if (player.getSelectedKit() != null) {
            document.put("selectedKit", player.getSelectedKit().name());
        }
        if (player.getSelectedPowerup() != null) {
            document.put("selectedPowerup", player.getSelectedPowerup().name());
        }
        document.put("points", player.getPoints());
        document.put("coins", player.getCoins());
        document.put("earnedCoins", player.getEarnedCoins());
        document.put("kills", player.getKills());
        document.put("deaths", player.getDeaths());
        if (player.getDeaths() == 0) {
            document.put("kdr", player.getDeaths());
        } else {
            document.put("kdr", player.getKills() / player.getDeaths());
        }
        document.put("wins", player.getWins());
        document.put("losses", player.getLosses());
        document.put("gamesPlayed", player.getGamesPlayed());
        document.put("purchases", player.getPurchases().entrySet().stream().map(entry -> {
            Document purchase = new Document();
            purchase.put("type", entry.getKey().getType().name());
            purchase.put("name", entry.getKey().getName());
            purchase.put("level", entry.getValue());
            return purchase;
        }).collect(Collectors.toList()));
        document.put("boosters", player.getBoosters().stream().map(booster -> {
            Document boosterDocument = new Document();
            boosterDocument.put("level", booster.getLevel());
            boosterDocument.put("expiry", booster.getExpiry());
            return boosterDocument;
        }).collect(Collectors.toList()));
        //TODO: Better saving method?
        playerCollection.deleteMany(new Document("_id", player.getUUID()));
        playerCollection.insertOne(document);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadPlayer(GamePlayer player) {
        Document document = playerCollection.find(new Document("_id", player.getUUID())).first();
        if (document != null) {
            UUID party = document.get("party", UUID.class);
            if (party != null) {
                loadParty(party);
            }
            String selectedKit = document.getString("selectedKit");
            if (selectedKit != null) {
                player.setSelectedKit(Kit.valueOf(selectedKit));
            }
            String selectedPowerup = document.getString("selectedPowerup");
            if (selectedPowerup != null) {
                player.setSelectedPowerup(Powerup.valueOf(selectedPowerup));
            }
            player.setPoints(document.getInteger("points", 0));
            player.setCoins(document.getInteger("coins", 0));
            player.setEarnedCoins(document.getInteger("earnedCoins", 0));
            player.setKills(document.getInteger("kills", 0));
            player.setWins(document.getInteger("wins", 0));
            player.setLosses(document.getInteger("losses", 0));
            player.setDeaths(document.getInteger("deaths", 0));
            player.setGamesPlayed(document.getInteger("gamesPlayed", 0));
            ((List<Document>) document.get("purchases", List.class)).forEach(purchase -> {
                String type = purchase.getString("type");
                String name = purchase.getString("name");
                int level = purchase.getInteger("level");
                PurchaseType purchaseType = PurchaseType.valueOf(type);
                Purchasable purchasable = purchaseType.getPurchase(name);
                player.getPurchases().put(purchasable, level);
            });
            ((List<Document>) document.get("boosters", List.class)).forEach(purchase -> {
                int level = purchase.getInteger("level");
                LocalDateTime expiry = (LocalDateTime) purchase.get("expiry");
                player.getBoosters().add(new Booster(level, expiry));
            });
        } else {
            List<String> groups = Arrays.asList(Warfare.getPermission().getPlayerGroups(player.getBukkitPlayer()));

            if (groups.contains("Platinum")) {
                player.setCoins(1250);
            } else if (groups.contains("Sapphire")) {
                player.setCoins(2500);
            }  else if (groups.contains("Ruby")) {
                player.setCoins(3750);
            } else if (groups.contains("Emerald")) {
                player.setCoins(5000);
            } else if (groups.contains("Diamond")) {
                player.setCoins(6250);
            } else if (groups.contains("Gold")) {
                player.setCoins(7500);
            } else if (groups.contains("Iron")) {
                player.setCoins(10000);
            }
        }
        player.setLoaded(true);
    }

    @Override
    public List<Server> getServers() {
        List<Server> serverList = new ArrayList<>();
        for (Document document: serverCollection.find()) {
            String name = document.getString("name");
            Server.ServerType serverType = Server.ServerType.valueOf(document.getString("serverType"));
            Game.Stage gameStage = Game.Stage.valueOf(document.getString("gameStage"));
            String mapName = document.getString("mapName");
            int onlinePlayers = document.getInteger("onlinePlayers");
            int maxPlayers = document.getInteger("maxPlayers");
            serverList.add(new Server(name, serverType, gameStage, mapName, onlinePlayers, maxPlayers));
        }
        return serverList;
    }

    @Override
    public void updateServerStatus() {
        if (hasInserted) {
            //Better saving method
            serverCollection.updateOne(Filters.eq("_id", serverId),
                    Filters.and(
                            Updates.set("gameStage", warfare.getGame().getStage().name()),
                            Updates.set("onlinePlayers", warfare.getGame().getPlayers().size())
                    )
            );
        } else {
            Document document = new Document();
            document.put("_id", serverId = new ObjectId());
            document.put("name", StaticConfiguration.SERVER_NAME);
            document.put("serverType", (StaticConfiguration.LOBBY ? Server.ServerType.LOBBY : Warfare.getInstance().
                    getGame().isTeams() ? Server.ServerType.TEAMS : Server.ServerType.SOLO).name());
            document.put("gameStage", warfare.getGame().getStage().name());
            document.put("mapName", StaticConfiguration.MAP_NAME);
            document.put("onlinePlayers", warfare.getGame().getPlayers().size());
            document.put("maxPlayers", warfare.getGame().getCages().size());
            //Delete old server objects
            serverCollection.deleteMany(new Document("name", StaticConfiguration.SERVER_NAME));
            //Insert server object with correct ObjectId
            serverCollection.insertOne(document);
            hasInserted = true;
        }
    }

    @Override
    public void saveParty(Party party) {
        for (UUID member : party.getMembers()) {
            if (Bukkit.getPlayer(member) != null) {
                savePlayer(Warfare.getInstance().getPlayerManager().getPlayer(member));
            }
        }
        Document document = new Document();
        document.put("_id", party.getLeader());
        document.put("open", party.isOpen());
        document.put("members", party.getMembers());
        document.put("invites", party.getInvites());
        //TODO: Better saving method
        partyCollection.deleteMany(new Document("_id", party.getLeader()));
        partyCollection.insertOne(document);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Party loadParty(UUID leaderUUID) {
        Document document = partyCollection.find(new Document("_id", leaderUUID)).first();
        Party party = new Party(leaderUUID);
        if (document != null) {
            party.setOpen(document.getBoolean("open"));
            List<UUID> members = document.get("members", List.class);
            party.getMembers().addAll(members);
            List<UUID> invites = document.get("invites", List.class);
            party.getInvites().addAll(invites);
        }
        return party;
    }

    @Override
    public void setPartyServer(Party party, String server) {
        Document document = new Document();
        document.put("serverid", StaticConfiguration.SERVER_NAME);
        document.put("leader", party.getLeader());
        document.put("server", server);
        partyServersCollection.insertOne(document);
    }

    @Override
    public void getPartyServers() {
        Document query = new Document();
        Document projection = new Document();
        MongoCursor<Document> cursor = partyServersCollection.find(query).projection(projection)
                .cursorType(CursorType.TailableAwait).iterator();
        try {
            while (cursor.hasNext()) {//blocking
                Document document = cursor.next();
                UUID partyLeader = document.get("leader", UUID.class);
                String server = document.getString("server");
                if (server == null) {
                    continue;
                }
                Party party = warfare.getPartyManager().getParty(partyLeader);
                if (party == null) {
                    continue;
                }
                for (UUID member : party.getMembers()) {
                    if (Bukkit.getPlayer(member) != null) {
                        Utils.sendPlayerToServer(Bukkit.getServer().getPlayer(member), server);
                    }
                }
            }
        } catch (IllegalStateException ex) {
            warfare.getLogger().log(Level.INFO, "Cursor Thread closing ");
        }
    }

    @Override
    public void saveSign(SignDisplay signDisplay) {
        Document document = new Document();
        document.put("server", StaticConfiguration.SERVER_NAME);
        document.put("location", serializeLocation(signDisplay.getBukkitSign().getLocation()));
        document.put("type", signDisplay.getType().name());
        document.put("place", signDisplay.getPlace());
        //TODO: Better saving method
        deleteSign(signDisplay);
        signCollection.insertOne(document);
    }

    private Document serializeLocation(Location deserialized) {
        Document document = new Document();
        document.put("world", deserialized.getWorld().getName());
        document.put("x", deserialized.getX());
        document.put("y", deserialized.getY());
        document.put("z", deserialized.getZ());
        return document;
    }

    private Location deserializeLocation(Document serialized) {
        return new Location(
                Bukkit.getWorld(serialized.getString("world")),
                serialized.getDouble("x"),
                serialized.getDouble("y"),
                serialized.getDouble("z")
        );
    }

    @Override
    public void deleteSign(SignDisplay signDisplay) {
        Document document = new Document();
        document.put("server", StaticConfiguration.SERVER_NAME);
        document.put("location", serializeLocation(signDisplay.getLocation()));
        signCollection.deleteMany(document);
    }

    @Override
    public void loadSigns() {
        for (Document document: signCollection.find(new Document("server", StaticConfiguration.SERVER_NAME))) {
            Location location = deserializeLocation(document.get("location", Document.class));
            SignDisplay.Type type = SignDisplay.Type.valueOf(document.getString("type"));
            int place = document.getInteger("place");
            warfare.getSignManager().createSign(location, type, place, true);
        }
    }

    @Override
    public void saveMap() {
        Document document = new Document();
        Game game = warfare.getGame();
        document.put("name", StaticConfiguration.MAP_NAME);
        document.put("teams", game.isTeams());
        document.put("teamSize", game.getTeamSize());
        if (game.getMapLocation() != null) {
            document.put("mapLocation", serializeLocation(game.getMapLocation()));
        }
        if (game.getWaitingLocation() != null) {
            document.put("waitingLocation", serializeLocation(game.getWaitingLocation()));
        }
        document.put("teamSpawns", game.getTeamSpawns().entrySet().stream().map(entry -> {
            Document purchase = new Document();
            purchase.put("team", entry.getKey());
            purchase.put("location", serializeLocation(entry.getValue()));
            return purchase;
        }).collect(Collectors.toList()));
        document.put("cages", game.getCages()
                .stream()
                .map(cage -> {
                    Document cageDocument = new Document();
                    cageDocument.put("name", cage.getName());
                    cageDocument.put("location", serializeLocation(cage.getLocation()));
                    return cageDocument;
                })
                .collect(Collectors.toSet()));
        document.put("chests", game.getLootChests()
                .stream()
                .map(chest -> {
                    Document chestDocument = new Document();
                    chestDocument.put("tier", (int) chest.getTier().getNum());
                    chestDocument.put("location", serializeLocation(chest.getLocation()));
                    return chestDocument;
                })
                .collect(Collectors.toSet())
        );
        arenaCollection.deleteMany(new Document("name", StaticConfiguration.MAP_NAME));
        arenaCollection.insertOne(document);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void loadMap() {
        Game game = Warfare.getInstance().getGame();
        Document document = arenaCollection.find(new Document("name", StaticConfiguration.MAP_NAME)).first();
        if (document != null) {
            if (document.containsKey("teams")) {
                game.setTeams(document.getBoolean("teams"));
            }
            if (document.containsKey("teamSize")) {
                game.setTeamSize(game.getTeamSize());
            }
            Document waitingLocation = document.get("waitingLocation", Document.class);
            if (waitingLocation != null) {
                game.setWaitingLocation(deserializeLocation(waitingLocation));
            }
            Document mapLocation = document.get("mapLocation", Document.class);
            if (mapLocation != null) {
                game.setMapLocation(deserializeLocation(mapLocation));
            }
            List<Document> teamSpawns = ((List<Document>) document.get("teamSpawns", List.class));
            if (teamSpawns != null) {
                teamSpawns.forEach(spawn -> {
                    int team = spawn.getInteger("team");
                    Location location = deserializeLocation(spawn.get("location", Document.class));
                    game.getTeamSpawns().put(team, location);
                });
            }
            List<Document> cages = document.get("cages", List.class);
            if (cages != null) {
                game.setCages(cages.stream()
                        .map(cage -> {
                            String name = cage.getString("name");
                            Document location = cage.get("location", Document.class);
                            return new Cage(name, deserializeLocation(location));
                        }).collect(Collectors.toSet())
                );
            }
            List<Document> chests = document.get("chests", List.class);
            game.setLootChests(chests.stream()
                    .map(chest -> {
                int tier = chest.getInteger("tier");
                Document location = chest.get("location", Document.class);
                return new LootChest(deserializeLocation(location), (byte) tier);
                    }).collect(Collectors.toSet())
            );
        }
    }

    @Override
    public Map<Integer, Map.Entry<Object, Integer>> getTopFiveColumn(String tableName, String select, String orderBy) {
        Document projection = new Document();
        projection.put(select, true);
        projection.put(orderBy, true);
        Map<Integer, Map.Entry<Object, Integer>> topFiveMap = new HashMap<>();
        int place = 1;
        for (Document document: playerCollection.find()
                .sort(new Document(orderBy, -1))
                .limit(5)
                .projection(projection)) {
            Object selectField = document.get(select);
            Integer orderField = document.getInteger(orderBy);
            topFiveMap.put(place, new AbstractMap.SimpleEntry<>(selectField, orderField));
            place++;
        }
        return topFiveMap;
    }
}
