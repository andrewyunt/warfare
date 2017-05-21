package com.andrewyunt.warfare.managers.mongo;

import com.andrewyunt.warfare.Warfare;
import com.andrewyunt.warfare.configuration.StaticConfiguration;
import com.andrewyunt.warfare.exception.SignException;
import com.andrewyunt.warfare.managers.StorageManager;
import com.andrewyunt.warfare.objects.*;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

public class MongoStorageManager extends StorageManager{
    private final Warfare warfare;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private MongoCollection<Document> playerCollection;
    private MongoCollection<Document> serverCollection;
    private MongoCollection<Document> partyCollection;
    private MongoCollection<Document> signCollection;
    private MongoCollection<Document> arenaCollection;

    private boolean hasInserted = false;
    private ObjectId serverId;

    public MongoStorageManager(Warfare warfare) {
        this.warfare = warfare;
    }

    public boolean connect() {
        ConfigurationSection config = warfare.getConfig().getConfigurationSection("mongo");
        String address = config.getString("address");
        int port = config.getInt("port");
        String database = config.getString("database");
        boolean auth = config.getBoolean("auth.enabled");
        MongoCredential mongoCredential;
        if(auth){
            String username = config.getString("auth.username");
            String password = config.getString("auth.password");
            mongoCredential = MongoCredential.createMongoCRCredential(username, database, password.toCharArray());
        }
        else{
            mongoCredential = null;
        }
        try{
            mongoClient = auth ? new MongoClient(new ServerAddress(address, port), Collections.singletonList(mongoCredential)) : new MongoClient(address, port);
            mongoDatabase = mongoClient.getDatabase(database);
        }
        catch (Exception exception){
            handleException(exception);
            return false;
        }
        return true;
    }

    
    public void disconnect() {
        mongoClient.close();
    }

    
    public void updateDB() {
        playerCollection = mongoDatabase.getCollection("players");
        serverCollection = mongoDatabase.getCollection("gameservers");
        partyCollection = mongoDatabase.getCollection("parties");
        signCollection = mongoDatabase.getCollection("signs");
        arenaCollection = mongoDatabase.getCollection("arenas");
    }

    @SuppressWarnings("unchecked")
    public void savePlayer(GamePlayer player) {
        Document document = new Document();
        document.put("_id", player.getUUID());
        document.put("name", player.getName());
        Party party = warfare.getPartyManager().getParty(player.getUUID());
        if(party != null){
            document.put("party", party.getLeader());
        }
        if(player.getSelectedKit() != null){
            document.put("selectedKit", player.getSelectedKit().name());
        }
        if(player.getSelectedKit() != null){
            document.put("selectedPowerup", player.getSelectedPowerup().name());
        }
        document.put("points", player.getPoints());
        document.put("coins", player.getCoins());
        document.put("earnedCoins", player.getEarnedCoins());
        document.put("kills", player.getKills());
        document.put("wins", player.getWins());
        document.put("purchases", new Document((Map) player.getPurchases()));
        //TODO: Better saving method?
        playerCollection.deleteMany(new Document("_id", player.getUUID()));
        playerCollection.insertOne(document);
    }


    @SuppressWarnings("unchecked")
    public void loadPlayer(GamePlayer player) {
        Document document = playerCollection.find(new Document("_id", player.getUUID())).first();
        if(document != null){
            UUID party = document.get("party", UUID.class);
            if(party != null){
                loadParty(party);
            }
            String selectedKit = document.getString("selectedKit");
            if(selectedKit != null){
                player.setSelectedKit(Kit.valueOf(selectedKit));
            }
            String selectedPowerup = document.getString("selectedPowerup");
            if(selectedPowerup != null){
                player.setSelectedPowerup(Powerup.valueOf(selectedPowerup));
            }
            player.setPoints(document.getInteger("points", 0));
            player.setCoins(document.getInteger("coins", 0));
            player.setEarnedCoins(document.getInteger("earnedCoins", 0));
            player.setKills(document.getInteger("kills", 0));
            player.setWins(document.getInteger("wins", 0));
            player.setPurchases(document.get("purchases", Map.class));
        }
        player.setLoaded(true);
    }

    
    public List<Server> getServers() {
        List<Server> serverList = new ArrayList<>();
        for(Document document: serverCollection.find()){
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

    
    public void updateServerStatus() {
        if(hasInserted){
            //Better saving method
            serverCollection.updateOne(Filters.eq("_id", serverId),
                    Filters.and(
                            Updates.set("gameStage", warfare.getGame().getStage().name()),
                            Updates.set("onlinePlayers", warfare.getGame().getPlayers().size())
                    )
            );
        }
        else{
            Document document = new Document();
            document.put("_id", serverId = new ObjectId());
            document.put("name", StaticConfiguration.SERVER_NAME);
            document.put("serverType", (StaticConfiguration.LOBBY ? Server.ServerType.LOBBY : Server.ServerType.GAME).name());
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

    
    public void saveParty(Party party) {
        Document document = new Document();
        document.put("_id", party.getLeader());
        document.put("open", party.isOpen());
        document.put("members", party.getMembers());
        document.put("invites", party.getInvites());
        //TODO: Better saving method
        partyCollection.deleteMany(new Document("_id", party.getLeader()));
        partyCollection.insertOne(document);
    }


    @SuppressWarnings("unchecked")
    public Party loadParty(UUID leaderUUID) {
        Document document = partyCollection.find(new Document("_id", leaderUUID)).first();
        Party party = new Party(leaderUUID);
        if(document != null){
            party.setOpen(document.getBoolean("open"));
            List<UUID> members = document.get("members", List.class);
            party.getMembers().addAll(members);
            List<UUID> invites = document.get("invites", List.class);
            party.getInvites().addAll(invites);
        }
        return party;
    }

    
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
    
    public void deleteSign(SignDisplay signDisplay) {
        Document document = new Document();
        document.put("server", StaticConfiguration.SERVER_NAME);
        document.put("location", serializeLocation(signDisplay.getBukkitSign().getLocation()));
        signCollection.deleteMany(document);
    }

    
    public void loadSigns() {
        for(Document document: signCollection.find(new Document("server", StaticConfiguration.SERVER_NAME))){
            Location location = deserializeLocation(document.get("location", Document.class));
            SignDisplay.Type type = SignDisplay.Type.valueOf(document.getString("type"));
            int place = document.getInteger("place");
            try {
                warfare.getSignManager().createSign(location, type, place, true);
            } catch (SignException exception) {
                handleException(exception);
            }
        }
    }

    
    public void saveArena() {
        Document document = new Document();
        Arena arena = warfare.getArena();
        document.put("name", StaticConfiguration.MAP_NAME);
        if(arena.getMapLocation() != null){
            document.put("mapLocation", serializeLocation(arena.getMapLocation()));
        }
        document.put("cages", arena.getCageLocations().entrySet()
                .stream()
                .map(cage -> {
                    Document cageDocument = new Document();
                    cageDocument.put("name", cage.getKey());
                    cageDocument.put("location", serializeLocation(cage.getValue()));
                    return cageDocument;
                })
                .collect(Collectors.toSet()));
        document.put("chests", arena.getLootChests()
                .stream()
                .map(chest -> {
                    Document chestDocument = new Document();
                    chestDocument.put("tier", (int) chest.getTier());
                    chestDocument.put("location", serializeLocation(chest.getLocation()));
                    return chestDocument;
                })
                .collect(Collectors.toSet())
        );
        arenaCollection.deleteMany(new Document("name", StaticConfiguration.MAP_NAME));
        arenaCollection.insertOne(document);
    }


    @SuppressWarnings("unchecked")
    public Arena loadArena() {
        Document document = arenaCollection.find(new Document("name", StaticConfiguration.MAP_NAME)).first();
        Arena arena = new Arena();
        if(document != null){
            Document mapLocation = document.get("mapLocation", Document.class);
            if(mapLocation != null){
                arena.setMapLocation(deserializeLocation(mapLocation));
            }
            List<Document> cages = document.get("cages", List.class);
            cages.forEach(cage -> {
                String name = cage.getString("name");
                Document location = cage.get("location", Document.class);
                arena.addCageLocation(name, deserializeLocation(location));
            });
            List<Document> chests = document.get("chests", List.class);
            arena.setLootChests(chests.stream()
                    .map(chest -> {
                int tier = chest.getInteger("tier");
                Document location = chest.get("location", Document.class);
                return new LootChest(deserializeLocation(location), (byte) tier);
                    }).collect(Collectors.toSet())
            );
        }
        return arena;
    }

    
    public Map<Integer, Map.Entry<Object, Integer>> getTopFiveColumn(String tableName, String select, String orderBy) {
        Document projection = new Document();
        projection.put(select, true);
        projection.put(orderBy, true);
        Map<Integer, Map.Entry<Object, Integer>> topFiveMap = new HashMap<>();
        int place = 1;
        for(Document document: playerCollection.find()
                .sort(new Document(orderBy, -1))
                .limit(5)
                .projection(projection)){
            Object selectField = document.get(select);
            Integer orderField = document.getInteger(orderBy);
            topFiveMap.put(place, new AbstractMap.SimpleEntry<>(selectField, orderField));
            place++;
        }
        return topFiveMap;
    }
}
